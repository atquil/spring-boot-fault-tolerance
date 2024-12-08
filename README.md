# spring-boot-fault-tolerance

Spring Retry provides an ability to automatically re-invoke a failed operation. 
This is helpful where the errors may be transient (like a momentary network glitch).
## Table of Contents

## What and Why Spring Retry ?:

Spring Retry provides an ability to automatically re-invoke a failed operation.
This is helpful where the errors may be transient (like a momentary network glitch).





## Spring Retry Steps: 

### Step 1: Setting Up the Spring Boot Project
    
    - Create a new Spring Boot project using Spring Initializr (https://start.spring.io/). Include the following dependencies:
        - Spring Web
        - Spring Retry : spring-retry
        - Spring Boot Starter Actuator
    - 
Always check for the latest version for **spring-retry**
```gradle
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.projectlombok:lombok'
    implementation 'org.springframework.retry:spring-retry:2.0.10'
    implementation 'org.springframework:spring-aspects'
```

### Step 2 : Create the Endpoint 

- Create a `controller` package and add `RetryController.java` file 

```java
@RestController
public class RetryController {

    @Autowired
    private ExternalService externalService;

    @GetMapping("/retry")
    public String retryService() {
        return externalService.callExternalService(); }
}

```

### Step 3 : Add the Method to call external service

- Create a package `service` inside this create class `ExternalService` which will call external method/endpoint 

    ```java
    // Marks this class as a Spring-managed service bean
    @Service
    public class ExternalService {
        
        private int attempt = 0;
        private static final Logger log = LoggerFactory.getLogger(ExternalService.class);

        @Retryable( // Annotation to enable retry logic on the method
                retryFor = RuntimeException.class, //Specifies the type of exception to trigger a retry
                maxAttempts = 3, // Maximum number of retry attempts
                // Backoff: Retry strategy
                backoff = @Backoff(
                        delay = 5000, //Initial delay before the first retry (in milliseconds)
                        maxDelay = 4, // Maximum delay between retries (corrected from 4 to a meaningful value)
                        multiplier = 3, // Factor, by which the delay should be multiplied after each retry
                        random = true // To add jitter between retries within range
                )
        )
        public String callExternalService() {
            attempt ++;
            if(attempt < 3) {
                log.info("Retry Number: {}", Objects.requireNonNull(RetrySynchronizationManager.getContext()).getRetryCount());
                // When we get a runtime exception, it should reattempt for at-least 2 times
                throw new RuntimeException("Failing");
            }
            System.out.println("Attempt " + attempt + ": Success");
            return "Success";
        }   
    }
    ```
### Step 3 : Enable Retry Logic for spring boot application

-  Add `@EnableRetry`

    ```java
    @SpringBootApplication
    @EnableRetry
    public class SpringBootFaultToleranceApplication {
    
        public static void main(String[] args) {
            SpringApplication.run(SpringBootFaultToleranceApplication.class, args);
        }
    
    }
    ```
    **OR**

- You can also create a `configuration` repo and add a `RetryConfig` in it and add **@EnableRetry** to it

  ```java
  @Configuration
  @EnableRetry
  public class RetryConfig {
  }

    ```

### Step 4: Now Enable logs for retry in application.yml

- In `applicaiton.yml` file add these config

    ```properties
    spring:
      application:
        name: spring-boot-fault-tolerance
    
    logging:
      level:
        root: INFO
        org.springframework.retry: DEBUG
    ```


### Step 5: Now the Endpoint and see the console: 

- Api: `GET http://localhost:8080/retry`

    ```java
    2024-12-08T22:34:18.147+05:30  INFO 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
    2024-12-08T22:34:18.170+05:30 DEBUG 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=0; for: 'com.atquil.springbootfaulttolerance.service.ExternalService.callExternalService'
    2024-12-08T22:34:18.171+05:30  INFO 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] c.a.s.service.ExternalService            : Retry Number: 0
    2024-12-08T22:34:18.171+05:30 DEBUG 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.r.b.ExponentialRandomBackOffPolicy   : Sleeping for 9605
    2024-12-08T22:34:27.787+05:30 DEBUG 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=1; for: 'com.atquil.springbootfaulttolerance.service.ExternalService.callExternalService'
    2024-12-08T22:34:27.788+05:30 DEBUG 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=1; for: 'com.atquil.springbootfaulttolerance.service.ExternalService.callExternalService'
    2024-12-08T22:34:27.788+05:30  INFO 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] c.a.s.service.ExternalService            : Retry Number: 1
    2024-12-08T22:34:27.788+05:30 DEBUG 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.r.b.ExponentialRandomBackOffPolicy   : Sleeping for 30000
    2024-12-08T22:34:57.795+05:30 DEBUG 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=2; for: 'com.atquil.springbootfaulttolerance.service.ExternalService.callExternalService'
    2024-12-08T22:34:57.798+05:30 DEBUG 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=2; for: 'com.atquil.springbootfaulttolerance.service.ExternalService.callExternalService'
    Attempt 3: Success
    ```


## Configuration at Application Level: 

### Step 1: Create a method for which throws `ArthmeticException`

- In Service create a method `someArithmeticException`

    ```java

   // RetryConfigFile
    @Retryable(
            retryFor = ArithmeticException.class
    )
    public String someArithmeticException(){
        log.info("Retry Number: %d".formatted(Objects.requireNonNull(RetrySynchronizationManager.getContext()).getRetryCount()));
        int number = getDivideByZeroError(10) ;// ArithmeticException
        return "Success"; // It will never reach there
    }

    private int getDivideByZeroError(int i) {
        return i/0;
    }
    ```

### Step 2 : Now Configure the `RetryConfig` file for `ArthemeticException`

- Note: If **SimpleRetryPolicy** does not metion the Excepiton then it will be applicable to complete application

```java

@Configuration
@EnableRetry
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

```

This will give us more granular control on Config policy

### Step 3: Run the application

- API: `GET http://localhost:8080/retry/config`

    ```Java
    
    2024-12-08T22:41:53.167+05:30  INFO 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 5 ms
    2024-12-08T22:41:53.190+05:30 DEBUG 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=0; for: 'com.atquil.springbootfaulttolerance.service.ExternalService.someArithmeticException'
    2024-12-08T22:41:53.192+05:30  INFO 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] c.a.s.service.ExternalService            : Retry Number: 0
    2024-12-08T22:41:54.195+05:30 DEBUG 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=1; for: 'com.atquil.springbootfaulttolerance.service.ExternalService.someArithmeticException'
    2024-12-08T22:41:54.196+05:30 DEBUG 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=1; for: 'com.atquil.springbootfaulttolerance.service.ExternalService.someArithmeticException'
    2024-12-08T22:41:54.196+05:30  INFO 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] c.a.s.service.ExternalService            : Retry Number: 1
    2024-12-08T22:41:55.199+05:30 DEBUG 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=2; for: 'com.atquil.springbootfaulttolerance.service.ExternalService.someArithmeticException'
    2024-12-08T22:41:55.199+05:30 DEBUG 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=2; for: 'com.atquil.springbootfaulttolerance.service.ExternalService.someArithmeticException'
    2024-12-08T22:41:55.199+05:30  INFO 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] c.a.s.service.ExternalService            : Retry Number: 2
    2024-12-08T22:41:55.200+05:30 DEBUG 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=3; for: 'com.atquil.springbootfaulttolerance.service.ExternalService.someArithmeticException'
    2024-12-08T22:41:55.200+05:30 DEBUG 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Retry failed last attempt: count=3; for: 'com.atquil.springbootfaulttolerance.service.ExternalService.someArithmeticException'
    2024-12-08T22:41:55.209+05:30 ERROR 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: java.lang.ArithmeticException: / by zero] with root cause
    
    java.lang.ArithmeticException: / by zero
    ```

### Now Configure using 
# Interview Questions

1. What and why we need Retry Logic, and when we will use it ?
2. What is the default delay for Retry : `1000ms`
3. Explain **BackOff** and its terms? 

    Backoff is a strategy used in fault-tolerant systems to **manage retry attempts after a failure**.

   - **Delay**: Initial delay before the first retry
   - **MaxDelay**: Maximum delay between retries
   - **Multiplier**: Factor, by which the delay should be multiplied after each retry
   - **Random**:  Random value between range. 
4. What is the benefit of **Randomize with jitter** ?

    - This helps avoid **"thundering herds"** where multiple retries happen at exactly the same time.
    - **Reduced Load Spikes**: By spreading out the retries, jitter reduces the chance of many retries happening simultaneously, which can overwhelm your system or external dependencies.
    - **Better Distribution**: It helps distribute the retry attempts over time more evenly, improving the overall stability and responsiveness of your system.
