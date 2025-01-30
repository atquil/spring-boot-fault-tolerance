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

## 3. Basic Spring Retry: 

>By default, @Retryable will attempt 3 times (1 first attempt + 2 retries) for all exceptions are thrown 

-  3.1: Create the Service -  `BasicRetryService.java`

   ```java
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
    }
   ```

- 3.2 Create a `controller` package and add `RetryController.java` file 

```java
@RestController
@RequiredArgsConstructor
public class RetryController {


    private final BasicRetryService basicRetryService;

    @GetMapping("/basic-retry")
    public ResponseEntity<?> basicRetry() {
        return ResponseEntity.ok(basicRetryService.basicRetry());
    }
}
```

- 3.3 Now Call teh API : **GET** http://localhost:8081/basic-retry

**Output**
```java
2025-01-30T12:44:15.944+05:30  INFO 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2025-01-30T12:44:15.944+05:30  INFO 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2025-01-30T12:44:15.945+05:30  INFO 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
2025-01-30T12:44:15.971+05:30 DEBUG 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=0; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetry'
2025-01-30T12:44:15.972+05:30  INFO 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] c.a.s.service.BasicRetryService          : [BasicRetryService:basicRetry] Retry Number:0
2025-01-30T12:44:16.977+05:30 DEBUG 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=1; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetry'
2025-01-30T12:44:16.978+05:30 DEBUG 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=1; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetry'
2025-01-30T12:44:16.978+05:30  INFO 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] c.a.s.service.BasicRetryService          : [BasicRetryService:basicRetry] Retry Number:1
2025-01-30T12:44:17.983+05:30 DEBUG 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=2; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetry'
2025-01-30T12:44:17.984+05:30 DEBUG 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=2; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetry'
2025-01-30T12:44:17.984+05:30  INFO 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] c.a.s.service.BasicRetryService          : [BasicRetryService:basicRetry] Retry Number:2
2025-01-30T12:44:17.984+05:30 DEBUG 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=3; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetry'
2025-01-30T12:44:17.985+05:30 DEBUG 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Retry failed last attempt: count=3; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetry'
2025-01-30T12:44:17.995+05:30 ERROR 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: org.springframework.retry.ExhaustedRetryException: Cannot locate recovery method] with root cause

java.lang.RuntimeException: API call failed!

```

## 4. Spring Recovery

>When all teh retry attempts result in failure, then we can use recover method to return proper response.
>- Recover must have same **return type as the `@EnableRetry`.**

- 4.1 Let's add a method **basicRetryWithRecover**
```java

@Slf4j
@Service
public class BasicRetryService {

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
}

```

- 4.2 Create a endpoint to call the API 

```java
    @GetMapping("/basic-retry-with-recovery")
    public ResponseEntity<?> basicRetryWithRecoveryService() {
        return ResponseEntity.ok(basicRetryService.basicRetryWithRecover());
    }

```

- 4.3 Call the Endpoint : **GET** http://localhost:8081/basic-retry-with-recovery

**Output** 
```java

2025-01-30T12:44:48.892+05:30 DEBUG 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-2] o.s.retry.support.RetryTemplate          : Retry: count=0; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryWithRecover'
2025-01-30T12:44:48.893+05:30  INFO 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-2] c.a.s.service.BasicRetryService          : [BasicRetryService:basicRetryWithRecover] Retry Number:0
2025-01-30T12:44:49.899+05:30 DEBUG 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-2] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=1; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryWithRecover'
2025-01-30T12:44:49.899+05:30 DEBUG 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-2] o.s.retry.support.RetryTemplate          : Retry: count=1; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryWithRecover'
2025-01-30T12:44:49.903+05:30  INFO 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-2] c.a.s.service.BasicRetryService          : [BasicRetryService:basicRetryWithRecover] Retry Number:1
2025-01-30T12:44:50.909+05:30 DEBUG 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-2] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=2; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryWithRecover'
2025-01-30T12:44:50.909+05:30 DEBUG 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-2] o.s.retry.support.RetryTemplate          : Retry: count=2; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryWithRecover'
2025-01-30T12:44:50.909+05:30  INFO 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-2] c.a.s.service.BasicRetryService          : [BasicRetryService:basicRetryWithRecover] Retry Number:2
2025-01-30T12:44:50.909+05:30 DEBUG 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-2] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=3; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryWithRecover'
2025-01-30T12:44:50.909+05:30 DEBUG 19507 --- [spring-boot-fault-tolerance] [nio-8081-exec-2] o.s.retry.support.RetryTemplate          : Retry failed last attempt: count=3; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryWithRecover'
All retries failed. Fallback method called.
```

