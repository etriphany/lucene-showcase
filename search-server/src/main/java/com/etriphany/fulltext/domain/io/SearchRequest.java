package com.etriphany.fulltext.domain.io;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * Defines a request for content search.
 *
 * @author cadu.goncalves
 *
 */
@Data
public class SearchRequest implements Serializable {

    // Search query
    private String query;

    // Detect language from query
    private boolean detectLanguage = Boolean.TRUE;

    // Language filtering
    private Set<String> languages;

    // Mark for deep paging.
    private String deep;

    public Boolean isValid() {
        return query != null && !query.isEmpty();
    }

}
