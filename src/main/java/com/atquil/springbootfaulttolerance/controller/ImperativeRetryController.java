package com.atquil.springbootfaulttolerance.controller;

import com.atquil.springbootfaulttolerance.service.ImperativeRetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author atquil
 */
@RestController
@RequiredArgsConstructor
public class ImperativeRetryController {


    private final ImperativeRetryService imperativeRetryService;

    @GetMapping("/imperative-retry")
    public String performImperativeRetry(@RequestParam int id, @RequestParam String argument) {
        return imperativeRetryService.performImperativeRetry(id, argument);
    }
}