> We can also include **method arguments** for @Recover methods, but 
> - the **order must follow its @Retryable method**
> - return type must be same for both @Retryable and @Recover

Inside Service: 

```java
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
```

Inside Controller: 

```java
@GetMapping("/basic-retry-with-recovery-using-arguments")
    public ResponseEntity<?> basicRetryWithRecoveryUsingArguments() {
        String argument = "ABC";
        int id = 123;
        return ResponseEntity.ok(basicRetryService.basicRetryWithRecoveryUsingArguments(id,argument));
    }
```
- Output: 
```properties
2025-01-30T12:56:46.466+05:30  INFO 19571 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2025-01-30T12:56:46.466+05:30  INFO 19571 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2025-01-30T12:56:46.467+05:30  INFO 19571 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
2025-01-30T12:56:46.490+05:30 DEBUG 19571 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=0; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryWithRecoveryUsingArguments'
Id:{} 123 Argument:{}ABC
2025-01-30T12:56:46.491+05:30  INFO 19571 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] c.a.s.service.BasicRetryService          : [BasicRetryService:basicRetryWithRecoveryUsingArguments] Retry Number:0 
2025-01-30T12:56:47.498+05:30 DEBUG 19571 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=1; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryWithRecoveryUsingArguments'
2025-01-30T12:56:47.498+05:30 DEBUG 19571 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=1; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryWithRecoveryUsingArguments'
Id:{} 123 Argument:{}ABC
2025-01-30T12:56:47.499+05:30  INFO 19571 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] c.a.s.service.BasicRetryService          : [BasicRetryService:basicRetryWithRecoveryUsingArguments] Retry Number:1 
2025-01-30T12:56:48.504+05:30 DEBUG 19571 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=2; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryWithRecoveryUsingArguments'
2025-01-30T12:56:48.505+05:30 DEBUG 19571 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=2; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryWithRecoveryUsingArguments'
Id:{} 123 Argument:{}ABC
2025-01-30T12:56:48.506+05:30  INFO 19571 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] c.a.s.service.BasicRetryService          : [BasicRetryService:basicRetryWithRecoveryUsingArguments] Retry Number:2 
2025-01-30T12:56:49.507+05:30 DEBUG 19571 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=3; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryWithRecoveryUsingArguments'
2025-01-30T12:56:49.508+05:30 DEBUG 19571 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=3; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryWithRecoveryUsingArguments'
Id:{} 123 Argument:{}ABC
2025-01-30T12:56:49.508+05:30  INFO 19571 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] c.a.s.service.BasicRetryService          : [BasicRetryService:basicRetryWithRecoveryUsingArguments] Retry Number:3 
2025-01-30T12:56:49.508+05:30 DEBUG 19571 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=4; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryWithRecoveryUsingArguments'
2025-01-30T12:56:49.509+05:30 DEBUG 19571 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Retry failed last attempt: count=4; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryWithRecoveryUsingArguments'
All retries failed. Fallback method called.
```

> Explicitly declare a @Recover method for a @Retryable method i.e. which recover method should trigger

Inside Service: 
```java
    // ---------------- Call Specific Recover Method:"recoverFromArithmeticException"--------------------------

    // Retry configuration: Retry on ArithmeticException, max 4 attempts, 1-second delay
    @Retryable(
            retryFor = {ArithmeticException.class}, // Retry on ArithmeticException
            maxAttempts = 4,                 // Max retry attempts
            backoff = @Backoff(delay = 1000), // 1-second delay between retries
            recover = "recoverFromArithmeticException"
    )
    public String basicRetryWithSpecificRecover(int id, String argument) {
        System.out.println("Id:{} "+id+" Argument:{}"+argument);
        log.info("[BasicRetryService:basicRetryWithSpecificRecover] Retry Number:{} ",Objects.requireNonNull(RetrySynchronizationManager.getContext()).getRetryCount());
        throw new ArithmeticException("Divide by zero!"); // Simulate failure

    }

    // Recover with Explicit Exception
    @Recover
    public String recoverFromArithmeticException(ArithmeticException e,int id,String argument) {
        System.out.println("All retries failed. Fallback method called.");
        return "Recover Using User Defined Method"+"id:"+id+" Argument:"+argument;
    }
```
Inside Controller: 
```java

    @GetMapping("/basic-retry-with-specific-recover")
    public ResponseEntity<?> basicRetryWithSpecificRecover() {
        String argument = "ABC";
        int id = 123;
        return ResponseEntity.ok(basicRetryService.basicRetryWithSpecificRecover(id,argument));
    }

```

