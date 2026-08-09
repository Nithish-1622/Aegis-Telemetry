# Aegis Telemetry Platform

[![Java Version](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-green.svg)](https://spring.io/projects/spring-boot)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()
[![Phase Status](https://img.shields.io/badge/Phase-9%20Completed-blue.svg)]()

**Aegis Telemetry** is the high-performance telemetry generation platform for the Aegis suite (**Person 1 system**). Built as a bounded, multi-module Maven reactor in **Java 25** and **Spring Boot 3.5.0**, it provides end-to-end distributed tracing, runtime behavior instrumentation, service registry integration, dynamic runtime configuration, resilient event publishing to Kafka streams, and complete platform integration.

---

## 🏛 Architecture & System Boundary

The Aegis architecture strictly enforces a boundary between event generation (**Person 1: Aegis Telemetry**) and runtime intelligence analytics (**Person 2: Aegis Runtime**):

- **Decoupled Messaging**: Person 1 publishes Kafka events exclusively; it never directly invokes Person 2 REST or RPC APIs.
- **Contract-Driven**: All messaging adheres strictly to standard contracts defined in `shared-contracts`.
- **Kafka Topic Specifications**:
  - `runtime.events`: Lifecycle events (`REQUEST_STARTED`, `REQUEST_COMPLETED`, `SERVICE_CALL_STARTED`, `SERVICE_CALL_COMPLETED`, `CONFIG_UPDATED`, `METRIC_SAMPLED`).
  - `runtime.errors`: Exception and failure events (`ERROR_OCCURRED`).
  - `runtime.retries`: Fault recovery and retry attempt events (`RETRY_TRIGGERED`).
  - `runtime.heartbeats`: Periodical instance health and state heartbeats (`HEARTBEAT`).

---

## 🧩 Module Overview

The platform is structured into 10 modular, loosely-coupled components:

| Module | Core Responsibility | Key Components / Features |
| :--- | :--- | :--- |
| [`shared-contracts`](file:///k:/Aegis-Telementry/shared-contracts) | Immutable DTOs & Kafka/Header Constants | `RuntimeEvent`, `RuntimeStatus`, `RuntimeEventType`, `KafkaTopics`, `TraceHeaders` |
| [`trace-engine`](file:///k:/Aegis-Telementry/trace-engine) | 128-bit Trace & 64-bit Span Context Management | `TraceIdGenerator`, `SpanIdGenerator`, `TraceContext`, `TraceContextHolder`, `TraceHeaderPropagator` |
| [`runtime-sdk`](file:///k:/Aegis-Telementry/runtime-sdk) | Core Developer SDK Facade & Validation | `RuntimeTelemetrySdk`, `RuntimeEventBuilder`, `RuntimeEventValidator`, `RuntimeEventSerializer` |
| [`instrumentation-engine`](file:///k:/Aegis-Telementry/instrumentation-engine) | Automatic Interception & Exception Tracking | `@Monitored` AOP Aspect, `TelemetryHttpFilter`, `RuntimeExceptionHandler`, `ExecutionTimer` |
| [`event-publisher`](file:///k:/Aegis-Telementry/event-publisher) | Resilient Kafka Event Publishing | `KafkaTelemetryPublisher`, `BatchingTelemetryPublisher`, `RuntimeTopicRouter`, `PublishRetryPolicy`, `DeadLetterPublisher` |
| [`service-registry`](file:///k:/Aegis-Telementry/service-registry) | Service Instance Health & Heartbeat Bridge | `ServiceRegistryClient`, `HeartbeatService`, `ServiceHealthMonitor`, `RegistryIntegrationBridge` |
| [`runtime-config`](file:///k:/Aegis-Telementry/runtime-config) | Dynamic Live Configuration Management | `TelemetryConfigManager`, `DynamicConfigListener`, `@ConfigurationProperties` Binding |
| [`telemetry-bootstrap`](file:///k:/Aegis-Telementry/telemetry-bootstrap) | Runnable Spring Boot Bootstrap Application | `TelemetryBootstrapApplication`, `TelemetryVerificationController`, `StartupVerificationRunner`, `FullPlatformIntegrationTest` |
| [`failure-simulator`](file:///k:/Aegis-Telementry/failure-simulator) | Resiliency & Performance Benchmarking | `TelemetryFailureSimulator`, `PlatformPerformanceBenchmarkTest`, `FailureSimulatorTest` |
| [`demo-services`](file:///k:/Aegis-Telementry/demo-services) | Distributed Microservice Tracing Reference | `gateway-service`, `order-service`, `payment-service`, `inventory-service` |

---

## ⚡ Quick Start & Docker Deployment

### 1. Docker Compose Single-Command Full-Stack Run
Start the entire Aegis telemetry ecosystem (Zookeeper, Kafka, PostgreSQL, Telemetry Bootstrap, and Gateway/Order/Payment/Inventory demo microservices):

```bash
docker-compose up --build -d
```

### 2. Maven Build & Reactor Testing
To compile all 15 modules and execute unit, integration, and performance tests:
```bash
mvn clean test
```

### 3. Standalone JAR Execution
Build the executable fat JAR and launch:
```bash
mvn -pl telemetry-bootstrap -am package
java -jar telemetry-bootstrap/target/telemetry-bootstrap-0.1.0-SNAPSHOT.jar
```

---

## 📊 Phase 9 Verified Performance Baselines

Performance metrics collected via `PlatformPerformanceBenchmarkTest`:

| Benchmark Metric | Measured Baseline | Target Threshold | Status |
| :--- | :--- | :--- | :--- |
| **Event Creation Latency** | **4.73 μs** / event | $< 100$ μs / event | **PASSED** |
| **JSON Serialization Throughput** | **82,616 ops** / sec | $> 1,000$ ops / sec | **PASSED** |
| **Batch Publisher Burst Rate** | **13,021 events** / sec | $> 1,000$ events / sec | **PASSED** |

---

## 🌐 Endpoints & Verification

Once `telemetry-bootstrap` is running (default port `8085`), you can exercise and verify event generation and publishing via the following REST endpoints:

### Verification Controller (`/demo`)
- **`GET /demo/ping`**: Emits `REQUEST_STARTED` event to Kafka and returns HTTP 200 with generated `traceId` and `eventId`.
- **`GET /demo/trace`**: Emits `REQUEST_COMPLETED` event with timing statistics and returns the full `RuntimeEvent` DTO.
- **`GET /demo/error`**: Simulates an exception, extracts stack trace info, emits `ERROR_OCCURRED` event, and returns HTTP 500.

### Actuator Health & Metrics (`/actuator`)
- **`GET /actuator/health`**: Returns application health status and Kafka publisher metrics.

---

## 🔍 Distributed Tracing Headers

Across HTTP REST boundaries, trace context is propagated via standardized headers:

```http
X-Aegis-Trace-Id: 9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d
X-Aegis-Span-Id: 1024a18c-8f92-4d9a-9861-123456789abc
X-Aegis-Parent-Span-Id: 00000000-0000-0000-0000-000000000001
X-Aegis-Sampled: true
```

---

## 📮 Postman Collection

A complete Postman Collection is provided in [`Postman.json`](file:///k:/Aegis-Telementry/Postman.json).

### How to Import & Use:
1. Open **Postman**.
2. Click **Import** $\rightarrow$ select [`Postman.json`](file:///k:/Aegis-Telementry/Postman.json).
3. The collection includes pre-configured environment variables:
   - `telemetry_url`: `http://localhost:8085` (Person 1)
   - `runtime_url`: `http://localhost:8080` (Person 2)
   - `gateway_url`: `http://localhost:8081` (Demo Microservices)

---

## 📚 Comprehensive System Documentation

Detailed technical documentation is available in the [`docs/`](file:///k:/Aegis-Telementry/docs) directory:

- 📐 [**Architecture & Data Flow Diagram**](file:///k:/Aegis-Telementry/docs/architecture-diagram.md) - System boundaries, sequence flows, and topic routing.
- 🚀 [**Deployment Guide**](file:///k:/Aegis-Telementry/docs/deployment-guide.md) - Docker Compose stack and production execution.
- 🛠 [**Developer Setup Guide**](file:///k:/Aegis-Telementry/docs/developer-setup-guide.md) - Local Java 25 environment setup and build instructions.
- 🔌 [**SDK Integration Guide**](file:///k:/Aegis-Telementry/docs/integration-guide.md) - How to integrate Aegis Telemetry SDK into new Spring Boot microservices.
- 🔧 [**Troubleshooting Guide**](file:///k:/Aegis-Telementry/docs/troubleshooting-guide.md) - Diagnosing connection timeouts, missing headers, and serialization issues.

---

## 📄 License & Ownership

This project is part of the Aegis Telemetry platform suite (**Person 1 System**). All code lives under the approved `com.aegis.telemetry` package hierarchy.
