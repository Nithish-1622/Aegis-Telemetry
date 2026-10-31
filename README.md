# Aegis Telemetry

Production-oriented telemetry generation for the Aegis platform.

Aegis Telemetry is a Java 25, Spring Boot 3.5.0 platform for generating distributed runtime events from applications and services. It provides trace propagation, HTTP and method instrumentation, event validation and serialization, Kafka publishing, service registration, heartbeats, and live runtime configuration.

The platform is the event-generation side of Aegis. Aegis Runtime consumes these events for runtime intelligence, analytics, dependency reconstruction, and anomaly detection. The two systems communicate through versioned Kafka contracts rather than direct application calls.

## Release Status

| Item | Value |
| --- | --- |
| Current development version | `0.1.0-SNAPSHOT` |
| Target release | `1.0.0` |
| Java | 25 |
| Spring Boot | 3.5.0 |
| Build system | Maven multi-module reactor |
| Primary artifact registry | GitHub Packages |
| Kafka topics | `runtime.events`, `runtime.errors`, `runtime.retries`, `runtime.heartbeats` |
| Default bootstrap port | `9085` |

The APIs and package coordinates should be treated as pre-1.0 until the first tagged release. Review the release checklist before publishing `v1.0.0`.

## Capabilities

- Distributed trace and span identifiers with HTTP header propagation.
- Automatic HTTP request lifecycle events.
- Annotation-based method monitoring with `@Monitored`.
- Exception and failure event generation.
- Runtime event validation and Java time aware JSON serialization.
- Kafka topic routing, retries, batching, and dead-letter publishing.
- Service registration, heartbeat processing, stale-instance detection, and health classification.
- Dynamic runtime configuration without restarting the host application.
- Spring Boot auto-configuration for SDK and instrumentation components.
- A runnable bootstrap application for platform verification.

## Architecture

```text
Application code
      |
      v
Instrumentation Engine
      |
      v
Runtime SDK ---- Trace Engine
      |
      v
Shared Contracts
      |
      v
Event Publisher ---- Kafka
      |
      +---- runtime.events
      +---- runtime.errors
      +---- runtime.retries
      +---- runtime.heartbeats

Service Registry and Runtime Configuration operate alongside the SDK and update
service identity, liveness, sampling, and capture behavior at runtime.
```

### System boundary

Aegis Telemetry generates and publishes events. It does not call Aegis Runtime directly. Both systems must use the shared event contracts and compatible topic definitions.

## Modules

| Module | Purpose | Published |
| --- | --- | --- |
| `shared-contracts` | Immutable event DTOs, enums, Kafka topics, and trace header constants | Yes |
| `trace-engine` | Trace context creation, storage, and propagation | Yes |
| `runtime-sdk` | SDK facade, runtime state, event creation, validation, and serialization | Yes |
| `instrumentation-engine` | HTTP filtering, `@Monitored` interception, timing, metadata, and exception capture | Yes |
| `event-publisher` | Kafka publishers, topic routing, batching, retries, and dead-letter handling | Yes |
| `service-registry` | Service registration, heartbeats, lifecycle, and health monitoring | Yes |
| `runtime-config` | Externalized and live runtime configuration | Yes |
| `telemetry-bootstrap` | Runnable Spring Boot integration and verification application | No |
| `failure-simulator` | Failure and performance test harness | No |
| `demo-services` | Reference distributed services for local demonstrations | No |

The parent POM and reusable library modules are deployed to the registry. Executable applications and test harnesses remain source-level modules and are intentionally excluded from package publication.

## Requirements

- JDK 25.
- Maven 3.9 or newer, or the Maven Wrapper in dependent projects.
- Docker Desktop if running Kafka, PostgreSQL, or the complete local stack.
- A Kafka broker for event publishing.
- GitHub Packages credentials when consuming or publishing package artifacts.

Verify the Java and Maven installations:

```powershell
java -version
mvn -version
```

## Build and Test

Run the complete reactor from the repository root:

```powershell
mvn clean test
```

Build the bootstrap application and all required modules:

```powershell
mvn -pl telemetry-bootstrap -am package
```

Run a focused module build:

```powershell
mvn -pl runtime-sdk -am test
mvn -pl event-publisher -am test
mvn -pl service-registry,runtime-config -am test
```

The build targets Java release 25. A lower JDK cannot compile this project even if Maven itself starts successfully.

## Run the Bootstrap Application

