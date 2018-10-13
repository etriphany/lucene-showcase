package com.etriphany.fulltext.domain.io;

import com.etriphany.fulltext.domain.embed.Content;
import com.etriphany.fulltext.domain.util.ContentSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.util.List;

/**
 * Defines a response for content search.
 *
 * @author cadu.goncalves
 *
 */
@Value
@AllArgsConstructor
public class SearchResponse implements Serializable {

    // Matches
    @JsonSerialize(contentUsing=ContentSerializer.class)
    private final List<Content> matches;

    // Total of results to request more pages
    private final Integer total;

    // Register the paging status to navigate over more pages.
    private final String deep;

}
