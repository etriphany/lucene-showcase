package com.etriphany.fulltext.component.schedule;

import com.etriphany.fulltext.domain.persistent.IndexRequest;
import com.etriphany.fulltext.domain.IndexingException;
import com.etriphany.fulltext.service.IndexService;
import com.etriphany.fulltext.service.QueueService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Content indexer which following behaviours.
 * <ul>
 * <li>Uses a cumulative queue consume approach</li>
 * <li>A block of indexing requests is processed in each running cycle</li>
 * <li>A single thread will process many requests (overlap is forbidden).</li>
 * </ul>
 *
 * @author cadu.goncalves
 *
 */
@Component
@Profile("serial-indexer")
public class Serialndexer {

    private static final Logger LOGGER = LogManager.getLogger(Serialndexer.class.getName());

    @Autowired
    private IndexService indexService;

    @Autowired
    private QueueService queueService;

    // In memory running control to avoid overlaps
    private transient boolean busy;

    /**
     * Task execution endpoint
     */
    @Scheduled(fixedRateString = "${fulltext.indexer.serial.rate}")
    public void runTask() {
        // Avoid overlapping
        if (busy) {
            LOGGER.debug("Busy now, retry latter");
            return;
        } else {
            busy = true;
        }

        consumeIndexRequests();

        busy = false;
    }

    /**
     * Consume index requests from queue.
     */
    private void consumeIndexRequests() {
        try {
            // Delimit the input with a lock state
            queueService.lockAll();

            // Compute basic paging stuff
            Integer total = queueService.countLockedRequests();
            Integer pages = queueService.computePages(total);

            // Process each request
            for (int page = 0; page < pages; ++page) {
                List<IndexRequest> requests = queueService.getLockedRequests(page);

                for (IndexRequest request : requests) {
                    try {
                        // Index contents
                        indexService.process(request);
                        // Mark as consumed
                        queueService.consume(request);
                    } catch (IndexingException e) {
                        LOGGER.error(e);
                    }
                }
            }

            // Purge consumed when done
            queueService.purgeConsumed();

            // Flush anything in memory
            indexService.flush();
        } catch (Exception e) {
            LOGGER.error(e);
            e.printStackTrace();
        }
    }
}
