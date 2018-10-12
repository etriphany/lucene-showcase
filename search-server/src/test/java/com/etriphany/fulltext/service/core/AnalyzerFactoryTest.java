package com.etriphany.fulltext.service.core;

import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import org.apache.lucene.analysis.Analyzer;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

/**
 * Tester for {@link AnalyzerFactory}.
 *
 * @author cadu.goncalves
 *
 */
public class AnalyzerFactoryTest {

    @Test
    public void checkMappings() throws Exception {
        for(String lang : AnalyzerFactory.getKnownLanguages()) {
            assertThat(AnalyzerFactory.getInstance(lang), is(instanceOf(Analyzer.class)));
        }
        // Invalid mapping fallback
        assertThat(AnalyzerFactory.getInstance("invalid"), is(instanceOf(Analyzer.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkIllegalMappings() throws Exception {
        AnalyzerFactory.getInstance("");
    }

    @Test
    public void checkLanguages() throws Exception {
        List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();
        for(LanguageProfile langProfile : languageProfiles) {
            assertThat(AnalyzerFactory.getKnownLanguages(), hasItem(langProfile.getLocale().toString().toLowerCase()));
        }
    }

}
