package com.etriphany.fulltext.service;

import com.etriphany.fulltext.service.core.ContentExtractor;
import com.etriphany.fulltext.domain.embed.Content;
import com.etriphany.fulltext.domain.embed.ContentOperation;
import com.etriphany.fulltext.domain.persistent.IndexRequest;
import com.etriphany.fulltext.domain.IndexingException;
import com.etriphany.fulltext.domain.util.FieldNames;
import com.etriphany.fulltext.service.core.IndexWriterFactory;
import com.etriphany.fulltext.service.core.LanguageExtractor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Content indexing service.
 *
 * @author cadu.goncalves
 *
 */
@Service
public class IndexService {

    private static final Logger LOGGER = LogManager.getLogger(IndexService.class.getName());

    // Define properties of content field
    private static final FieldType CONTENT_FIELD_TYPE = new FieldType();

    /**
     * The index path must defined externally using parameter
     */
    @Value("${fulltext.index.path}")
    private String indexPathParameter;

    @Autowired
    private LanguageExtractor languageDetectorUtil;

    // Static initialization
    static {
        // Very much like not stored TextField, with term vectors enabled
        CONTENT_FIELD_TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        CONTENT_FIELD_TYPE.setTokenized(true);
        CONTENT_FIELD_TYPE.setStoreTermVectors(true);
        CONTENT_FIELD_TYPE.freeze();
    }

    /**
     * Process indexing request for a particular content.
     *
     * @param request   {@link IndexRequest}
     * @throws IndexingException if path points to a directory
     */
    public void process(IndexRequest request) throws IndexingException {
        Content content = request.getContent() ;
        ContentOperation operation = request.getOperation();
        if (content == null) {
            LOGGER.error("Content is null");
            throw new IndexingException(IndexingException.ErrorType.NULL_CONTENT);
        }
        Path docsPath = content.getFilePath();
        if (Files.isDirectory(docsPath)) {
            LOGGER.error("Content must be a file not a directory");
            throw new IndexingException(IndexingException.ErrorType.CONTENT_NOT_FILE);
        }

        // Detect language
        String language = languageDetectorUtil.detect(content.getFilePath());
        content.setLanguage(language);

        // Build index writer
        Path indexPath = Paths.get(indexPathParameter);
        IndexWriter writer = IndexWriterFactory.getInstance(indexPath, language);

        try {
            switch (operation) {
                case ADD:
                    addDocument(writer, content);
                    break;
                case UPDATE:
                    updateDocument(writer, content);
                    break;
                case DELETE:
                    deleteDocument(writer, content);
                    break;
            }
        } catch (IOException ioe) {
            LOGGER.error(ioe);
            throw new IndexingException(IndexingException.ErrorType.INPUT_OUTPUT_FAILURE, ioe);
        }

        IndexWriterFactory.commitAll();
    }

    /**
     * Flush any pending index write.
     */
    public void flush() {
        IndexWriterFactory.commitAll();
    }

    /**
     * Add document to index.
     *
     * @param writer  {@link org.apache.lucene.index.IndexWriter}
     * @param content {@link Content}
     * @throws IOException in case of filesystem error
     */
    private void addDocument(final IndexWriter writer, Content content) throws IOException {
        // New empty document
        Document doc = new Document();

        // Id = stringfield (indexed but not tokenized)
        Field id = new StringField(FieldNames.ID, content.getId(), Field.Store.YES);
        doc.add(id);

        // Path = stringfield (indexed but not tokenized).
        Field pathField = new StringField(FieldNames.PATH, content.getFilePath().toString(), Field.Store.YES);
        doc.add(pathField);

        // Language = stringfield (indexed but not tokenized).
        Field languageField = new StringField(FieldNames.LANGUAGE, content.getLanguage(), Field.Store.YES);
        doc.add(languageField);

        // Contents = textfield (indexed and tokenized, the content is not stored, include term vectors)
        Field contentsField = new Field(FieldNames.CONTENTS, ContentExtractor.extractContent(content.getFilePath()), CONTENT_FIELD_TYPE);
        //new TextField("contents", ContentExtractor.extractContent(content.getFilePath()), Field.Store.NO);
        doc.add(contentsField);

        writer.addDocument(doc);
    }

    /**
     * Update document on index.
     *
     * @param writer  {@link org.apache.lucene.index.IndexWriter}
     * @param content {@link Content}
     * @throws IOException in case of filesystem error
     */
    private void updateDocument(final IndexWriter writer, Content content) throws IOException {
        // New empty document
        Document doc = new Document();

        // Id = stringfield (not searchable at all)
        Field id = new StringField(FieldNames.ID, content.getId(), Field.Store.NO);
        doc.add(id);

        // Path = stringfield (indexed but not tokenized).
        Field pathField = new StringField(FieldNames.PATH, content.getFilePath().toString(), Field.Store.YES);
        doc.add(pathField);

        // Language = stringfield (indexed but not tokenized).
        Field languageField = new StringField(FieldNames.LANGUAGE, content.getLanguage(), Field.Store.YES);
        doc.add(languageField);

        // Contents = textfield (indexed and tokenized, the content is not stored, no term vectors)
        Field contentsField = new Field(FieldNames.CONTENTS, ContentExtractor.extractContent(content.getFilePath()), CONTENT_FIELD_TYPE);
        //TextField contentsField = new TextField(FieldNames.CONTENTS, ContentExtractor.extractContent(content.getFilePath()), Field.Store.NO);
        doc.add(contentsField);

        writer.updateDocument(new Term(FieldNames.PATH, content.getFilePath().toString()),  doc);
    }

    /**
     * Delete document from index.
     *
     * @param writer  {@link org.apache.lucene.index.IndexWriter}
     * @param content {@link Content}
     * @throws IOException in case of filesystem error
     */
    private void deleteDocument(final IndexWriter writer, Content content) throws IOException {
        writer.deleteDocuments(new Term(FieldNames.PATH, content.getFilePath().toString()));
    }

}
