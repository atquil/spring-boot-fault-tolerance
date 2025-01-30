package com.atquil.springbootfaulttolerance.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author atquil
 */
@Slf4j
@Service
public class ImperativeStyleRetryServiceUsingRestTemplate {


    // Manually create RetryTemplate instead of injecting it
    private final RetryTemplate retryTemplate;

    public ImperativeStyleRetryServiceUsingRestTemplate() {
        this.retryTemplate = new RetryTemplate();

        // Configure retry policy
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        // Configure backoff policy
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(1000); // 1-second delay
        retryTemplate.setBackOffPolicy(backOffPolicy);
    }

    public String retryUsingRestTemplate() {
        return retryTemplate.execute(context -> {
            System.out.println("Imperative: Calling external API...");
            throw new RuntimeException("API call failed!"); // Simulate failure
        }, context -> {
            System.out.println("Imperative: All retries failed. Fallback method called.");
            return "Imperative Fallback response";
        });
    }

}
