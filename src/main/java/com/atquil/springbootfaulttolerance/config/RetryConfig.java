package com.atquil.springbootfaulttolerance.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Collections;

/**
 * @author atquil
 */


@Configuration
@EnableRetry
@Slf4j
public class RetryConfig {

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        // SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(); If for global level retry.

        // Retry
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
                5, // Max Attempt is 5
                Collections.singletonMap(ArithmeticException.class,true)
        );

        // Strategy for Retry
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(2000);
        backOffPolicy.setMultiplier(2);
        backOffPolicy.setMaxInterval(5000);

        //For for retryTemplate set both of them
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;

    }
}
