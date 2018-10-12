package com.etriphany.fulltext.service;

import com.etriphany.fulltext.domain.persistent.IndexRequest;
import com.etriphany.fulltext.domain.embed.IndexRequestStatus;
import com.etriphany.fulltext.persistence.IndexRequestRepository;
import com.etriphany.fulltext.persistence.PersistenceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Content indexing queue service.
 *
 * @author cadu.goncalves
 *
 */
@Service
public class QueueService {

    private static final Logger LOGGER = LogManager.getLogger(QueueService.class.getName());

    @Autowired
    private IndexRequestRepository repository;

    /**
     * Add a new request to the queue.
     *
     * @param request {@link IndexRequest}
     */
    @Transactional
    public void add(final IndexRequest request) throws IllegalArgumentException {
        LOGGER.debug("Queuing request " + request.toString());
        if (request.isValid()) {
            try {
                repository.insert(request);
            } catch (PersistenceException pse) {
                LOGGER.error(pse);
                pse.printStackTrace();
            }
        } else {
            LOGGER.error("Invalid IndexRequest object");
            throw new IllegalArgumentException();
        }
    }

    /**
     * Consume a particular request from the queue.
     *
     * @param request {@link IndexRequest}
     */
    @Transactional
    public void consume(IndexRequest request) {
        LOGGER.debug("Consuming request " + request.toString());
        try {
            repository.changeStatus(request, IndexRequestStatus.CONSUMED);
        } catch (PersistenceException pse) {
            LOGGER.error(pse);
            pse.printStackTrace();
        }
    }

    /**
     * Purge a particular request from queue.
     *
     * @param request {@link IndexRequest}
     */
    @Transactional
    public void purge(IndexRequest request) {
        LOGGER.debug("Purging request " + request.toString());
        try {
            repository.remove(request);
        } catch (PersistenceException pse) {
            LOGGER.error(pse);
            pse.printStackTrace();
        }
    }

    /**
     * Lock all queued requests.
     */
    @Transactional
    public void lockAll() {
        LOGGER.debug("Locking all queued requests");
        try {
            repository.changeStatus(IndexRequestStatus.QUEUED, IndexRequestStatus.LOCKED);
        } catch (PersistenceException pse) {
            LOGGER.error(pse);
            pse.printStackTrace();
        }
    }

    /**
     * Remove all consumed requests.
     */
    @Transactional
    public void purgeConsumed() {
        LOGGER.debug("Purging consumed requests");
        try {
            repository.remove(IndexRequestStatus.CONSUMED);
        } catch (PersistenceException pse) {
            LOGGER.error(pse);
            pse.printStackTrace();
        }
    }

    /**
     * Recover a request from the queue, changing its status to consumed.
     *
     * @return {@link IndexRequest}
     */
    @Transactional
    public IndexRequest getSingleAndLock() {
        try {
            IndexRequest request = repository.getNext();
            if(request != null) {
                repository.changeStatus(request, IndexRequestStatus.LOCKED);
            }
            return request;
        } catch (PersistenceException pse) {
            LOGGER.error(pse);
            pse.printStackTrace();
            return null;
        }
    }

    /**
     * Count all valid requests.
     */
    public Integer countLockedRequests() {
        int locked = 0;
        try {
            locked = repository.countInStatus(IndexRequestStatus.LOCKED);
        } catch (PersistenceException pse) {
            LOGGER.error(pse);
            pse.printStackTrace();
        }
        LOGGER.debug("Total of locked requests = " + locked);
        return locked;
    }

    /**
     * Recover a page of requests locked in the queue.
     *
     * @param page The page return (0...n)
     * @return {@link java.util.List}
     */
    public List<IndexRequest> getLockedRequests(Integer page) {
        try {
            return repository.listLocked(page);
        } catch (PersistenceException pse) {
            LOGGER.error(pse);
            pse.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Compute the number of pages.
     *
     * @param total Total of elements
     * @return Total of pages
     */
    public Integer computePages(Integer total) {
        Integer pages = total / IndexRequestRepository.PAGE_SIZE;

        if (total % IndexRequestRepository.PAGE_SIZE != 0) {
            pages = pages + 1;
        }

        return pages;
    }
}
