# Aegis Telemetry SDK Integration Guide

## Overview
This guide explains how to integrate the **Aegis Telemetry SDK** into any new Spring Boot microservice to enable automatic HTTP request tracing, `@Monitored` method timing, exception tracking, and Kafka event publishing.

---

## Step 1: Add Maven Dependencies

Add `runtime-sdk` and `instrumentation-engine` to your `pom.xml`:

```xml
<dependency>
    <groupId>com.aegis.telemetry</groupId>
    <artifactId>runtime-sdk</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.aegis.telemetry</groupId>
    <artifactId>instrumentation-engine</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

---

## Step 2: Add Configuration Properties

In your `application.yml`:

```yaml
aegis:
  runtime:
    service-name: my-custom-service
    instance-id: instance-01
    environment: production
    sampling-rate: 1.0
    capture-payload: true
  kafka:
    bootstrap-servers: localhost:19092
```

---

## Step 3: Enable Method & HTTP Interception

### 1. Automatic HTTP Request Filtering
The `TelemetryHttpFilter` automatically intercepts incoming requests, extracts `X-Aegis-Trace-Id` headers, tracks execution duration, and emits `REQUEST_STARTED` and `REQUEST_COMPLETED` events.

### 2. Annotation-Based Method Monitoring
Annotate custom service methods with `@Monitored`:

```java
package com.mycompany.service;

import com.aegis.telemetry.instrumentation.aop.Monitored;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Monitored(name = "processOrder")
    public OrderResult processOrder(OrderRequest request) {
        // Business logic here
        return new OrderResult("SUCCESS");
    }
}
```

---

## Step 4: Propagate Trace Headers in Outbound RestClient

When making outbound REST calls to downstream services, propagate trace context:

```java
@Bean
RestClient orderRestClient(RestClient.Builder builder) {
    return builder
        .requestInterceptor((request, body, execution) -> {
            TraceContextHolder.getContext().ifPresent(context -> {
                request.getHeaders().set(TraceHeaders.TRACE_ID, context.traceId());
                request.getHeaders().set(TraceHeaders.SPAN_ID, context.spanId());
                if (context.parentSpanId() != null) {
                    request.getHeaders().set(TraceHeaders.PARENT_SPAN_ID, context.parentSpanId());
                }
            });
            return execution.execute(request, body);
        })
        .build();
}
```
