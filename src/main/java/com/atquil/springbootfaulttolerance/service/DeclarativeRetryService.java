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
public class DeclarativeRetryService {

    // ----------------- BASIC RETRY --------------------------
    /**
     * Demonstrates basic retry mechanism with 3 attempts and 1-second delay
     * @return Never returns successfully (always throws exception)
     */
    @Retryable(
            retryFor = RuntimeException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public String basicRetry() {
        log.info("[BasicRetry] Attempt {}",
                RetrySynchronizationManager.getContext().getRetryCount() + 1);
        throw new RuntimeException("Simulated API failure!");
    }

    // ----------------- RETRY WITH RECOVERY ------------------
    /**
     * Demonstrates retry with recovery mechanism
     * @return Fallback response after 3 failed attempts
     */
    @Retryable(
            retryFor = ArithmeticException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public String basicRetryWithRecover() {
        log.info("[RetryWithRecovery] Attempt {}",
                RetrySynchronizationManager.getContext().getRetryCount() + 1);
        throw new ArithmeticException("Division error!");
    }

    @Recover
    public String recover(ArithmeticException e) {
        log.warn("[Recovery] All attempts failed. Returning fallback");
        return "Safe fallback result";
    }

    // ------------ RETRY WITH PARAMETERS & RECOVERY -----------
    /**
     * Demonstrates retry with method parameters passed to recovery
     * @param id Transaction ID
     * @param argument Business context parameter
     * @return Fallback response with input parameters
     */
    @Retryable(
            retryFor = ArithmeticException.class,
            maxAttempts = 4,
            backoff = @Backoff(delay = 1000)
    )
    public String parameterizedRetry(int id, String argument) {
        log.info("[ParamRetry] ID: {}, Arg: {}, Attempt: {}",
                id, argument,
                RetrySynchronizationManager.getContext().getRetryCount() + 1);
        throw new ArithmeticException("Parametrized failure!");
    }

    @Recover
    public String parameterizedRecover(ArithmeticException e, int id, String argument) {
        log.warn("[ParamRecovery] Failed for ID: {}, Arg: {}", id, argument);
        return String.format("Fallback for ID: %d, Arg: %s", id, argument);
    }

    // ------------ TARGETED RECOVERY METHOD -------------------
    /**
     * Demonstrates explicit recovery method mapping
     * @param id Transaction ID
     * @param argument Business context parameter
     * @return Specialized fallback response
     */
    @Retryable(
            retryFor = ArithmeticException.class,
            maxAttempts = 4,
            backoff = @Backoff(delay = 1000),
            recover = "targetedRecovery"
    )
    public String directedRecoveryRetry(int id, String argument) {
        log.info("[DirectedRetry] Attempt {}",
                RetrySynchronizationManager.getContext().getRetryCount() + 1);
        throw new ArithmeticException("Directed failure!");
    }

    @Recover
    public String targetedRecovery(ArithmeticException e, int id, String argument) {
        log.warn("[TargetedRecovery] Special handling for ID: {}", id);
        return String.format("Special recovery for ID: %d", id);
    }

    // ------------ NON-RECOVERABLE RETRY ----------------------
    /**
     * Demonstrates non-recoverable retry scenario
     * @param id Transaction ID
     * @param argument Business context parameter
     * @return Throws exception after all attempts
     */
    @Retryable(
            retryFor = ArithmeticException.class,
            maxAttempts = 4,
            backoff = @Backoff(delay = 1000),
            notRecoverable = ArithmeticException.class
    )
    public String nonRecoverableRetry(int id, String argument) {
        log.info("[NonRecoverable] Attempt {}",
                RetrySynchronizationManager.getContext().getRetryCount() + 1);
        throw new ArithmeticException("Unrecoverable error!");
    }

    // ------------ NO RETRY BUT RECOVERABLE -------------------
    /**
     * Demonstrates immediate recovery without retries
     * @param id Transaction ID
     * @param argument Business context parameter
     * @return Immediate fallback response
     */
    @Retryable(
            noRetryFor = ArithmeticException.class,
            recover = "immediateRecovery",
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public String immediateRecoveryCase(int id, String argument) {
        log.info("[ImmediateRecovery] Initial attempt");
        throw new ArithmeticException("Immediate recovery trigger!");
    }

    @Recover
    public String immediateRecovery(ArithmeticException e, int id, String argument) {
        log.warn("[ImmediateRecovery] Handling without retries");
        return String.format("Immediate fallback for ID: %d", id);
    }
}