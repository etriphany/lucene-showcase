package com.etriphany.fulltext.domain.io;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Defines a content term.
 *
 * @author cadu.goncalves
 *
 */
@Value
@AllArgsConstructor
public class ContentTerm implements Comparable<ContentTerm> {

    // Term value
    private final String text;

    // Term frequency
    private final Integer freq;

    @Override
    public int compareTo(ContentTerm other) {
        int compareTxt = this.text.compareTo(other.getText());
        int compareFreq = Integer.compare(this.freq, other.getFreq());
        return compareTxt + compareFreq;
    }

}
