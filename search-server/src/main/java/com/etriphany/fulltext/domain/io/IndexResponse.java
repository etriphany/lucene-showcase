package com.etriphany.fulltext.domain.io;

import java.io.Serializable;

/**
 * Defines a response for content indexing.
 *
 * @author cadu.goncalves
 *
 */
public class IndexResponse implements Serializable {

    // Succeed or not
    private final Boolean success;

    // Result message
    private final String message;

    public IndexResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

}