The bootstrap application assembles the SDK, tracing, instrumentation, event publisher, registry, and runtime configuration modules.

Build and run the executable JAR:

```powershell
mvn -pl telemetry-bootstrap -am package
java -jar telemetry-bootstrap\target\telemetry-bootstrap-0.1.0-SNAPSHOT.jar
```

The default HTTP port is `9085`. Override it with a Spring property:

```powershell
java -jar telemetry-bootstrap\target\telemetry-bootstrap-0.1.0-SNAPSHOT.jar --server.port=9090
```

The Maven Spring Boot plugin can also be used after the reactor artifacts have been built:

```powershell
mvn -pl telemetry-bootstrap -am spring-boot:run
```

## Local Infrastructure

Start the repository's local infrastructure and reference services with Docker Compose:

```powershell
docker compose up --build -d
```

Stop the stack:

```powershell
docker compose down
```

The bootstrap configuration expects Kafka at `localhost:9092` by default. Change the broker address using `aegis.kafka.bootstrap-servers` or the corresponding environment-specific configuration.

## Configuration

The default bootstrap configuration is in `telemetry-bootstrap/src/main/resources/application.yml`.

```yaml
server:
  port: 9085

spring:
  application:
    name: aegis-telemetry

aegis:
  kafka:
    bootstrap-servers: localhost:9092
  runtime:
    service-name: telemetry-bootstrap
    instance-id: telemetry-001
    environment: bootstrap
    sampling-rate: 1.0
    capture-payload: false
    capture-thread-info: false
    stack-trace-enabled: true
    maximum-payload-size: 65536
  registry:
    default-version: 1.0.0
    host: localhost
    port: 9085
    heartbeat-interval-ms: 30000
    heartbeat-timeout-ms: 90000
```

### Runtime configuration guidance

Use a unique `instance-id` for every running process. Keep payload capture disabled unless the payload is known to be safe to collect. Use sampling to control event volume in production, and set a bounded maximum payload size.

The runtime configuration service can update SDK behavior while the application is running. Configuration changes should be validated and audited by the host application before being applied in production.

### Environment variables

Spring Boot relaxed binding can be used in deployment environments:

```text
SERVER_PORT=9085
AEGIS_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
AEGIS_RUNTIME_SERVICE_NAME=orders-service
AEGIS_RUNTIME_INSTANCE_ID=orders-01
AEGIS_RUNTIME_ENVIRONMENT=production
AEGIS_RUNTIME_SAMPLING_RATE=0.25
AEGIS_RUNTIME_CAPTURE_PAYLOAD=false
```

## Integrating the SDK

Add the repository and dependencies to a Spring Boot service. The package registry requires authentication, even when the package is readable to your account.

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Nithish-1622/Aegis-Telemetry</url>
    </repository>
</repositories>

<dependencies>
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
    <dependency>
        <groupId>com.aegis.telemetry</groupId>
        <artifactId>event-publisher</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

The auto-configuration imports the platform components when the corresponding modules are on the classpath. Configure the service identity and Kafka connection in the host application's `application.yml`.

### Monitor application methods

```java
import com.aegis.telemetry.instrumentation.aop.Monitored;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Monitored(name = "processOrder")
    public OrderResult processOrder(OrderRequest request) {
        return new OrderResult("SUCCESS");
    }
}
```

### Propagate trace context

Use the standard Aegis headers for downstream HTTP requests:

```text
X-Aegis-Trace-Id
X-Aegis-Span-Id
X-Aegis-Parent-Span-Id
X-Aegis-Sampled
```

For a complete integration example, see [docs/integration-guide.md](docs/integration-guide.md).

## Kafka Contracts

| Topic | Event categories |
| --- | --- |
| `runtime.events` | Request, service-call, configuration, and metric lifecycle events |
| `runtime.errors` | Exceptions and failures |
| `runtime.retries` | Retry and recovery attempts |
| `runtime.heartbeats` | Service liveness and runtime state |

The canonical event model is in `shared-contracts`. Consumers should depend on that module instead of duplicating event DTOs.

## Verification Endpoints

