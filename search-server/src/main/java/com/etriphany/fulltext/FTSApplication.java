package com.etriphany.fulltext;

import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.util.List;

/**
 * Full text search application.
 *
 * @author cadu.goncalves
 *
 */
@SpringBootApplication
@EnableScheduling
@Configuration
public class FTSApplication {

    private static final Logger LOGGER = LogManager.getLogger(FTSApplication.class.getName());

    @Autowired
    private ApplicationContext context;

    @Value("${spring.profiles.active}")
    private String profile;

    public static void main(String[] args) {
        SpringApplication.run(FTSApplication.class, args);
    }

    /**
     * Factory for LanguageDetector, used on language detection
     */
    @Bean
    public LanguageDetector buildLanguageDetector() {
        LanguageDetector languageDetector = null;
        try {
            LOGGER.debug("Language detector bootstrap");
            List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();
            LanguageDetectorBuilder builder = LanguageDetectorBuilder.create(NgramExtractors.standard()).withProfiles(languageProfiles);
            languageDetector = builder.build();
            LOGGER.debug("Language detector loaded");
        } catch (IOException ioe) {
            LOGGER.error(ioe);
            ioe.printStackTrace();
            // Fatal
            SpringApplication.exit(context);
        }
        return languageDetector;
    }

    /**
     * Factory for TaskExecutor, used on indexing schedule thread pool.
     *
     * @return {@link org.springframework.core.task.TaskExecutor}
     */
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        if (profile.equals("serial-indexer")) {
            // One thread
            taskExecutor.setCorePoolSize(1);
            taskExecutor.setMaxPoolSize(1);
        } else {
            // Multiple threads
            taskExecutor.setCorePoolSize(10);
            taskExecutor.setMaxPoolSize(20);
        }
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }

}
