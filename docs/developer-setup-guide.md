# Aegis Telemetry Developer Setup Guide

## Prerequisites
- **JDK 25** (Oracle JDK 25 or Eclipse Temurin 25)
- **Maven 3.9+**
- **Git**
- **IDE**: IntelliJ IDEA 2025+ or VS Code with Java Extension Pack

---

## Environment Setup

### 1. Clone the Repository
```bash
git clone https://github.com/Nithish-1622/Aegis-Telemetry.git
cd Aegis-Telemetry
```

### 2. Verify Java 25 Compiler Configuration
```bash
java -version
mvn -version
```
Ensure Maven compiler output points to release `25`.

### 3. Build Reactor and Install Artifacts
To compile all sub-modules and install them into your local `~/.m2` cache:
```bash
mvn clean install
```

### 4. Run Unit and Integration Tests
```bash
mvn clean test
```

---

## IDE Setup (IntelliJ IDEA / VS Code)

1. Open project root `K:\Aegis-Telemetry`.
2. Set SDK to **Java 25**.
3. Enable annotation processing: `Settings -> Build, Execution, Deployment -> Compiler -> Annotation Processors -> Enable`.
4. Run `TelemetryBootstrapApplication.java` main method directly from your IDE.

---

## Local Verification Commands

Test telemetry endpoints via `curl`:
```bash
# Ping Endpoint (REQUEST_STARTED event)
curl http://localhost:8080/demo/ping

# Trace Endpoint (REQUEST_COMPLETED event)
curl http://localhost:8080/demo/trace

# Error Endpoint (ERROR_OCCURRED event with stack trace)
curl http://localhost:8080/demo/error
```
