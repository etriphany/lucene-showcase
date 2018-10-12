package com.etriphany.fulltext.domain.io;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * Defines a request for content search.
 *
 * @author cadu.goncalves
 *
 */
public class SearchRequest implements Serializable {

    // Search query
    private String query;

    // Detect language from query
    private Boolean detectLanguage = Boolean.TRUE;

    // Language filtering
    private Set<String> languages;

    // Mark for deep paging.
    private String deep;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getDeep() {
        return deep;
    }

    public void setDeep(String deep) {
        this.deep = deep;
    }

    public Set<String> getLanguages() {
        if (languages == null) {
            languages = Collections.emptySet();
        }
        return languages;
    }

    public void setLanguages(Set<String> languages) {
        this.languages = languages;
    }

    public Boolean isDetectLanguage() {
        return detectLanguage;
    }

    public void setDetectLanguage(Boolean detectLanguage) {
        this.detectLanguage = detectLanguage;
    }

    public Boolean isValid() {
        return query != null && !query.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("SearchRequest:{ query = %s }", query == null ? "null" : query);
    }
}
