# Spring Retry (Including Circuit Breaker Pattern)


## 1. Introduction
> **What and Why Spring Retry ?**
> 
> Spring Retry provides an ability to automatically re-invoke a failed operation. 
> This is helpful where the errors may be transient (like a momentary network glitch).
> 
> When the remote APIs are invoked, Spring Retry `@Retry` help us to retry the request if it fails for some reason such as 
>   - a network outage, 
>   - server down, 
>   - network glitch, 
>   - deadlock etc
>   - This is helpful where the errors may be transient (like a momentary network glitch)
> 
> In such cases, Spring Retry provides an ability to automatically re-invoke a failed operation. 

> **Why is it important in microservices?**
> 
> It’s essential for building **`fault-tolerant microservices`**.

> **What are different types of `Spring Retry` ?**
> 
> There are two types of Retry : 
> - `Stateless` : when **no** transaction needed, usually in a simple request call with `RestClient`/`RestTemplate`. This is usually used along with `@Retryable`.
> - `Stateful`: when transaction needed, usually in a database update with `Hibernate`. This is usually used along with `@CircuitBreaker`.
> 

> **Explain @Recover?**
>
> We can also define the **fallback method** - `@Recover` if all retries fail.


## 2. Setup 

1. **Dependencies**

    Add the following dependencies to your pom.xml (Maven) or build.gradle (Gradle):

    **Maven:**
    ```xml
    <dependency>
        <groupId>org.springframework.retry</groupId>
        <artifactId>spring-retry</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>
    ```
    
    **Gradle: **
    
    ```groovy
    implementation 'org.springframework.retry:spring-retry'
    implementation 'org.springframework.boot:spring-boot-starter-aop'
    ```
2. Enable Retry:
   - The `@EneableRetry` annotation enables the spring retry feature in the application.
   - The @EnableRetry scan for all `@Retryable` and `@Recover` annotated methods and `proxies them using AOP`

    - `@EnableRetry` will import `RetryConfiguration` for us that create a `AnnotationAwareRetryOperationsInterceptor` as our AOP advice. This interceptor works as an entry point for processing retries of our methods.

    - Add `@EnableRetry`
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

    You can also create a `configuration` repo and add a `RetryConfig` in it and add **@EnableRetry** to it

    ```java
      @Configuration
      @EnableRetry
      public class RetryConfig {
      }
     ```
3. (Optional) Print logs 
- application.properties
```properties
logging.level.org.springframework.retry=debug
```
- application.yml
```yaml
logging:
  level:
    root: INFO
    org.springframework.retry: DEBUG
```

## 3. Declarative (@Retryable) Style for Retry

### Declarative Retry
>By default, **@Retryable will attempt 3 times** (1 first attempt + 2 retries) for all exceptions are thrown and fallback to the @Recover method that has the same return type in the same class..
>- @Retryable: Indicates that this method is the candidate for retry.
    - **backoff** policy: Configure the strategy for the number of retries, delay between two retries etc.
    - **maxAttempts** : Default retry count is 3, and the first invocation of this method is always the first retry attempt

Create a class : `DeclarativeRetryService` and `DeclarativeRetryController` for this explanation. 

#### 1. Basic Retry
  - Description: Demonstrates a basic retry mechanism with 3 attempts and a 1-second delay between retries.


- Service: 
```java
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
```

- Controller: 
```java
@GetMapping("/basic-retry")
public ResponseEntity<String> basicRetry() {
    return ResponseEntity.ok(declarativeRetryService.basicRetry());
}
```
- Test: http://localhost:8081/basic-retry

```java

```

#### 2. Retry with Recovery

>When all the retry attempts result in failure, then we can use `@Recover` method to return proper response.
>- Recover must have same **return type as the `@EnableRetry`.**
>

- Description: Demonstrates retry with a recovery mechanism. After 3 failed attempts, the recovery method is called.

