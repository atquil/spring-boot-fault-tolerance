package com.atquil.springbootfaulttolerance.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author atquil
 */

@Slf4j
@Service
public class BasicRetryService {

    // ----------------   Basic Retry  --------------------------

    // Retry configuration: Retry on RuntimeException, max 3 attempts, 1-second delay
    @Retryable(
            retryFor = {RuntimeException.class}, // Retry on RuntimeException
            maxAttempts = 3,                 // Max retry attempts
            backoff = @Backoff(delay = 1000) // 1-second delay between retries
    )
    public String basicRetry() {
        log.info("[BasicRetryService:basicRetry] Retry Number:{} ",Objects.requireNonNull(RetrySynchronizationManager.getContext()).getRetryCount());
        throw new RuntimeException("API call failed!"); // Simulate failure
    }



    // ---------------- Basic Retry With Recover --------------------------


    // Retry configuration: Retry on ArithmeticException, max 3 attempts, 1-second delay
    @Retryable(
            retryFor = {ArithmeticException.class}, // Retry on ArithmeticException
            maxAttempts = 3,                 // Max retry attempts
            backoff = @Backoff(delay = 1000) // 1-second delay between retries
    )
    public String basicRetryWithRecover() {
        log.info("[BasicRetryService:basicRetryWithRecover] Retry Number:{} ",Objects.requireNonNull(RetrySynchronizationManager.getContext()).getRetryCount());
        throw new ArithmeticException("Divide by zero!"); // Simulate failure
    }


    // Fallback method: Called when all retries fail
    @Recover
    public String recover(ArithmeticException e) {
        System.out.println("All retries failed. Fallback method called.");
        return "Fallback response";
    }


    // ---------------- Basic Retry With Recover Using Arguments --------------------------


    // Retry configuration: Retry on ArithmeticException, max 4 attempts, 1-second delay
    @Retryable(
            retryFor = {ArithmeticException.class}, // Retry on ArithmeticException
            maxAttempts = 4,                 // Max retry attempts
            backoff = @Backoff(delay = 1000) // 1-second delay between retries
    )
    public String basicRetryWithRecoveryUsingArguments(int id, String argument) {
        System.out.println("Id:{} "+id+" Argument:{}"+argument);
        log.info("[BasicRetryService:basicRetryWithRecoveryUsingArguments] Retry Number:{} ",Objects.requireNonNull(RetrySynchronizationManager.getContext()).getRetryCount());
        throw new ArithmeticException("Divide by zero!"); // Simulate failure

    }

    // Recover with Explicit Exception
    @Recover
    public String recoverForBasicRetryWithRecoveryUsingArguments(ArithmeticException e,int id,String argument) {
        System.out.println("All retries failed. Fallback method called.");
        return "Fallback response"+"id:"+id+" Argument:"+argument;
    }



    // ---------------- Call Specific Recover Method:"recoverExample"--------------------------

    // Retry configuration: Retry on ArithmeticException, max 4 attempts, 1-second delay
    @Retryable(
            retryFor = {ArithmeticException.class}, // Retry on ArithmeticException
            maxAttempts = 4,                 // Max retry attempts
            backoff = @Backoff(delay = 1000), // 1-second delay between retries
            recover = "recoverExample"
    )
    public String basicRetryWithSpecificRecover(int id, String argument) {
        System.out.println("Id:{} "+id+" Argument:{}"+argument);
        log.info("[BasicRetryService:basicRetryWithSpecificRecover] Retry Number:{} ",Objects.requireNonNull(RetrySynchronizationManager.getContext()).getRetryCount());
        throw new ArithmeticException("Divide by zero!"); // Simulate failure

    }

    // Recover with Explicit Exception
    @Recover
    public String recoverExample(ArithmeticException e,int id,String argument) {
        System.out.println("All retries failed. Fallback method called.");
        return "Recover Using User Defined Method"+"id:"+id+" Argument:"+argument;
    }
}
