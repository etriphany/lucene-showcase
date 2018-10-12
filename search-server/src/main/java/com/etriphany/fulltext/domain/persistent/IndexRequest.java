package com.etriphany.fulltext.domain.persistent;

import com.etriphany.fulltext.domain.embed.Content;
import com.etriphany.fulltext.domain.embed.ContentOperation;
import com.etriphany.fulltext.domain.embed.IndexRequestStatus;

import java.io.Serializable;

/**
 * Defines a request for content indexing.
 *
 * @author cadu.goncalves
 *
 */
public final class IndexRequest implements Serializable {

    // Related content
    private Content content;

    // Index operation
    private ContentOperation operation;

    // Request processing status
    private IndexRequestStatus status;

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public ContentOperation getOperation() {
        return operation;
    }

    public void setOperation(ContentOperation operation) {
        this.operation = operation;
    }

    public IndexRequestStatus getStatus() {
        return status;
    }

    public void setStatus(IndexRequestStatus status) {
        this.status = status;
    }

    public Boolean isValid() {
        if(content == null || operation == null) {
            return false;
        } else {
            return content.isValid();
        }
    }

    @Override
    public String toString() {
        return String.format("IndexRequest:{ content = %s, operation = %s }", content == null ? "null" : content, operation);
    }
}