- Service:
```java
/// ----------------- RETRY WITH RECOVERY ------------------
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

```

- Controller:
```java
@GetMapping("/retry-with-recovery")
public ResponseEntity<String> retryWithRecovery() {
    return ResponseEntity.ok(declarativeRetryService.basicRetryWithRecover());
}
```
- Test: 
  - http://localhost:8081/retry-with-recovery
  - Behavior: Retries 3 times, then calls the recovery method.

#### 3. Retry with Parameters and Recovery
> We can also include **method arguments** for @Recover methods, but
> - the **order must follow its @Retryable method**
> - return type must be same for both @Retryable and @Recover

- Description: Demonstrates retry with method parameters passed to the recovery method

- Service:
```java
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

```

- Controller:
```java
@GetMapping("/retry-with-params")
public ResponseEntity<String> retryWithParams(
        @RequestParam(defaultValue = "123") int id,
        @RequestParam(defaultValue = "test") String argument) {
    return ResponseEntity.ok(declarativeRetryService.basicRetryWithRecoveryUsingArguments(id, argument));
}
```
- Test: 
  - http://localhost:8081/retry-with-params?id=123&argument=test
  - Behavior: Retries 4 times, then calls the recovery method with the provided parameters.

#### 4. Targeted Recovery Method

> Explicitly declare a @Recover method for a @Retryable method i.e. which recover method should trigger

- Description: Demonstrates retry with a specific recovery method mapped using the recover attribute.
- Service:
```java
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
```

- Controller:
```java
@GetMapping("/targeted-recovery")
public ResponseEntity<String> targetedRecovery(
        @RequestParam(defaultValue = "456") int id,
        @RequestParam(defaultValue = "target") String argument) {
    return ResponseEntity.ok(declarativeRetryService.basicRetryWithSpecificRecover(id, argument));
}
```
- Test: 
  - http://localhost:8081/targeted-recovery?id=456&argument=target
  - Behavior: Retries 4 times, then calls the specific recovery method

#### 5. Skip Exception Handling: 

> Using :
> - `noRetryFor`: Excepiton which you don't want retry to happen, but if if Recovery is provided then it will happen
> - `notRecoverable`: Exception for which you don't even want recovery to happen, it will result in throwing of Exception, after retry.

> We can use any of the combination between `retry | noRetryFor` and `recover | notRecoverable`

##### Non-Recoverable Retry
- Description: Demonstrates a retry scenario where recovery is skipped for specific exceptions.
- Service:
```java
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

```

- Controller:
```java
@GetMapping("/non-recoverable")
public ResponseEntity<String> nonRecoverableRetry(
        @RequestParam(defaultValue = "789") int id,
        @RequestParam(defaultValue = "critical") String argument) {
    try {
        return ResponseEntity.ok(declarativeRetryService.basicRetryButNotRecoverable(id, argument));
    } catch (ArithmeticException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Non-recoverable error: " + e.getMessage());
    }
}
```
- Test: 
  - http://localhost:8081/non-recoverable?id=789&argument=critical
  - Behavior: Retries 4 times, then throws an exception without recovery.

##### No Retry but Recoverable
- Description: Demonstrates a scenario where no retry is performed, but the recovery method is called immediately
- Service:
```java
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

```

- Controller:
```java
@GetMapping("/no-retry-recoverable")
public ResponseEntity<String> noRetryButRecoverable(
        @RequestParam(defaultValue = "101") int id,
        @RequestParam(defaultValue = "fallback") String argument) {
    return ResponseEntity.ok(declarativeRetryService.noRetryButRecoverable(id, argument));
}
```
- Test: 
  - http://localhost:8081/no-retry-recoverable?id=101&argument=fallback
  - Behavior: Skips retries and immediately calls the recovery method


## 6. Imperative (RetryTemplate) Style for Retry

> Imperative Retry using `RetryTemplate`
> 
>Imperative retry is programmatic and gives you **more control** over the retry logic. **It’s useful when you need dynamic or complex retry behavior**.
 

