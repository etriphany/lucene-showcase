package com.etriphany.fulltext.component.schedule;

import com.etriphany.fulltext.domain.persistent.IndexRequest;
import com.etriphany.fulltext.service.IndexService;
import com.etriphany.fulltext.service.QueueService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Content indexer which following behaviours.
 * <ul>
 * <li>Uses a non-cumulative queue consume approach</li>
 * <li>A single indexing request is consumed in each running cycle</li>
 * <li>Many threads can consume requests in parallel (overlap is good).</li>
 * </ul>
 *
 * @author cadu.goncalves
 *
 */
@Component
@Profile("parallel-indexer")
public class ParallelIndexer {

    private static final Logger LOGGER = LogManager.getLogger(ParallelIndexer.class.getName());

    @Autowired
    private IndexService indexService;

    @Autowired
    private QueueService queueService;

    /**
     * Task execution endpoint
     */
    @Scheduled(fixedRateString = "${fulltext.indexer.parallel.rate}")
    public void runTask() {
        consumeIndexRequest();
    }

    /**
     * Consume index requests from queue.
     */
    private void consumeIndexRequest() {
        try {
            // Recover next request
            IndexRequest request = queueService.getSingleAndLock();
            if (request != null) {
                // Index contents
                indexService.process(request);
                // Purge request
                queueService.purge(request);
                // Flush anything in memory
                indexService.flush();
            }
        } catch (Exception e) {
            LOGGER.error(e);
            e.printStackTrace();
        }
    }
}
