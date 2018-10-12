package com.etriphany.fulltext.domain;

/**
 * Exception occurred during index write.
 *
 * @author cadu.goncalves
 *
 */
public final class IndexingException extends Exception {

    private final ErrorType type;

    public IndexingException(ErrorType type) {
        this.type = type;
    }

    public IndexingException(ErrorType type, Throwable cause) {
        super(cause);
        this.type = type;
    }

    public ErrorType getType() {
        return type;
    }

    public enum ErrorType {
        NULL_CONTENT,
        CONTENT_NOT_FILE,
        INPUT_OUTPUT_FAILURE
    }
}
