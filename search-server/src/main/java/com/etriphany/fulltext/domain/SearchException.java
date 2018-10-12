package com.etriphany.fulltext.domain;

/**
 * Exception occurred during content search.
 *
 * @author cadu.goncalves
 *
 */
public class SearchException extends Exception {

    private final ErrorType type;

    public SearchException(ErrorType type) {
        this.type = type;
    }

    public SearchException(ErrorType type, Throwable cause) {
        super(cause);
        this.type = type;
    }

    public ErrorType getType() {
        return type;
    }

    public enum ErrorType {
        NO_IDEX,
        QUERY_PARSE_FAILURE,
        INPUT_OUTPUT_FAILURE
    }
}
