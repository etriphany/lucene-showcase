package com.etriphany.fulltext.domain.io;

import lombok.Data;

/**
 * Defines a request for content term vector.
 *
 * @author cadu.goncalves
 *
 */
@Data
public class TermsRequest {

    // Content path
    private String path;

}
