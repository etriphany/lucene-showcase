package com.etriphany.fulltext.domain.embed;

/**
 * Defines the status of an index request inside the queue.
 *
 * @author cadu.goncalves
 *
 */
public enum IndexRequestStatus {
    QUEUED,
    LOCKED,
    CONSUMED
}