## 5. Skip Exception Handling Using

> Using :
> - `noRetryFor`: Excepiton which you don't want retry to happen, but if if Recovery is provided then it will happen
> - `notRecoverable`: Exception for which you don't even want recovery to happen, it will result in throwing of Exception, after retry. 

> We can use any of the combination between retry|noRetryFor and recover|notRecoverable

### 5.1 : **notRecoverable** 

Service: 
```java
  // ---------------- Basic Retry But Not Recoverable: --------------------------


    // Though the Exception is same, it will retry 4 times, but will not try to find recover method.
    // Retry configuration: Retry on ArithmeticException, max 4 attempts, 1-second delay
    @Retryable(
            retryFor = {ArithmeticException.class}, // Retry on ArithmeticException
            maxAttempts = 4,                 // Max retry attempts
            backoff = @Backoff(delay = 1000), // 1-second delay between retries
            //recover = "recoverExample"
            notRecoverable = {ArithmeticException.class} //It will not try to recover.
    )
    public Object basicRetryButNotRecoverable(int id, String argument) {
        System.out.println("Id:{} "+id+" Argument:{}"+argument);
        log.info("[BasicRetryService:basicRetryButNotRecoverable] Retry Number:{} ",Objects.requireNonNull(RetrySynchronizationManager.getContext()).getRetryCount());
        throw new ArithmeticException("Divide by zero!"); // Simulate failure

    }

```

Controller: 

```java
   @GetMapping("/basic-retry-but-notRecoverable")
    public ResponseEntity<?> basicRetryButNotRecoverable() {
        String argument = "ABC";
        int id = 123;
        return ResponseEntity.ok(basicRetryService.basicRetryButNotRecoverable(id,argument));
    }
```
Output: **GET** http://localhost:8081/basic-retry-but-notRecoverable
```java
2025-01-30T16:20:43.332+05:30  INFO 20951 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2025-01-30T16:20:43.335+05:30  INFO 20951 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2025-01-30T16:20:43.337+05:30  INFO 20951 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 2 ms
2025-01-30T16:20:43.372+05:30 DEBUG 20951 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=0; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryButNotRecoverable'
Id:{} 123 Argument:{}ABC
2025-01-30T16:20:43.373+05:30  INFO 20951 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] c.a.s.service.BasicRetryService          : [BasicRetryService:basicRetryButNotRecoverable] Retry Number:0 
2025-01-30T16:20:44.379+05:30 DEBUG 20951 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=1; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryButNotRecoverable'
2025-01-30T16:20:44.379+05:30 DEBUG 20951 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=1; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryButNotRecoverable'
Id:{} 123 Argument:{}ABC
2025-01-30T16:20:44.379+05:30  INFO 20951 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] c.a.s.service.BasicRetryService          : [BasicRetryService:basicRetryButNotRecoverable] Retry Number:1 
2025-01-30T16:20:45.384+05:30 DEBUG 20951 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=2; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryButNotRecoverable'
2025-01-30T16:20:45.385+05:30 DEBUG 20951 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=2; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryButNotRecoverable'
Id:{} 123 Argument:{}ABC
2025-01-30T16:20:45.385+05:30  INFO 20951 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] c.a.s.service.BasicRetryService          : [BasicRetryService:basicRetryButNotRecoverable] Retry Number:2 
2025-01-30T16:20:46.387+05:30 DEBUG 20951 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=3; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryButNotRecoverable'
2025-01-30T16:20:46.388+05:30 DEBUG 20951 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=3; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryButNotRecoverable'
Id:{} 123 Argument:{}ABC
2025-01-30T16:20:46.388+05:30  INFO 20951 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] c.a.s.service.BasicRetryService          : [BasicRetryService:basicRetryButNotRecoverable] Retry Number:3 
2025-01-30T16:20:46.388+05:30 DEBUG 20951 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=4; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryButNotRecoverable'
2025-01-30T16:20:46.388+05:30 DEBUG 20951 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Retry failed last attempt: count=4; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryButNotRecoverable'
2025-01-30T16:20:46.388+05:30 DEBUG 20951 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Retry exhausted and recovery disabled for this throwable
2025-01-30T16:20:46.398+05:30 ERROR 20951 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: java.lang.ArithmeticException: Divide by zero!] with root cause

java.lang.ArithmeticException: Divide by zero!
	at com.atquil.springbootfaulttolerance.service.BasicRetryService.basicRetryButNotRecoverable(BasicRetryService.java:121) ~[main/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
```

