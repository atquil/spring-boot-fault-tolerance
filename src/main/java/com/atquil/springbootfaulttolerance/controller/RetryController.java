package com.atquil.springbootfaulttolerance.controller;

import com.atquil.springbootfaulttolerance.service.BasicRetryService;
import com.atquil.springbootfaulttolerance.service.DeclarativeStyleRetryService;
import com.atquil.springbootfaulttolerance.service.ImperativeStyleRetryServiceUsingRestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

/**
 * @author atquil
 */

@RestController
@RequiredArgsConstructor
public class RetryController {


    private final BasicRetryService basicRetryService;

    @GetMapping("/basic-retry")
    public ResponseEntity<?> basicRetry() {
        return ResponseEntity.ok(basicRetryService.basicRetry());
    }

    @GetMapping("/basic-retry-with-recovery")
    public ResponseEntity<?> basicRetryWithRecoveryService() {
        return ResponseEntity.ok(basicRetryService.basicRetryWithRecover());
    }

    @GetMapping("/basic-retry-with-recovery-using-arguments")
    public ResponseEntity<?> basicRetryWithRecoveryUsingArguments() {
        String argument = "ABC";
        int id = 123;
        return ResponseEntity.ok(basicRetryService.basicRetryWithRecoveryUsingArguments(id,argument));
    }

    @GetMapping("/basic-retry-with-specific-recover")
    public ResponseEntity<?> basicRetryWithSpecificRecover() {
        String argument = "ABC";
        int id = 123;
        return ResponseEntity.ok(basicRetryService.basicRetryWithSpecificRecover(id,argument));
    }

    @GetMapping("/basic-retry-but-notRecoverable")
    public ResponseEntity<?> basicRetryButNotRecoverable() {
        String argument = "ABC";
        int id = 123;
        return ResponseEntity.ok(basicRetryService.basicRetryButNotRecoverable(id,argument));
    }

    @GetMapping("/noretry-but-recoverable")
    public ResponseEntity<?> noRetryButRecoverable() {
        String argument = "ABC";
        int id = 123;
        return ResponseEntity.ok(basicRetryService.noRetryButRecoverable(id,argument));
    }


    private final ImperativeStyleRetryServiceUsingRestTemplate retryUsingRestTemplate;
    @GetMapping("/retry-using-rest-template")
    public ResponseEntity<?> retryUsingRestTemplate() {
        return ResponseEntity.ok(retryUsingRestTemplate.retryUsingRestTemplate());
    }

    @Autowired
    private DeclarativeStyleRetryService declarativeStyleRetryService;

//    @GetMapping("/retry")
//    public String retryService() {
//        return declarativeStyleRetryService.callExternalService(); }

//    @GetMapping("/retry/config")
//    public Strinhhg retryConfig() {
//        return declarativeStyleRetryService.someArithmeticException();
//    }
}
