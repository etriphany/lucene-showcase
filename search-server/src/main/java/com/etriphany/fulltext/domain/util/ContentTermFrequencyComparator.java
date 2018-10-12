package com.etriphany.fulltext.domain.util;

import com.etriphany.fulltext.domain.io.ContentTerm;

import java.util.Comparator;

/**
 * Compare content term based on frequency.
 *
 * @author cadu.goncalves
 *
 */
public class ContentTermFrequencyComparator implements Comparator<ContentTerm> {

    @Override
    public int compare(ContentTerm term1, ContentTerm term2) {
        // Reverse order
        if (term1.getFreq() > term2.getFreq()) {
            return -1;
        } else if (term1.getFreq() < term2.getFreq()) {
            return 1;
        } else {
            return 0;
        }
    }
}
