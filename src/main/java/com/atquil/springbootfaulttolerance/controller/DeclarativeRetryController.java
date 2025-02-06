package com.atquil.springbootfaulttolerance.controller;

import com.atquil.springbootfaulttolerance.service.DeclarativeRetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author atquil
 */

@RestController
@RequiredArgsConstructor
public class DeclarativeRetryController {

    private final DeclarativeRetryService retryService;

    @GetMapping("/basic-retry")
    public ResponseEntity<String> basicRetry() {
        return ResponseEntity.ok(retryService.basicRetry());
    }

    @GetMapping("/retry-with-recovery")
    public ResponseEntity<String> retryWithRecovery() {
        return ResponseEntity.ok(retryService.basicRetryWithRecover());
    }

    @GetMapping("/param-retry")
    public ResponseEntity<String> parameterizedRetry(
            @RequestParam(defaultValue = "1001") int id,
            @RequestParam(defaultValue = "default") String arg) {
        return ResponseEntity.ok(retryService.parameterizedRetry(id, arg));
    }

    @GetMapping("/directed-recovery")
    public ResponseEntity<String> directedRecovery(
            @RequestParam(defaultValue = "2001") int id,
            @RequestParam(defaultValue = "special") String arg) {
        return ResponseEntity.ok(retryService.directedRecoveryRetry(id, arg));
    }

    @GetMapping("/non-recoverable")
    public ResponseEntity<String> nonRecoverableRetry(
            @RequestParam(defaultValue = "3001") int id,
            @RequestParam(defaultValue = "critical") String arg) {
        try {
            return ResponseEntity.ok(retryService.nonRecoverableRetry(id, arg));
        } catch (ArithmeticException e) {
            return ResponseEntity.status(500).body("Final error: " + e.getMessage());
        }
    }

    @GetMapping("/immediate-recovery")
    public ResponseEntity<String> immediateRecovery(
            @RequestParam(defaultValue = "4001") int id,
            @RequestParam(defaultValue = "fast") String arg) {
        return ResponseEntity.ok(retryService.immediateRecoveryCase(id, arg));
    }
}