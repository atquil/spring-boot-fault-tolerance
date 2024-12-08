package com.atquil.springbootfaulttolerance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class SpringBootFaultToleranceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootFaultToleranceApplication.class, args);
    }

}
