package com.atquil.springbootfaulttolerance.controller;

import com.atquil.springbootfaulttolerance.service.ExternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author atquil
 */

@RestController
public class RetryController {

    @Autowired
    private ExternalService externalService;

    @GetMapping("/retry")
    public String retryService() {
        return externalService.callExternalService(); }

    @GetMapping("/retry/config")
    public String retryConfig() {
        return externalService.someArithmeticException();
    }
}
