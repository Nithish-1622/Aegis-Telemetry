# Phase 0 Architecture Contract

## Scope

This repository is the Person 1 telemetry generation system for Aegis.
It establishes structure, ownership boundaries, and integration contracts only.
No business logic, SDK functionality, or Kafka producer implementation exists in Phase 0.

## System Boundary

Person 1 must not call Person 2 APIs.
Person 1 publishes Kafka events only.
Person 2 consumes Kafka events only.

Kafka topics are stable contracts and must not change:

- `runtime.events`
- `runtime.errors`
- `runtime.retries`
- `runtime.heartbeats`

## Module Ownership

- `shared-contracts`: common DTOs, event contracts, validation primitives, and shared constants.
- `trace-engine`: trace and span lifecycle modeling.
- `runtime-sdk`: reusable developer-facing SDK package.
- `instrumentation-engine`: instrumentation abstractions and interception points.
- `event-publisher`: outbound event publishing boundary.
- `service-registry`: service registration and discovery contract boundary.
- `runtime-config`: typed configuration model and property binding.
- `failure-simulator`: simulated failure scenarios for validation.
- `demo-services`: sample distributed services used to exercise the telemetry stack.

## Package Boundaries

All code in this repository must live under `com.aegis.telemetry`.
Do not use `com.aegis.runtime` anywhere in this repository.

Approved package roots:

- `com.aegis.telemetry.sdk`
- `com.aegis.telemetry.trace`
- `com.aegis.telemetry.publisher`
- `com.aegis.telemetry.instrumentation`
- `com.aegis.telemetry.registry`
- `com.aegis.telemetry.simulator`
- `com.aegis.telemetry.config`
- `com.aegis.telemetry.common`
- `com.aegis.telemetry.contracts`

## Coding Conventions

- Java 25
- Spring Boot
- Maven multi-module build
- constructor injection only
- immutable DTOs where practical
- Jakarta Validation
- SLF4J logging
- Spring `@ConfigurationProperties`
- record classes for DTOs where appropriate
- no field injection
- no circular dependencies
- no duplicated event models

## Design Principles

Follow:

- Single Responsibility Principle
- Dependency Injection
- Layered Architecture
- Package-by-Feature
- Loose Coupling
- High Cohesion

Business logic must stay out of controllers.
Modules should communicate through interfaces.

## Compatibility Rules

Any `RuntimeEvent` produced later in Person 1 must remain directly consumable by Person 2 without changes.
Stable contract fields include trace IDs, span IDs, event types, and Kafka topic names.

## Phase Order

1. Shared Contracts
2. Trace Engine
3. Runtime SDK
4. Instrumentation Engine
5. Kafka Event Publisher
6. Service Registry
7. Trace Context
8. Runtime Configuration
9. Failure Simulator
10. Demo Distributed Services

No later phase should require redesign of an earlier phase.
