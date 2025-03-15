# Mastering Fault Tolerance in Spring Boot Microservices: A Comprehensive Guide


## Understanding Fault Tolerance in Microservices

Fault tolerance is critical in microservice architectures where multiple services
communicate over networks, introducing various failure points. 
In a distributed system, failures are inevitable, so our applications must be designed to handle them gracefully.

## How it works: 

- Expect Failures: Networks drop, services crash, databases timeout.

- **Plan for Recovery**:
  - **Retry (🔄)**: Automatically re-try temporary failures (e.g., network blips).
  - **Circuit Breaker (⚡ )**: Stop calling a broken service (like turning off a overloaded circuit).
  - **Fallback**: Show cached data or a friendly message instead of errors. 
  - **Timeout**: Don’t wait forever—fail fast if a service is slow. 
  - **Bulkhead (🚧)**: Isolate failures (like ship compartments) to protect healthy services.
  - **Rate Limiting (🚦)**
- Monitoring (Actuator)

## Why It Matters
💡 **No Single Point of Failure**: One broken microservice doesn’t break the app.

💡 **Better User Experience**: Users see predefined backup response instead of a blank page.

💡 **Cost Savings**: Prevents servers from melting down during traffic spikes.

## Key Interview Points

1. Circuit Breaker vs Retry:
   - Use retry for transient errors (network blips)
   - Use circuit breaker for prolonged failures (down service)
2. Bulkhead Configuration:
   - "How to choose thread pool size?"
   - ```→ (Max expected requests) × (Avg processing time) / Time window```

3. Rate Limiter Strategies:
   - Token bucket: Allows burst (e.g., 100 requests in 1s)
   - Leaky bucket: Smooths traffic (e.g., 10 requests/sec)

4. Fallback Pitfalls:
   - "When is cached data dangerous?"
   - ```→ Never use stale prices in checkout → fail fast instead```

5. Resilience4J vs Spring Retry:
   - Resilience4J: Circuit breaker + metrics + better Spring Boot 3 support 
   - Spring Retry: Simpler but lacks advanced features
6. Why Resilience4J?
   
   | Feature       | Hystrix (Deprecated) | Resilience4J |
   |---------------|----------------------|--------------|
   | Active Support| ❌                  | ✅            |
   | Spring Boot 3 | ❌                  | ✅            |
   | Observability | Basic               | Advanced     |
