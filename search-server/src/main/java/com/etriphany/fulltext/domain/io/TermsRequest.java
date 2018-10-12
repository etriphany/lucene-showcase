package com.etriphany.fulltext.domain.io;

/**
 * Defines a request for content term vector.
 *
 * @author cadu.goncalves
 *
 */
public class TermsRequest {

    // Content path
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
