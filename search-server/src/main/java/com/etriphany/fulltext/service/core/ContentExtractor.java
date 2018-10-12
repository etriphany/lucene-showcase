package com.etriphany.fulltext.service.core;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Content extraction.
 *
 * @author cadu.goncalves
 *
 */
public final class ContentExtractor {

    public static final String PLAIN_TEXT = MimeTypes.PLAIN_TEXT;

    // Defines number of characters used for content sampling
    private static final int CONTENT_SAMPLING_CHARS = 7500;

    /**
     * Private constructor
     */
    private ContentExtractor() {

    }

    /**
     * Detect file MIME Type.
     *
     * @param path path {@link java.nio.file.Path}
     * @return File MIME Type
     * @throws java.io.IOException in case of filesystem error
     */
    public static String detectType(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            throw new IOException("Not a file " + path.toString());
        }
        Tika tika = new Tika();
        return tika.detect(path.toFile());
    }

    /**
     * Extract file contents.
     *
     * Uses Tika to support PDF, DOC, XLS, HXML, XML and many other formats
     *
     * @param path {@link java.nio.file.Path}
     * @return String with file contents
     * @throws IOException in case of filesystem error
     */
    public static String extractContent(Path path) throws IOException {
        Tika tika = new Tika();
        try (InputStream stream = Files.newInputStream(path)) {
            return tika.parseToString(stream);
        } catch (TikaException e) {
            throw new IOException(e);
        }
    }

    /**
     * Extract small sample from file contents.
     *
     * Uses Tika to support PDF, DOC, XLS, HXML, XML and many other formats
     *
     * @param path {@link java.nio.file.Path}
     * @return File sample
     */
    public static String extractSample(Path path) throws IOException {
        Tika tika = new Tika();
        try (InputStream stream = Files.newInputStream(path)) {
            return tika.parseToString(stream, new Metadata(), CONTENT_SAMPLING_CHARS);
        } catch (TikaException e) {
            throw new IOException(e);
        }
    }

}
