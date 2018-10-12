package com.etriphany.fulltext.control;

import com.etriphany.fulltext.domain.SearchException;
import com.etriphany.fulltext.domain.io.TermsRequest;
import com.etriphany.fulltext.domain.io.TermsResponse;
import com.etriphany.fulltext.service.TermsService;
import com.etriphany.fulltext.domain.io.SearchRequest;
import com.etriphany.fulltext.domain.io.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;

/**
 * Content terms controller.
 *
 * @author cadu.goncalves
 *
 */
@RestController
public class TermsController {

    @Autowired
    private TermsService termsService;

    /**
     * Show document terms.
     *
     * @param request {@link SearchRequest}
     * @return {@link SearchResponse}
     */
    @RequestMapping(value = "/terms", method = RequestMethod.POST)
    public Callable<TermsResponse> search(@RequestBody final TermsRequest request) {
        return () -> {
            try {
                return termsService.listTerms(request);
            } catch (IllegalArgumentException | SearchException e) {
                return null;
            }
        };
    }
}
