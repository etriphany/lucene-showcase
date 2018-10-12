package com.etriphany.fulltext.control;

import com.etriphany.fulltext.domain.io.SearchRequest;
import com.etriphany.fulltext.domain.io.SearchResponse;
import com.etriphany.fulltext.domain.SearchException;
import com.etriphany.fulltext.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Content search controller.
 *
 * @author cadu.goncalves
 *
 */
@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * Search contents.
     *
     * @param request {@link SearchRequest}
     * @return {@link SearchResponse}
     */
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public Callable<SearchResponse> search(@RequestBody final SearchRequest request) {
        return () -> {
            try {
                return searchService.search(request);
            } catch (IllegalArgumentException | SearchException e) {
                return new SearchResponse(new ArrayList<>(), 0, "");
            }
        };
    }

}
