package com.etriphany.fulltext.control;

import com.etriphany.fulltext.domain.persistent.IndexRequest;
import com.etriphany.fulltext.domain.io.IndexResponse;
import com.etriphany.fulltext.service.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;

/**
 * Content indexing queue controller.
 *
 * @author cadu.goncalves
 *
 */
@RestController
public class QueueController {

    @Autowired
    private QueueService queueService;

    /**
     * Queue content indexing request (add, update or delete).
     *
     * @param request {@link IndexRequest}
     * @return {@link IndexResponse}
     */
    @RequestMapping(value = "/queue", method = RequestMethod.POST)
    public Callable<IndexResponse> queue(@RequestBody final IndexRequest request) {
        return () -> {
            try {
                queueService.add(request);
                return new IndexResponse(true, "OK");
            } catch (IllegalArgumentException e) {
                return new IndexResponse(false, "Invalid request");
            }
        };
    }
}
