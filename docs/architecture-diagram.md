# Aegis Platform Architecture & Data Flow Diagram

## High-Level System Architecture

The **Aegis Platform** is decoupled into two primary systems:
1. **Person 1 System (`Aegis-Telemetry`)**: Generates runtime telemetry, traces HTTP requests, tracks exceptions, and publishes structured events.
2. **Person 2 System (`Aegis-Runtime`)**: Consumes events from Kafka, reconstructs request timelines, builds service dependency graphs, computes runtime analytics, and detects anomalies.

```mermaid
flowchart TD
    subgraph Host Application / Microservices
        A[Gateway Service] -->|HTTP REST + Trace Headers| B[Order Service]
        B -->|HTTP REST + Trace Headers| C[Payment Service]
        B -->|HTTP REST + Trace Headers| D[Inventory Service]
        
        A -.->|Intercept & Monitored| SDK1[Aegis Telemetry SDK]
        B -.->|Intercept & Monitored| SDK2[Aegis Telemetry SDK]
        C -.->|Intercept & Monitored| SDK3[Aegis Telemetry SDK]
        D -.->|Intercept & Monitored| SDK4[Aegis Telemetry SDK]
    end

    subgraph Aegis Telemetry Core (Person 1)
        SDK1 & SDK2 & SDK3 & SDK4 --> TE[Trace Engine]
        TE --> IE[Instrumentation Engine]
        IE --> EP[Kafka Event Publisher]
    end

    subgraph Kafka Event Bus
        EP -->|runtime.events| K1[(Topic: runtime.events)]
        EP -->|runtime.errors| K2[(Topic: runtime.errors)]
        EP -->|runtime.retries| K3[(Topic: runtime.retries)]
        EP -->|runtime.heartbeats| K4[(Topic: runtime.heartbeats)]
    end

    subgraph Aegis Runtime Intelligence (Person 2)
        K1 & K2 & K3 & K4 --> ING[Event Ingestor]
        ING --> DB[(PostgreSQL Database)]
        DB --> TLE[Timeline Reconstruction Engine]
        DB --> GRE[Graph & Dependency Engine]
        DB --> ALE[Analytics & Anomaly Engine]
    end
```

---

## Data Model & Trace Context Propagation

### Trace Header Format
```http
X-Aegis-Trace-Id: 9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d
X-Aegis-Span-Id: 1024a18c-8f92-4d9a-9861-123456789abc
X-Aegis-Parent-Span-Id: 00000000-0000-0000-0000-000000000001
X-Aegis-Sampled: true
```

### Event Routing Table

| Runtime Event Type | Target Kafka Topic | Primary Payload Content |
| :--- | :--- | :--- |
| `REQUEST_STARTED`, `REQUEST_COMPLETED`, `SERVICE_CALL_COMPLETED` | `runtime.events` | Latency, Status, Endpoint, Method |
| `ERROR_OCCURRED` | `runtime.errors` | Exception Class, Error Message, Full Stack Trace |
| `RETRY_TRIGGERED` | `runtime.retries` | Attempt Count, Delay, Cause |
| `HEARTBEAT` | `runtime.heartbeats` | Instance Status, Uptime, Memory/Thread Stats |