- **Configuration Class** : `ImperativeRetryConfigUsingRestTemplate`
  
This class defines the RetryTemplate bean with custom retry policies and backoff strategies


```java
@Configuration
@Slf4j
public class ImperativeRetryConfigUsingRestTemplate {

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Retry Policy: Retry up to 5 times for IllegalArgumentException
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
                5, // Max retry attempts
                Collections.singletonMap(IllegalArgumentException.class, true) // Retry on IllegalArgumentException
        );

        // Backoff Strategy: Exponential backoff with initial delay of 1 second and max delay of 7 seconds
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // Initial delay: 1 second
        backOffPolicy.setMultiplier(2); // Multiplier for exponential backoff
        backOffPolicy.setMaxInterval(7000); // Maximum delay: 7 seconds

        // Set retry policy and backoff policy
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;

    }
}

```

> The delay between retries will follow an exponential backoff strategy (1s, 2s, 4s, etc., up to a maximum of 7s)

>**Noted: Set the backoff too short can overwhelm our server, but set it too long can increase latency, affecting user/client experience. Some clients have their own response timeout, they'll obviously give up waiting for a response and receive an error instead.**

- **Service Class**: `ImperativeRetryService.java`

This class uses the RetryTemplate to **perform retries and handle recovery**.

```java
@Slf4j
@Service
public class ImperativeRetryService {


    @Autowired
    private RetryTemplate retryTemplate;

    public String performImperativeRetry(int id, String argument) {
        return retryTemplate.execute(context -> {
            // Retryable logic
            log.info("[ImperativeRetryService:performOperation ]Performing operation for id: {} and argument: {}. Retry count: {}", id, argument, context.getRetryCount());
            if (id == 1) {
                throw new IllegalArgumentException("Invalid ID!"); // Simulate failure
            }
            return "Operation successful for id: " + id + " and argument: " + argument;
        }, context -> {
            // Recovery logic
            log.info("[ImperativeRetryService:performOperation ]All retries failed. Recovery method called for id: {} and argument: {}", id, argument);
            return "Recovered from failure for id: " + id + " and argument: " + argument;
        });
    }
}
```
If all retries fail, the recovery logic (second lambda in retryTemplate.execute) will be called

- **Controller** :  `ImperativeRetryController.java`
```java
@RestController
@RequiredArgsConstructor
public class ImperativeRetryController {


    private final ImperativeRetryService imperativeRetryService;

    @GetMapping("/imperative-retry")
    public String performImperativeRetry(@RequestParam int id, @RequestParam String argument) {
        return imperativeRetryService.performImperativeRetry(id, argument);
    }
}


```
- **Testing**: 
  - Successful Operation: 
    - Call the endpoint: http://localhost:8081/imperative-retry?id=2&argument=test
    - The operation will succeed without retries.
  - Retry and Recovery:
    - Call the endpoint: http://localhost:8081/imperative-retry?id=1&argument=test
    - The method will retry up to 5 times (with exponential backoff) and then call the recovery method.



## Key Differences: Declarative vs Imperative Retry

| Feature       | Declarative Retry (@Retryable)                        | Imperative Retry (RetryTemplate)                      |
|---------------|-------------------------------------------------------|-------------------------------------------------------|
| Configuration | Annotations (@Retryable, @Recover).                   | Programmatic configuration (RetryTemplate).           |
| Control       | Limited to annotation parameters                      | Full control over retry policies and listeners.       |
| Use Case	     | Simple retry scenarios with minimal boilerplate.      | Complex retry logic requiring dynamic behavior.       |
| Recovery      | Uses a recovery callback in retryTemplate.execute().  | Uses a recovery callback in retryTemplate.execute().  |



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
5. What is the default no of retry available : `3`


### References
1. Wonderful Resource : [Spring Retry Github Repository.](https://github.com/spring-projects/spring-retry)
2. 