### 5.2 noRetryFor


Service: 

```java

    // ---------------- No Retry But Recoverable:--------------------------
    
    
    @Retryable(
            recover = "recoverFromException", // Recovery method
            noRetryFor = {ArithmeticException.class}, // Do not retry on ArithmeticException
            maxAttempts = 3,                          // Max retry attempts
            backoff = @Backoff(delay = 1000)          // 1-second delay between retries
    )
    public String noRetryButRecoverable(int id, String argument) {
        log.info("Performing operation for id: {} and argument: {}", id, argument);
        throw new ArithmeticException("Divide by zero!"); // Simulate failure
    }
    
    // Recovery method
    @Recover
    public String recoverFromException(ArithmeticException e, int id, String argument) {
        log.info("Recovery method called for id: {} and argument: {}", id, argument);
        System.out.println("Recovering from ArithmeticException , with No Retry");
        return "Recovered from ArithmeticException: " + e.getMessage();
        
    }
```
Controller: 

```java
    @GetMapping("/noretry-but-recoverable")
    public ResponseEntity<?> noRetryButRecoverable() {
        String argument = "ABC";
        int id = 123;
        return ResponseEntity.ok(basicRetryService.noRetryButRecoverable(id,argument));
    }
```

Output: **GET** http://localhost:8081/noretry-but-recoverable

```java
2025-01-30T16:43:52.932+05:30  INFO 21248 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2025-01-30T16:43:52.932+05:30  INFO 21248 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2025-01-30T16:43:52.933+05:30  INFO 21248 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
2025-01-30T16:43:52.950+05:30 DEBUG 21248 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=0; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.noRetryButRecoverable'
2025-01-30T16:43:52.950+05:30  INFO 21248 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] c.a.s.service.BasicRetryService          : Performing operation for id: 123 and argument: ABC
2025-01-30T16:43:52.951+05:30 DEBUG 21248 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=1; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.noRetryButRecoverable'
2025-01-30T16:43:52.951+05:30 DEBUG 21248 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] o.s.retry.support.RetryTemplate          : Retry failed last attempt: count=1; for: 'com.atquil.springbootfaulttolerance.service.BasicRetryService.noRetryButRecoverable'
2025-01-30T16:43:52.951+05:30  INFO 21248 --- [spring-boot-fault-tolerance] [nio-8081-exec-1] c.a.s.service.BasicRetryService          : Recovery method called for id: 123 and argument: ABC
Recovering from ArithmeticException , with No Retry

```
## 5. Imperative (RetryTemplate) vs Declarative (@Retryable)

> Imperative Retry with `RetryTemplate`
> 
>Imperative retry is programmatic and gives you **more control** over the retry logic. **It’s useful when you need dynamic or complex retry behavior**.
> 

- 5.1 Let's create a Service `ImperativeStyleRetryServiceUsingRestTemplate`


```java
RetryTemplate template = RetryTemplate.builder()
        .maxAttempts(3)
        .fixedBackoff(1000)
        .retryOn(RuntimeException.class)
        .build();
        
    template.execute(ctx -> {
        // ... do something
    });
```

>**Declarative Style**
>
> - For using Spring Retry in a Declarative way, we need to add `AOP dependency`

```java
@Configuration
@EnableRetry
public class Application {

}

@Service
class Service {
    @Retryable(retryFor = RuntimeException.class)
    public void service() {
        // ... do something
    }
    @Recover
    public void recover(RuntimeException e) {
       // ... panic
    }
}
```  


### Step 5: Using Declarative Style

