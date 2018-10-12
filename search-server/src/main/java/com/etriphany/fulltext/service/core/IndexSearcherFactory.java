package com.etriphany.fulltext.service.core;

import com.etriphany.fulltext.domain.SearchException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Factory for {@link org.apache.lucene.search.IndexSearcher}
 *
 * @author cadu.goncalves
 *
 */
public final class IndexSearcherFactory {

    private static final Logger LOGGER = LogManager.getLogger(IndexSearcherFactory.class.getName());


    // Holds index reader instances bound to specific language.
    private static final ConcurrentMap<String, IndexReader> READER_INSTANCES = new ConcurrentHashMap<>();

    // Holds index multireader instances bound to specific language set.
    private static final ConcurrentMap<String, MultiReader> MULTIREADER_INSTANCES = new ConcurrentHashMap<>();

    /**
     * Private constructor
     */
    private IndexSearcherFactory() {
    }

    /**
     * Recover index searcher instance.
     *
     * @param indexPath Index path.
     * @return {@link org.apache.lucene.search.IndexSearcher}
     * @throws SearchException in case of search error
     */
    public static IndexSearcher getInstance(Path indexPath) throws SearchException {
        return getInstance(indexPath, LanguageExtractor.UNKNOWN_LANGUAGE);
    }

    /**
     * Recover index searcher instance.
     *
     * @param indexPath Index path.
     * @param language  The language used to analyze index contents.
     * @return {@link org.apache.lucene.search.IndexSearcher}
     * @throws SearchException in case of search error
     */
    public static IndexSearcher getInstance(Path indexPath, String language) throws SearchException {
        language = language.toLowerCase();
        IndexReader reader = READER_INSTANCES.get(language);
        if (reader == null) {
            reader = buildReader(indexPath, language);
        } else {
            reader = reopenIfChanged(reader, language);
        }
        return new IndexSearcher(reader);
    }

