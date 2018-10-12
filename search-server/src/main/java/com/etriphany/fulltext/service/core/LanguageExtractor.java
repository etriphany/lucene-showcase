package com.etriphany.fulltext.service.core;

import com.optimaize.langdetect.DetectedLanguage;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Language extraction.
 *
 * @author cadu.goncalves
 *
 */
@Component
public class LanguageExtractor {

    public static final String UNKNOWN_LANGUAGE = "unknown";

    // Language detector
    @Autowired
    private LanguageDetector languageDetector;

    /**
     * Detect file content language.
     *
     * @param path Path to file to analyze
     * @return Detected language.
     */
    public String detect(Path path) {
        if (Files.isReadable(path)) {
            try {
                return detect(ContentExtractor.extractSample(path));
            } catch (IOException e) {
                return UNKNOWN_LANGUAGE;
            }
        } else {
            return UNKNOWN_LANGUAGE;
        }
    }

    /**
     * Detect text language.
     *
     * @param text Content to analyze
     * @return Detected language.
     */
    public String detect(String text) {
        TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();

        TextObject textObject = textObjectFactory.forText(text);
        List<DetectedLanguage> list = languageDetector.getProbabilities(textObject);
        if (list.isEmpty()) {
            return UNKNOWN_LANGUAGE;
        } else {
            // Return the most probable one from the collection
            return list.get(0).getLocale().toString().toLowerCase();
        }
    }

}
