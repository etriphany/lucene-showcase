package com.etriphany.fulltext.domain.io;

/**
 * Provides deep pagination support.
 *
 * @author cadu.goncalves
 *
 */
public class DeepPage {

    // Parsing delimiter
    private static final String DELIMITER = ":";

    // Marked document
    private final Integer doc;

    // Marked score
    private final Float score;

    /**
     * Constructor.
     *
     * @param doc Marked document
     * @param score Marked score
     */
    public DeepPage(Integer doc, Float score) {
        this.doc = doc;
        this.score = score;
    }

    public Integer getDoc() {
        return doc;
    }

    public Float getScore() {
        return score;
    }

    @Override
    public String toString() {
        return doc + DELIMITER + score;
    }

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
