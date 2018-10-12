package com.etriphany.fulltext.domain.io;

import com.etriphany.fulltext.domain.embed.Content;
import com.etriphany.fulltext.domain.util.ContentSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.List;

/**
 * Defines a response for content search.
 *
 * @author cadu.goncalves
 *
 */
public class SearchResponse implements Serializable {

    // Matches
    private final List<Content> matches;

    // Total of results to request more pages
    private final Integer total;

    // Register the paging status to navigate over more pages.
    private final String deep;

    public SearchResponse(List<Content> matches, Integer total, String deep) {
        this.matches = matches;
        this.total = total;
        this.deep = deep;
    }

    @JsonSerialize(contentUsing=ContentSerializer.class)
    public List<Content> getMatches() {
        return matches;
    }

    public Integer getTotal() {
        return total;
    }

    public String getDeep() {
        return deep;
    }
}
