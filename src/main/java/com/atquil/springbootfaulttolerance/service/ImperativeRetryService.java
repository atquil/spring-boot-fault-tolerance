package com.atquil.springbootfaulttolerance.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

/**
 * @author atquil
 */
@Slf4j
@Service
public class ImperativeRetryService {


    @Autowired
    private RetryTemplate retryTemplate;

    public String performImperativeRetry(int id, String argument) {
        return retryTemplate.execute(context -> {
            // Retryable logic
            log.info("[ImperativeRetryService:performOperation ]Performing operation for id: {} and argument: {}. Retry count: {}", id, argument, context.getRetryCount());
            if (id == 1) {
                throw new IllegalArgumentException("Invalid ID!"); // Simulate failure
            }
            return "Operation successful for id: " + id + " and argument: " + argument;
        }, context -> {
            // Recovery logic
            log.info("[ImperativeRetryService:performOperation ]All retries failed. Recovery method called for id: {} and argument: {}", id, argument);
            return "Recovered from failure for id: " + id + " and argument: " + argument;
        });
    }
}
