package com.etriphany.fulltext.service.core;

import com.etriphany.fulltext.domain.IndexingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class basically aims to create and keep multiple instances of {@link org.apache.lucene.index.IndexWriter}
 * each one responsible to create/update an index bound to a particular language.
 * <p/>
 * Keeping documents in different languages inside the same index is a bad practice, because the analysis performed
 * in the index will not match all the requirements for all languages (tokenization, stopwords, stemming).
 * <p/>
 * Keeping each index bound to a specific language provides an optimal approach during analysis and index management.
 * <p/>
 * The objects from {@link org.apache.lucene.index.IndexWriter} class are thread safe, sharing them
 * among multiple threads is advisable.
 *
 * @author cadu.goncalves
 *
 */
public final class IndexWriterFactory {

    private static final Logger LOGGER = LogManager.getLogger(IndexWriterFactory.class.getName());

    // Holds index writer instances bound to specific language.
    private static final ConcurrentMap<String, IndexWriter> INSTANCES = new ConcurrentHashMap<>();

    /**
     * Private constructor.
     */
    private IndexWriterFactory(){
    }

    /**
     * Recover thread safe index writer.
     *
     * @param indexPath Index path, some remote filesystems may perform really bad.
     * @return {@link org.apache.lucene.index.IndexWriter}
     * @throws IndexingException in case of indexing error
     */
    public static IndexWriter getInstance(Path indexPath) throws IndexingException {
        return getInstance(indexPath, LanguageExtractor.UNKNOWN_LANGUAGE);
    }

    /**
     * Recover thread safe index writer.
     *
     * @param indexPath Index path, some remote filesystems may perform really bad.
     * @param language  The language used to analyze index contents.
     * @return {@link org.apache.lucene.index.IndexWriter}
     * @throws IndexingException in case of indexing error
     */
    public static IndexWriter getInstance(Path indexPath, String language) throws IndexingException {
        language = language.toLowerCase();
        IndexWriter writer = INSTANCES.get(language);
        if (writer == null || !writer.isOpen()) {
            writer = buildWriter(indexPath, language);
        }
        return writer;
    }

    /**
     * Commit data from all allocated instances.
     */
    public static void commitAll() {
        for(IndexWriter writer : INSTANCES.values()) {
            try {
                writer.commit();
            } catch (IOException ioe) {
                LOGGER.error(ioe);
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Constructs thread safe index writer.
     *
     * @param indexPath Index path, some remote filesystems may perform really bad.
     * @param language  The language used to analyze index contents.
     * @return {@link org.apache.lucene.index.IndexWriter}
     * @throws IndexingException in case of indexing error
     */
    private static IndexWriter buildWriter(Path indexPath, String language) throws IndexingException {
        try {

            language = language.toLowerCase();
            /*
              FSDirectory internally picks the best directory implementation based on current OS
              We just have to shift to the correct index based on the language
             */
            if(!AnalyzerFactory.getKnownLanguages().contains(language)) {
                language = LanguageExtractor.UNKNOWN_LANGUAGE;
            }
            Path realPath = Paths.get(indexPath.toString(), language);
            Directory dir = FSDirectory.open(realPath);

            // Recover the proper analyzer
            Analyzer analyzer;
            if (!LanguageExtractor.UNKNOWN_LANGUAGE.equals(language)) {
                analyzer = AnalyzerFactory.getInstance(language);
            } else {
                analyzer = new StandardAnalyzer();
            }

            // Index configurations
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            // Performance stuff
            //iwc.setUseCompoundFile(false);

            // Create writer and store it for reuse
            IndexWriter writer = new IndexWriter(dir, iwc);
            INSTANCES.put(language, writer);
            return writer;
        } catch (IOException ioe) {
            LOGGER.error(ioe);
            throw new IndexingException(IndexingException.ErrorType.INPUT_OUTPUT_FAILURE, ioe);
        }
    }
}
