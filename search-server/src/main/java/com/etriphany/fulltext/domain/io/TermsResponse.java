package com.etriphany.fulltext.domain.io;

import java.util.SortedSet;

/**
 * Defines a response for content term vector.
 *
 * @author cadu.goncalves
 *
 */
public class TermsResponse {

    // Matches
    private final SortedSet<ContentTerm> matches;

    public TermsResponse(SortedSet<ContentTerm> matches) {
        this.matches = matches;
    }

    public SortedSet<ContentTerm> getMatches() {
        return matches;
    }
}
