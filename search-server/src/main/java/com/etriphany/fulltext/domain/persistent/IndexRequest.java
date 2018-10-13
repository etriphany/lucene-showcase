package com.etriphany.fulltext.domain.persistent;

import com.etriphany.fulltext.domain.embed.Content;
import com.etriphany.fulltext.domain.embed.ContentOperation;
import com.etriphany.fulltext.domain.embed.IndexRequestStatus;
import lombok.*;

import java.io.Serializable;

/**
 * Defines a request for content indexing.
 *
 * @author cadu.goncalves
 *
 */
@Data
public final class IndexRequest implements Serializable {

    // Related content
    private Content content;

    // Index operation
    private ContentOperation operation;

    // Request processing status
    private IndexRequestStatus status;

    public Boolean isValid() {
        if(content == null || operation == null) {
            return false;
        } else {
            return content.isValid();
        }
    }

}
