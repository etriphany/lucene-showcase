package com.etriphany.fulltext.domain.io;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.SortedSet;

/**
 * Defines a response for content term vector.
 *
 * @author cadu.goncalves
 *
 */
@Value
@AllArgsConstructor
public class TermsResponse {

    // Matches
    private final SortedSet<ContentTerm> matches;

}
