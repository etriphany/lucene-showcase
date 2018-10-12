package com.etriphany.fulltext.service.core;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

/**
 * Factory for {@link org.apache.lucene.analysis.Analyzer} that matches a particular language.
 *
 * @author cadu.goncalves
 *
 */
public final class AnalyzerFactory {

    // Lucene base package for analyzers
    private static final String BASE = "org.apache.lucene.analysis.";

    // Canonical name for Lucene standard analyzer
    private static final String STANDARD = BASE + "standard.StandardAnalyzer";

    private static Map<String, String> langMap;

    /**
     * Private constructor.
     */
    private AnalyzerFactory() {

    }

    static {
        // All languages supported by language detector (https://github.com/optimaize/language-detector)
        // Language codes reference https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes

        langMap = Map.ofEntries(
            entry("af", STANDARD), // Afrikaans
            entry("an", STANDARD), // Aragonese
            entry("ar", BASE + "ar.ArabicAnalyzer"),
            entry("ast", STANDARD), // Asturian
            entry("be", STANDARD), // Belarusian
            entry("br", STANDARD), // Breton
            entry("ca", BASE + "ca.CatalanAnalyzer"),
            entry("bg", BASE + "bg.BulgarianAnalyzer"),
            entry("bn", STANDARD), // Bengali
            entry("cs", BASE + "cz.CzechAnalyzer"), // (cs -> cz)
            entry("cy", STANDARD), // Welsh
            entry("da", BASE + "da.DanishAnalyzer"),
            entry("de", BASE + "de.GermanAnalyzer"),
            entry("el", BASE + "el.GreekAnalyzer"),
            entry("en", BASE + "en.EnglishAnalyzer"),
            entry("es", BASE + "es.SpanishAnalyzer"),
            entry("et", STANDARD), // Estonian
            entry("eu", BASE + "eu.BasqueAnalyzer"),
            entry("fa", BASE + "fa.PersianAnalyzer"),
            entry("fi", BASE + "fi.FinnishAnalyzer"),
            entry("fr", BASE + "fr.FrenchAnalyzer"),
            entry("ga", BASE + "ga.IrishAnalyzer"),
            entry("gl", BASE + "gl.GalicianAnalyzer"),
            entry("gu", STANDARD), // Gujarati
            entry("he", STANDARD), // Hebrew
            entry("hi", STANDARD), // Hindi
            entry("hr", STANDARD),  // Croatian
            entry("ht", STANDARD), // Haitian
            entry("hu", BASE + "hu.HungarianAnalyzer"),
            entry("id", BASE + "id.IndonesianAnalyzer"),
            entry("is", STANDARD), // Icelandic
            entry("it", BASE + "it.ItalianAnalyzer"),
            entry("ja", BASE + "ja.JapaneseAnalyzer"),
            entry("km", STANDARD), // Khmer
            entry("kn", STANDARD), // Kannada
            entry("ko", STANDARD), // Korean
            entry("lt", BASE + "lt.LithuanianAnalyzer"),
            entry("lv", BASE + "lv.LativianAnalyzer"),
            entry("mk", STANDARD), // Macedonian
            entry("ml", STANDARD), // Malayalam
            entry("mr", STANDARD), // Marathi
            entry("ms", STANDARD), // Malay
            entry("mt", STANDARD), // Maltese
            entry("ne", STANDARD), // Nepali
            entry("nl", BASE + "nl.DutchAnalyzer"),
            entry("no", BASE + "no.NorwegianAnalyzer"),
            entry("oc", STANDARD), // Occitan
            entry("pa", STANDARD), // Panjabi
            entry("pl", STANDARD), // Polish
            entry("pt", BASE + "pt.PortugueseAnalyzer"),
            entry("ro", BASE + "ro.RomanianAnalyzer"),
            entry("ru", BASE + "ru.RussianAnalyzer"),
            entry("sk", STANDARD), // Slovak
            entry("sl", STANDARD), // Slovene
            entry("so", STANDARD), // Somali
            entry("sq", STANDARD), // Albanian
            entry("sr", STANDARD), // Serbian (SerbianNormalizationFilter?)
            entry("sv", BASE + "sv.SwedishAnalyzer"),
            entry("sw", STANDARD), // Swahili
            entry("ta", STANDARD), // Tamil
            entry("te", STANDARD), // Telugu
            entry("th", BASE + "th.ThaiAnalyzer"),
            entry("tl", STANDARD), // Tagalog
            entry("tr", BASE + "tr.TurkishAnalyzer"),
            entry("uk", STANDARD), // Ukrainian
            entry("ur", STANDARD), // Urdu
            entry("vi", STANDARD), // Vietnamese
            entry("yi", STANDARD), // Yiddish
            entry("zh-cn", STANDARD), // Simplified Chinese
            entry("zh-tw", STANDARD) // Traditional Chinese
        );
    }

    /**
     * Construct the proper {@link org.apache.lucene.analysis.Analyzer} for given language.
     *
     * @param isoLang Language ISO code 2 letters
     * @return {@link org.apache.lucene.analysis.Analyzer}
     * @throws IllegalArgumentException if isoLang is null or empty
     */
    public static Analyzer getInstance(String isoLang) {
        if(StringUtils.isEmpty(isoLang)) {
            throw new IllegalArgumentException();
        }

        String analyzerClassName = langMap.get(isoLang.toLowerCase());
        if(analyzerClassName == null) {
            // Fallback
            return new StandardAnalyzer();
        }

        try {
            Class<Analyzer> aClazz = (Class<Analyzer>) Class.forName(analyzerClassName);
            return aClazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            // Fallback
            return new StandardAnalyzer();
        }
    }

    /**
     * Recover all known languages.
     *
     * @return {@link java.util.Set}
     */
    public static Set<String> getKnownLanguages() {
        return langMap.keySet();
    }
}
