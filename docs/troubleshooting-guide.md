# Aegis Telemetry Troubleshooting Guide

## Common Issues & Solutions

### 1. Kafka Connection Timeout (`org.apache.kafka.common.errors.TimeoutException`)

#### Symptom:
`WARN com.aegis.telemetry.publisher.core.RuntimeEventPublisher -- Failed to publish runtime event ERROR_OCCURRED after 0 retries`

#### Cause:
Kafka broker is offline or unreachable at the configured address (`localhost:19092` or `kafka:19092`).

#### Solution:
- **Resilient Fallback**: The host application continues to function normally without crashing.
- Verify Kafka container status: `docker-compose ps`
- Ensure property `aegis.kafka.bootstrap-servers` points to a reachable broker address.

---

### 2. Jackson `InvalidDefinitionException`: Java 8 Date/Time `java.time.Instant` Not Supported

#### Symptom:
`HttpMessageConversionType definition error: [simple type, class java.time.Instant]`

#### Cause:
Jackson `ObjectMapper` lacks the `JavaTimeModule` module registration for `java.time.Instant`.

#### Solution:
- Register `JavaTimeModule` on your `ObjectMapper` bean:
  ```java
  ObjectMapper mapper = new ObjectMapper();
  mapper.registerModule(new JavaTimeModule());
  mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  ```
- Ensure `com.fasterxml.jackson.datatype:jackson-datatype-jsr310` is present in your classpath.

---

### 3. Missing Parent Span ID in Downstream Traces

#### Symptom:
Downstream service creates a new root trace instead of linking as a child span.

#### Cause:
Outbound HTTP client did not inject `X-Aegis-Trace-Id` or `X-Aegis-Span-Id` headers into request.

#### Solution:
- Verify `TraceHeaderPropagator.inject(context, headers)` is registered in your `RestClient` or `RestTemplate` request interceptors.
- Ensure `TraceContextHolder.clear()` is called in an `@AfterEach` or HTTP filter `finally` block to prevent `ThreadLocal` context leaks.

---

### 4. Actuator Health Reporting 503 Service Unavailable

#### Symptom:
`GET /actuator/health` returns HTTP 503 when Kafka is offline.

#### Solution:
- The platform uses `KafkaPublisherHealthIndicator` with resilient fallback.
- In test environments, verify `BootstrapConfiguration` runs before `RuntimeSdkAutoConfiguration` via `@AutoConfigureBefore`.
