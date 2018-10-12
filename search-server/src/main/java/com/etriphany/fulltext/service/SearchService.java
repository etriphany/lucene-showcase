package com.etriphany.fulltext.service;

import com.etriphany.fulltext.domain.embed.Content;
import com.etriphany.fulltext.domain.io.DeepPage;
import com.etriphany.fulltext.domain.io.SearchRequest;
import com.etriphany.fulltext.domain.io.SearchResponse;
import com.etriphany.fulltext.domain.SearchException;
import com.etriphany.fulltext.domain.util.FieldNames;
import com.etriphany.fulltext.service.core.AnalyzerFactory;
import com.etriphany.fulltext.service.core.IndexSearcherFactory;
import com.etriphany.fulltext.service.core.LanguageExtractor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Content search service.
 *
 * @author cadu.goncalves
 *
 */
@Service
public class SearchService {

    public static final Integer PAGE_SIZE = 10;

    private static final Logger LOGGER = LogManager.getLogger(SearchService.class.getName());

    // The index path must defined externally using parameter
    @Value("${fulltext.index.path}")
    private String indexPathParameter;

    @Autowired
    private LanguageExtractor languageDetectorUtil;

    /**
     * Search contents.
     *
     * @param request {@link SearchRequest}
     * @return {@link SearchResponse}
     * @throws SearchException in case of search error
     */
    public SearchResponse search(SearchRequest request) throws SearchException {
        if (!request.isValid()) {
            LOGGER.error("Invalid SearchRequest object");
            throw new IllegalArgumentException();
        }

        try {
            Path indexPath = Paths.get(indexPathParameter);

            // Compute requested languages
            Set<String> languages = processLanguages(request);

            // Build searcher
            IndexSearcher searcher = buildSearcher(languages, indexPath);

            // Build analyzer
            Analyzer analyzer = buildAnalyzer(languages);

            // Proceed search
            return search(request, searcher, analyzer);
        } catch (ParseException pse) {
            LOGGER.error(pse);
            throw new SearchException(SearchException.ErrorType.QUERY_PARSE_FAILURE, pse);
        } catch (IOException ioe) {
            LOGGER.error(ioe);
            throw new SearchException(SearchException.ErrorType.INPUT_OUTPUT_FAILURE, ioe);
        }
    }

    /**
     * Compute the proper language to match contents.
     *
     * @param request {@link SearchRequest}
     * @return {@link java.util.Set}
     */
    private Set<String> processLanguages(SearchRequest request) {
        Set<String> languages = request.getLanguages();

        // Detect language from query?
        if(request.isDetectLanguage() && languages.isEmpty()) {
            languages = new HashSet<>();
            languages.add(languageDetectorUtil.detect(request.getQuery()));
        }
        return languages;
    }

    /**
     * Build searcher that matches request languages.
     *
     * @param languages {@link java.util.Set} of languages
     * @param indexPath Index root path
     * @return {@link org.apache.lucene.search.IndexSearcher}
     * @throws SearchException in case of search error
     */
    private IndexSearcher buildSearcher(Set<String> languages, Path indexPath) throws SearchException {
        if(languages.size() == 1) {
            // Single index searcher
            return IndexSearcherFactory.getInstance(indexPath, languages.iterator().next());
        } else {
            // Multiple index searcher
            return IndexSearcherFactory.getInstance(indexPath, languages, false);
        }
    }

    /**
     * Build analyzer that matches request languages.
     *
     * @param languages  {@link java.util.Set} of languages
     * @return {@link org.apache.lucene.analysis.Analyzer}
     */
    private Analyzer buildAnalyzer(Set<String> languages) {
        if(languages.size() == 1) {
            // Analyzer that matches the index's language
            return AnalyzerFactory.getInstance(languages.iterator().next());
        } else {
            // TODO: How to use single analyzer with multireader and get decent results?
            return new StandardAnalyzer();
        }
    }

    /**
     * Full text search.
     *
     * @param request {@link SearchRequest}
     * @param searcher {@link org.apache.lucene.search.IndexSearcher}
     * @param analyzer {@link org.apache.lucene.analysis.Analyzer}
     * @return {@link SearchResponse}
     * @throws ParseException in case of search error
     * @throws IOException in case of filesystem error
     */
    private SearchResponse search(final SearchRequest request, final IndexSearcher searcher, final Analyzer analyzer) throws ParseException, IOException {
        // Use QueryParser to construct the query based on input query
        Query query = new QueryParser(FieldNames.CONTENTS, analyzer).parse(request.getQuery());

        // Compute basic paging stuff (simple when using Lucene deep paging)
        Integer total = countMatches(query, searcher);

        // Search considering paging (keeping in mind that ScoreDoc is a "GoF flyweight")
        DeepPage deepPage = DeepPage.fromString(request.getDeep());
        List<ScoreDoc> scoreDocs;
        if(deepPage == null) {
            TopScoreDocCollector docCollector = TopScoreDocCollector.create(total);
            searcher.search(query, docCollector);
            scoreDocs = Arrays.asList(docCollector.topDocs(0, PAGE_SIZE).scoreDocs);
        } else {
            ScoreDoc after = new ScoreDoc(deepPage.getDoc(), deepPage.getScore());
            TopDocs topDocs = searcher.searchAfter(after, query, total);
            // Must limit based on page size or we will return all matches at once latter.
            scoreDocs = Arrays.asList(Arrays.copyOf(topDocs.scoreDocs, PAGE_SIZE));
        }
        return buildResponse(searcher, deepPage, total, scoreDocs );
    }

    /**
     * Build search response.
     *
     * @param searcher {@link org.apache.lucene.search.IndexSearcher}
     * @param total Total of matches
     * @param deepPage Deep paging status
     * @param scoreDocs {@link java.util.List} of matched documents
     * @return SearchResponse
     * @throws IOException in case of filesystem error
     */
    private SearchResponse buildResponse(final IndexSearcher searcher, DeepPage deepPage, Integer total, final List<ScoreDoc> scoreDocs) throws IOException {
        // Extract flyweight contents and convert to our domain
        List<Content> matches = new ArrayList<>();
        ScoreDoc lastMatch = null;
        for (ScoreDoc scoreDoc : scoreDocs) {
            if (scoreDoc == null) {
                continue;
            }
            lastMatch = scoreDoc;

            Document document = searcher.doc(scoreDoc.doc);

            Content content = new Content();
            content.setId(document.getField(FieldNames.ID).stringValue());
            content.setPath(document.getField(FieldNames.PATH).stringValue());
            content.setLanguage(document.getField(FieldNames.LANGUAGE).stringValue());
            matches.add(content);
        }

        // The last match will provide support to deep paging
        String deep = "";
        if(lastMatch != null) {
            if(deepPage != null && lastMatch.doc == deepPage.getDoc() || matches.size() < PAGE_SIZE) {
                // Nothing more to load at this point
                deep = "";
            } else {
                deep = new DeepPage(lastMatch.doc, lastMatch.score).toString();
            }
        }

        // Response done
        return new SearchResponse(matches, total, deep);
    }

    /**
     * Compute the total of documents that match the query.
     *
     * @param query    {@link org.apache.lucene.search.Query}
     * @param searcher {@link org.apache.lucene.search.IndexSearcher}
     * @return Total of matched documents
     * @throws IOException in case of filesystem error
     */
    private Integer countMatches(final Query query, final IndexSearcher searcher) throws IOException {
        TotalHitCountCollector totalCollector = new TotalHitCountCollector();
        searcher.search(query, totalCollector);
        return totalCollector.getTotalHits();
    }

}
