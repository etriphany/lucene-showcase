package com.etriphany.fulltext.service;

import com.etriphany.fulltext.domain.SearchException;
import com.etriphany.fulltext.domain.io.ContentTerm;
import com.etriphany.fulltext.domain.util.ContentTermFrequencyComparator;
import com.etriphany.fulltext.domain.io.TermsRequest;
import com.etriphany.fulltext.domain.io.TermsResponse;
import com.etriphany.fulltext.domain.util.FieldNames;
import com.etriphany.fulltext.service.core.IndexSearcherFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Content terms service.
 *
 * @author cadu.goncalves
 *
 */
@Service
public class TermsService {

    // Maximal number of terms.
    public static final int MAX_TERMS = 50;

    private static final Logger LOGGER = LogManager.getLogger(TermsService.class.getName());

    // The index path must defined externally using parameter
    @Value("${fulltext.index.path}")
    private String indexPathParameter;

    /**
     * Terms recovery.
     *
     * @param request {@link TermsRequest}
     * @return {@link TermsResponse}
     */
    public TermsResponse listTerms(TermsRequest request) throws SearchException {
        try {
            Path indexPath = Paths.get(indexPathParameter);

            // Build searcher
            IndexSearcher searcher = IndexSearcherFactory.getInstance(indexPath, new HashSet<>(), false);

            // Search a document that match the path
            Query query = new TermQuery(new Term(FieldNames.PATH, request.getPath()));
            TopDocs topDocs = searcher.search(query, 1);
            if(topDocs.totalHits > 0) {
                return buildResponse(searcher, topDocs.scoreDocs[0]);
            } else {
                // No matches
                return new TermsResponse(new TreeSet<>());
            }
        } catch (IOException ioe) {
            LOGGER.error(ioe);
            throw new SearchException(SearchException.ErrorType.INPUT_OUTPUT_FAILURE, ioe);
        }
    }

    /**
     * Build terms recovery response.
     *
     * @param searcher {@link org.apache.lucene.search.IndexSearcher}
     * @param scoreDoc {@link org.apache.lucene.search.ScoreDoc}
     * @return {@link TermsResponse}
     */
    private TermsResponse buildResponse(final IndexSearcher searcher, final ScoreDoc scoreDoc) throws SearchException {
        List<ContentTerm> list = new ArrayList<>();
        try {
            // Recover content terms
            Terms terms = searcher.getIndexReader().getTermVector(scoreDoc.doc, FieldNames.CONTENTS);

            if (terms != null && terms.size() > 0) {
                // Each term is recovered
                TermsEnum termsEnum = terms.iterator();
                BytesRef ref;
                while ((ref = termsEnum.next()) != null) {
                    PostingsEnum docsEnum = termsEnum.postings(null);
                    if (docsEnum != null) {
                        while (docsEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
                            list.add(new ContentTerm(ref.utf8ToString(), docsEnum.freq()));
                        }
                    }
                }
            }

            // Subset wrapped as response
            list.sort(new ContentTermFrequencyComparator());
            if(list.size() < MAX_TERMS) {
                return new TermsResponse(new TreeSet<>(list));
            } else {
                return new TermsResponse(new TreeSet<>(list.subList(0, MAX_TERMS)));
            }
        } catch (IOException ioe) {
            LOGGER.error(ioe);
            throw new SearchException(SearchException.ErrorType.INPUT_OUTPUT_FAILURE, ioe);
        }
    }

}