When `telemetry-bootstrap` is running:

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/demo/ping` | Generates a request-start event and returns trace/event identifiers |
| `GET` | `/demo/trace` | Generates a traced request event with timing information |
| `GET` | `/demo/error` | Exercises error event generation and returns HTTP 500 |
| `GET` | `/actuator/health` | Reports application health |
| `GET` | `/actuator/info` | Reports application information |

Example:

```powershell
Invoke-RestMethod http://localhost:9085/demo/ping
Invoke-RestMethod http://localhost:9085/actuator/health
```

The verification endpoints are intended for local validation and integration testing. They are not a replacement for application-specific authorization, rate limiting, or production business endpoints.

## Observability and Operations

For production deployments:

1. Provide Kafka through a highly available cluster and configure authentication and encryption at the deployment layer.
2. Use a stable service name and unique instance identifier per process.
3. Keep payload capture disabled by default and review data protection requirements before enabling it.
4. Configure sampling and payload limits according to traffic volume and storage budgets.
5. Expose actuator endpoints only through an authenticated management network.
6. Monitor publisher failures, retry exhaustion, dead-letter traffic, Kafka lag, and heartbeat freshness.
7. Treat event schema changes as compatibility-sensitive releases.
8. Use immutable release versions in production; reserve `SNAPSHOT` versions for development.

## Performance Baselines

The repository includes performance tests and reports the following development baselines:

| Metric | Reported baseline | Target |
| --- | ---: | ---: |
| Event creation latency | 4.73 microseconds/event | Under 100 microseconds/event |
| JSON serialization throughput | 82,616 operations/second | Over 1,000 operations/second |
| Batch publisher burst rate | 13,021 events/second | Over 1,000 events/second |

These are environment-dependent measurements, not service-level guarantees. Re-run the benchmark suite on the target JDK, hardware, Kafka configuration, and workload before capacity planning.

## Published Packages

Aegis Telemetry is published to GitHub Packages under:

```text
https://maven.pkg.github.com/Nithish-1622/Aegis-Telemetry
```

Current development coordinates:

```text
com.aegis.telemetry:shared-contracts:0.1.0-SNAPSHOT
com.aegis.telemetry:trace-engine:0.1.0-SNAPSHOT
com.aegis.telemetry:runtime-sdk:0.1.0-SNAPSHOT
com.aegis.telemetry:instrumentation-engine:0.1.0-SNAPSHOT
com.aegis.telemetry:event-publisher:0.1.0-SNAPSHOT
com.aegis.telemetry:service-registry:0.1.0-SNAPSHOT
com.aegis.telemetry:runtime-config:0.1.0-SNAPSHOT
```

The reusable libraries include sources and Javadoc artifacts. The bootstrap application, demo services, and failure simulator are not deployed as reusable library packages.

### Local publishing

Configure `%USERPROFILE%\.m2\settings.xml` with a GitHub server entry whose id is `github`, then run:

```powershell
Set-Location K:\Aegis-Telementry
mvn clean deploy
```

For the complete two-repository publication process, including Aegis Runtime, see [docs/publishing-guide.md](docs/publishing-guide.md).

### Release process

1. Update versions and compatibility notes.
2. Run `mvn clean test` from the repository root.
3. Review the generated artifacts and dependency tree.
4. Publish a release tag such as `v1.0.0`.
5. Verify the package is resolvable from an empty Maven repository.
6. Publish release notes with API, schema, and configuration changes.

The repository workflow in `.github/workflows/publish.yml` publishes snapshots from `main` and release versions from version tags. GitHub Actions uses the repository `GITHUB_TOKEN` with package write permission.

## Repository Layout

```text
shared-contracts/       Public event and protocol contracts
trace-engine/           Trace context and propagation
runtime-sdk/            SDK facade and event lifecycle
instrumentation-engine/ Automatic application instrumentation
event-publisher/        Kafka publishing and retry behavior
service-registry/       Service lifecycle and health
runtime-config/         Live configuration management
telemetry-bootstrap/    Runnable platform bootstrap application
failure-simulator/      Failure and performance harness
demo-services/          Local distributed-service examples
docs/                   Architecture, integration, deployment, and publishing guides
```

## Documentation

- [Architecture and data flow](docs/architecture-diagram.md)
- [Deployment guide](docs/deployment-guide.md)
- [Developer setup guide](docs/developer-setup-guide.md)
- [SDK integration guide](docs/integration-guide.md)
- [Publishing guide](docs/publishing-guide.md)
- [Troubleshooting guide](docs/troubleshooting-guide.md)

## License and Ownership

This project is part of the Aegis platform suite. Package ownership, licensing, and redistribution terms should be finalized before the `v1.0.0` public release.
