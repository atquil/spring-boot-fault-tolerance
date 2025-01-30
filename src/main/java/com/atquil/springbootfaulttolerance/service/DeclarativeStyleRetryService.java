package com.atquil.springbootfaulttolerance.service;



import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author atquil
 */

// Marks this class as a Spring-managed service bean
@Service
@Slf4j
public class DeclarativeStyleRetryService {
    private int attempt = 0;
//
//    @Retryable( // Annotation to enable retry logic on the method
//            retryFor = RuntimeException.class, //Specifies the type of exception to trigger a retry
//            maxAttempts = 3, // Maximum number of retry attempts
//            // Backoff: Retry strategy
//            backoff = @Backoff(
//                    delay = 5000, //Initial delay before the first retry (in milliseconds)
//                    maxDelay = 4, // Maximum delay between retries (corrected from 4 to a meaningful value)
//                    multiplier = 3, // Factor, by which the delay should be multiplied after each retry
//                    random = true // To add jitter between retries within range
//            )
//    )
//    public String callExternalService() {
//        attempt ++;
//        if(attempt < 3) {
//            log.info("Retry Number: {}", Objects.requireNonNull(RetrySynchronizationManager.getContext()).getRetryCount());
//            // When we get a runtime exception, it should reattempt for at-least 2 times
//            throw new RuntimeException("Failing");
//        }
//        System.out.println("Attempt " + attempt + ": Success");
//        return "Success";
//    }
//
//
//    @Retryable( // Annotation to enable retry logic on the method
//            retryFor = RuntimeException.class, //Specifies the type of exception to trigger a retry
//            maxAttempts = 3, // Maximum number of retry attempts
//            // Backoff: Retry strategy
//            backoff = @Backoff(
//                    delay = 5000, //Initial delay before the first retry (in milliseconds)
//                    maxDelay = 4, // Maximum delay between retries (corrected from 4 to a meaningful value)
//                    multiplier = 3, // Factor, by which the delay should be multiplied after each retry
//                    random = true // To add jitter between retries within range
//            )
//    )
//    public String callExternalServiceWithRecover() {
//        attempt ++;
//        if(attempt < 3) {
//            log.info("Retry Number: {}", Objects.requireNonNull(RetrySynchronizationManager.getContext()).getRetryCount());
//            // When we get a runtime exception, it should reattempt for at-least 2 times
//            throw new RuntimeException("Failing");
//        }
//        System.out.println("Attempt " + attempt + ": Success");
//        return "Success";
//    }
//
//
//

}
