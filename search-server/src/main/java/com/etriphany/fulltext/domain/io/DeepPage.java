package com.etriphany.fulltext.domain.io;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Provides deep pagination support.
 *
 * @author cadu.goncalves
 *
 */
@Value
@AllArgsConstructor
public class DeepPage {

    // Parsing delimiter
    private static final String DELIMITER = ":";

    // Marked document
    private final Integer doc;

    // Marked score
    private final Float score;

    /**
     * Reconstruct instance from string representation.
     *
     * @param value String value
     * @return {@link DeepPage}
     */
    public static DeepPage fromString(String value) {
        if(value == null) {
            return null;
        }
        String[] parts = value.split(DELIMITER);
        try {
            return new DeepPage(Integer.valueOf(parts[0]), Float.valueOf(parts[1]));
        }catch(NumberFormatException nfe) {
            return null;
        }
    }
}