    /**
     * Recover index searcher instance able to search on multiple indexes at once.
     *
     * @param indexPath Index path, some remote filesystems may perform really bad.
     * @param languages The languages used to analyze index contents.
     * @return {@link org.apache.lucene.search.IndexSearcher}
     * @throws SearchException in case of search error
     */
    public static IndexSearcher getInstance(final Path indexPath, final Set<String> languages, boolean forceRebuild) throws SearchException {
        final Set<String> indexedLanguages = new HashSet<>();
        if (languages.isEmpty()) {
            // No languages on parameter, collect from filesystem
            try {
                Files.walkFileTree(indexPath, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                        if (!dir.equals(indexPath)) {
                            indexedLanguages.add(dir.toFile().getName());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException ioe) {
                LOGGER.error(ioe);
                throw new SearchException(SearchException.ErrorType.INPUT_OUTPUT_FAILURE, ioe);
            }

            // Repeat the call passing all indexed languages
            return getInstance(indexPath, indexedLanguages, false);
        }

        // Sort languages to build a unique key
        SortedSet<String> sortedLanguages = new TreeSet<>(languages);
        String languagesKey = StringUtils.join(languages, "_");

        // Recover/build individual multireader
        // also check force flag since ConcurrentHashMap do not allow "put" with null values
        MultiReader multiReader = MULTIREADER_INSTANCES.get(languagesKey);
        if (multiReader == null || forceRebuild) {
            // Recover/build individual readers
            List<IndexReader> readers = new ArrayList<>();
            for (String language : languages) {
                language = language.toLowerCase();
                IndexReader reader = READER_INSTANCES.get(language);
                if (reader == null) {
                    reader = buildReader(indexPath, language);
                } else {
                    reader = reopenIfChanged(reader, language);
                }
                readers.add(reader);
            }

            try {
                // Recover/build individual multireader
                multiReader = new MultiReader(readers.toArray(new IndexReader[readers.size()]));
                // Cache it as well using the language set as the key
                MULTIREADER_INSTANCES.put(languagesKey, multiReader);
                return new IndexSearcher(multiReader);
            } catch (IOException ioe) {
                LOGGER.error(ioe);
                throw new SearchException(SearchException.ErrorType.INPUT_OUTPUT_FAILURE, ioe);
            }
        } else {
            multiReader = reopenIfChanged(multiReader, sortedLanguages);
            try {
                return new IndexSearcher(multiReader);
            } catch(AlreadyClosedException ace) {
                // Impossible to reopen build again
                return getInstance(indexPath, languages, true);
            }
        }
    }

    /**
     * Constructs thread safe index reader.
     *
     * @param indexPath Index path, some remote filesystems may perform really bad.
     * @param language  The language used to analyze index contents.
     * @return {@link org.apache.lucene.index.IndexReader}
     * @throws SearchException in case of search error
     */
    private static IndexReader buildReader(Path indexPath, String language) throws SearchException {
        try {
            /*
              FSDirectory internally picks the best directory implementation based on current OS
              We just have to shift to the correct index based on the language
             */
            if (!AnalyzerFactory.getKnownLanguages().contains(language.toLowerCase())) {
                language = LanguageExtractor.UNKNOWN_LANGUAGE;
            }
            Path realPath = Paths.get(indexPath.toString(), language);
            if (!Files.exists(realPath)) {
                throw new SearchException(SearchException.ErrorType.NO_IDEX);
            }

            Directory dir = FSDirectory.open(realPath);
            IndexReader reader = DirectoryReader.open(dir);
            READER_INSTANCES.put(language, reader);
            return reader;
        } catch (IOException ioe) {
            LOGGER.error(ioe);
            throw new SearchException(SearchException.ErrorType.INPUT_OUTPUT_FAILURE, ioe);
        }
    }

    /**
     * Readers get staled when index changes, to refresh they must be reopened (which is faster than recreating).
     * <p/>
     * This method uses same approach of {@link org.apache.lucene.search.SearcherManager}
     *
     * @param reader   {@link org.apache.lucene.index.IndexReader}
     * @param language The associated index language
     */
    private static IndexReader reopenIfChanged(IndexReader reader, String language) throws SearchException {
        try {
            if (reader instanceof DirectoryReader) {
                IndexReader newReader = DirectoryReader.openIfChanged((DirectoryReader) reader);
                if (newReader != null) {
                    // Update reference
                    READER_INSTANCES.put(language, newReader);
                    reader.close();
                    return newReader;
                } else {
                    // No changes
                    return reader;
                }
            } else {
                // Should never happen
                return reader;
            }
        } catch (IOException ioe) {
            LOGGER.error(ioe);
            throw new SearchException(SearchException.ErrorType.INPUT_OUTPUT_FAILURE, ioe);
        }
    }

    /**
     * Readers get staled when index changes, to refresh they must be reopened (which is faster than recreating).
     * <p/>
     * This method uses same approach of {@link org.apache.lucene.search.SearcherManager}
     *
     * @param reader    {@link org.apache.lucene.index.MultiReader}
     * @param languages The associated index languages
     */
    private static MultiReader reopenIfChanged(MultiReader reader, Set<String> languages) throws SearchException {
        String languagesKey = StringUtils.join(languages, "_");
        List<IndexReader> readers = new ArrayList<>();
        boolean changed = false;
        for (String language : languages) {
            IndexReader currentReader = READER_INSTANCES.get(language);
            IndexReader newReader = reopenIfChanged(currentReader, language);
            readers.add(newReader);
            if (newReader != currentReader) {
                changed = true;
            }
        }
        if(changed) {
            try {
                MultiReader newMultiReader = new MultiReader(readers.toArray(new IndexReader[readers.size()]));
                reader.close();
                MULTIREADER_INSTANCES.put(languagesKey, newMultiReader);
                return newMultiReader;
            } catch (IOException ioe) {
                LOGGER.error(ioe);
                throw new SearchException(SearchException.ErrorType.INPUT_OUTPUT_FAILURE, ioe);
            }
        } else {
            return reader;
        }
    }
}
