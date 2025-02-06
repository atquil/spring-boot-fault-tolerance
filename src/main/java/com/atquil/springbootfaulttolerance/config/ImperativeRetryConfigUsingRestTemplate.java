package com.atquil.springbootfaulttolerance.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Collections;

/**
 * @author atquil
 */


@Configuration
@Slf4j
public class ImperativeRetryConfigUsingRestTemplate {

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Retry Policy: Retry up to 5 times for IllegalArgumentException
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
                5, // Max retry attempts
                Collections.singletonMap(IllegalArgumentException.class, true) // Retry on IllegalArgumentException
        );

        // Backoff Strategy: Exponential backoff with initial delay of 1 second and max delay of 7 seconds
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // Initial delay: 1 second
        backOffPolicy.setMultiplier(2); // Multiplier for exponential backoff
        backOffPolicy.setMaxInterval(7000); // Maximum delay: 7 seconds

        // Set retry policy and backoff policy
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;

    }
}
