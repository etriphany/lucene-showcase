package com.etriphany.fulltext.persistence;

import com.etriphany.fulltext.domain.embed.Content;
import com.etriphany.fulltext.domain.embed.ContentOperation;
import com.etriphany.fulltext.domain.persistent.IndexRequest;
import com.etriphany.fulltext.domain.embed.IndexRequestStatus;
import com.etriphany.fulltext.domain.util.FieldNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Control state of index request queue.
 *
 * <p>Queue data are stored using embedded database. The column "queued_at" is used to track position.</p>
 *
 * @author cadu.goncalves
 *
 */
@Repository
public class IndexRequestRepository {

    public static final Integer PAGE_SIZE = 50;

    private static final Logger LOGGER = LogManager.getLogger(IndexRequestRepository.class.getName());

    @Autowired
    private JdbcTemplate jdbc;

    /**
     * Add a new request to the queue.
     *
     * @param request {@link IndexRequest}
     * @throws PersistenceException in case of storage error
     */
    public void insert(final IndexRequest request) throws PersistenceException {
        final String sql = "INSERT INTO index_queue (id, path, operation) VALUES(?, ?, ?)";
        try {
            jdbc.update(sql, request.getContent().getId(), request.getContent().getPath(), request.getOperation().toString());
        } catch (DuplicateKeyException dke) {
            // Ignore
            LOGGER.debug("Ingore duplicated request " + request.toString());
        } catch (DataAccessException dae) {
            throw new PersistenceException(dae);
        }
    }

    /**
     * Remove all request that match the status.
     *
     * @param currentStatus {@link IndexRequestStatus}
     * @throws PersistenceException in case of storage error
     */
    public void remove(IndexRequestStatus currentStatus) throws PersistenceException {
        final String sql = "DELETE FROM index_queue WHERE status = ?";
        try {
            jdbc.update(sql, currentStatus.toString());
        } catch (DataAccessException dae) {
            throw new PersistenceException(dae);
        }
    }

    /**
     * Remove a request.
     *
     * @param request {@link IndexRequest}
     * @throws PersistenceException in case of storage error
     */
    public void remove(IndexRequest request) throws PersistenceException {
        final String sql = "DELETE FROM index_queue WHERE id = ? AND path = ? AND operation = ?";
        try {
            jdbc.update(sql, request.getContent().getId(), request.getContent().getPath(), request.getOperation().toString());
        } catch (DataAccessException dae) {
            throw new PersistenceException(dae);
        }
    }

    /**
     * Recover single queued request.
     *
     * @return {@link IndexRequest}
     * @throws PersistenceException in case of storage error
     */
    public IndexRequest getNext() throws PersistenceException {
        IndexRequest result = null;

        final String sql = "SELECT id, path, operation FROM index_queue WHERE status = 'QUEUED' ORDER BY queued_at ASC LIMIT 1";
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(sql);

            for (Map row : rows) {
                Content content = new Content();
                content.setId((String) row.get(FieldNames.ID));
                content.setPath((String) row.get(FieldNames.PATH));

                result = new IndexRequest();
                result.setContent(content);
                result.setOperation(ContentOperation.valueOf((String) row.get(FieldNames.OPERATION)));
            }
        }  catch (EmptyResultDataAccessException eds) {
            return null;
        } catch (DataAccessException dae) {
            throw new PersistenceException(dae);
        }

        return result;
    }

    /**
     * Change state of all requests that match the currentState.
     *
     * @param currentStatus {@link IndexRequestStatus}
     * @param newStatus     {@link IndexRequestStatus}
     * @throws PersistenceException in case of storage error
     */
    public void changeStatus(IndexRequestStatus currentStatus, IndexRequestStatus newStatus) throws PersistenceException {
        final String sql = "UPDATE index_queue SET status = ? WHERE status = ?";
        try {
            jdbc.update(sql, newStatus.toString(), currentStatus.toString());
        } catch (DataAccessException dae) {
            throw new PersistenceException(dae);
        }
    }

    /**
     * Change state of a particular requests.
     *
     * @param request   {@link IndexRequest}
     * @param newStatus {@link IndexRequestStatus}
     * @throws PersistenceException in case of storage error
     */
    public void changeStatus(final IndexRequest request, IndexRequestStatus newStatus) throws PersistenceException {
        final String sql = "UPDATE index_queue SET status = ? WHERE id = ? AND path = ?";
        try {
            jdbc.update(sql, newStatus.toString(), request.getContent().getId(), request.getContent().getPath());
        } catch (DataAccessException dae) {
            throw new PersistenceException(dae);
        }
    }

    /**
     * Count all request in particular state.
     *
     * @param status {@link IndexRequestStatus}
     * @throws PersistenceException in case of storage error
     */
    public Integer countInStatus(IndexRequestStatus status) throws PersistenceException {
        final String sql = "SELECT COUNT(*) FROM index_queue WHERE status = ?";
        try {
            return jdbc.queryForObject(sql, Integer.class, status.toString());
        } catch (DataAccessException dae) {
            throw new PersistenceException(dae);
        }
    }

    /**
     * Recover a page of requests locked in the queue.
     *
     * @param page The page to limit the result set (0...n)
     * @return {@link java.util.List}
     * @throws PersistenceException in case of storage error
     */
    public List<IndexRequest> listLocked(Integer page) throws PersistenceException {
        List<IndexRequest> result = new ArrayList<>();

        final String sql = "SELECT * FROM index_queue WHERE status = 'LOCKED' ORDER BY queued_at ASC OFFSET ? LIMIT ?";
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(sql, (page * PAGE_SIZE), PAGE_SIZE);
            for (Map row : rows) {
                Content content = new Content();
                content.setId((String) row.get(FieldNames.ID));
                content.setPath((String) row.get(FieldNames.PATH));

                IndexRequest request = new IndexRequest();
                request.setContent(content);
                request.setOperation(ContentOperation.valueOf((String) row.get(FieldNames.OPERATION)));
                result.add(request);
            }
        } catch (DataAccessException dae) {
            throw new PersistenceException(dae);
        }

        return result;
    }
}
