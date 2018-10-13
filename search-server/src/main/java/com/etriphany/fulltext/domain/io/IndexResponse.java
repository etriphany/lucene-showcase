package com.etriphany.fulltext.domain.io;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.Serializable;

/**
 * Defines a response for content indexing.
 *
 * @author cadu.goncalves
 *
 */
@Value
@AllArgsConstructor
public class IndexResponse implements Serializable {

    // Succeed or not
    private final Boolean success;

    // Result message
    private final String message;

}