>By default, **@Retryable will attempt 3 times** (1 first attempt + 2 retries) for all exceptions are thrown and fallback to the @Recover method that has the same return type in the same class.. 
- @Retryable: Indicates that this method is the candidate for retry. 
    - **backoff** policy: Configure the strategy for the number of retries, delay between two retries etc.
    - **maxAttempts** : Default retry count is 3, and the first invocation of this method is always the first retry attempt
    - 

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


### Step 6: [Optional]Now Enable logs for retry in application.yml

- In `applicaiton.yml` file add these config so that we can monitor the application

    ```properties
    spring:
      application:
        name: spring-boot-fault-tolerance
    
    logging:
      level:
        root: INFO
        org.springframework.retry: DEBUG
    ```


### Step 7: Now the Endpoint and see the console: 

- Api: `GET http://localhost:8080/retry`

    ```java
    2024-12-08T22:34:18.147+05:30  INFO 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
    2024-12-08T22:34:18.170+05:30 DEBUG 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=0; for: 'com.atquil.springbootfaulttolerance.service.DeclarativeStyleRetryService.callExternalService'
    2024-12-08T22:34:18.171+05:30  INFO 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] c.a.s.service.ExternalService            : Retry Number: 0
    2024-12-08T22:34:18.171+05:30 DEBUG 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.r.b.ExponentialRandomBackOffPolicy   : Sleeping for 9605
    2024-12-08T22:34:27.787+05:30 DEBUG 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=1; for: 'com.atquil.springbootfaulttolerance.service.DeclarativeStyleRetryService.callExternalService'
    2024-12-08T22:34:27.788+05:30 DEBUG 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=1; for: 'com.atquil.springbootfaulttolerance.service.DeclarativeStyleRetryService.callExternalService'
    2024-12-08T22:34:27.788+05:30  INFO 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] c.a.s.service.ExternalService            : Retry Number: 1
    2024-12-08T22:34:27.788+05:30 DEBUG 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.r.b.ExponentialRandomBackOffPolicy   : Sleeping for 30000
    2024-12-08T22:34:57.795+05:30 DEBUG 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=2; for: 'com.atquil.springbootfaulttolerance.service.DeclarativeStyleRetryService.callExternalService'
    2024-12-08T22:34:57.798+05:30 DEBUG 5782 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=2; for: 'com.atquil.springbootfaulttolerance.service.DeclarativeStyleRetryService.callExternalService'
    Attempt 3: Success
    ```

## Implementation of  `@Recover`


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
                5, // 3 is the default attempt
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
    2024-12-08T22:41:53.190+05:30 DEBUG 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=0; for: 'com.atquil.springbootfaulttolerance.service.DeclarativeStyleRetryService.someArithmeticException'
    2024-12-08T22:41:53.192+05:30  INFO 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] c.a.s.service.ExternalService            : Retry Number: 0
    2024-12-08T22:41:54.195+05:30 DEBUG 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=1; for: 'com.atquil.springbootfaulttolerance.service.DeclarativeStyleRetryService.someArithmeticException'
    2024-12-08T22:41:54.196+05:30 DEBUG 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=1; for: 'com.atquil.springbootfaulttolerance.service.DeclarativeStyleRetryService.someArithmeticException'
    2024-12-08T22:41:54.196+05:30  INFO 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] c.a.s.service.ExternalService            : Retry Number: 1
    2024-12-08T22:41:55.199+05:30 DEBUG 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=2; for: 'com.atquil.springbootfaulttolerance.service.DeclarativeStyleRetryService.someArithmeticException'
    2024-12-08T22:41:55.199+05:30 DEBUG 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=2; for: 'com.atquil.springbootfaulttolerance.service.DeclarativeStyleRetryService.someArithmeticException'
    2024-12-08T22:41:55.199+05:30  INFO 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] c.a.s.service.ExternalService            : Retry Number: 2
    2024-12-08T22:41:55.200+05:30 DEBUG 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=3; for: 'com.atquil.springbootfaulttolerance.service.DeclarativeStyleRetryService.someArithmeticException'
    2024-12-08T22:41:55.200+05:30 DEBUG 5834 --- [spring-boot-fault-tolerance] [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Retry failed last attempt: count=3; for: 'com.atquil.springbootfaulttolerance.service.DeclarativeStyleRetryService.someArithmeticException'
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
