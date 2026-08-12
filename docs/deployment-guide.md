# Aegis Telemetry Deployment Guide

## Overview
This document covers deployment strategies for **Aegis Telemetry**, including single-command Docker Compose orchestration and standalone JAR execution.

---

## 1. Docker Compose Stack Deployment

The complete platform stack (Zookeeper, Kafka, PostgreSQL, Telemetry Bootstrap, and Demo Services) can be deployed using Docker Compose.

### Start the Entire Platform
```bash
docker-compose up --build -d
```

### Check Container Status
```bash
docker-compose ps
```

### Expected Running Containers
- `aegis-zookeeper` (port 2181)
- `aegis-kafka` (ports 9092, 19092)
- `aegis-postgres` (port 5432)
- `aegis-telemetry-bootstrap` (port 8080)
- `aegis-gateway-service` (port 8081)
- `aegis-order-service` (port 8082)
- `aegis-payment-service` (port 8083)
- `aegis-inventory-service` (port 8084)

### View Application Logs
```bash
docker-compose logs -f telemetry-bootstrap
```

### Stop and Clean Up Stack
```bash
docker-compose down -v
```

---

## 2. Standalone JAR Deployment

### Step 1: Package Executable Artifact
```bash
mvn clean package -pl telemetry-bootstrap -am -DskipTests
```

### Step 2: Configure Environment Variables
```bash
export AEGIS_KAFKA_BOOTSTRAP_SERVERS="kafka.internal.prod:9092"
export AEGIS_RUNTIME_SERVICE_NAME="telemetry-prod"
export AEGIS_RUNTIME_ENVIRONMENT="production"
export SERVER_PORT=8080
```

### Step 3: Run the Application
```bash
java -Xms512m -Xmx2g -jar telemetry-bootstrap/target/telemetry-bootstrap-0.1.0-SNAPSHOT.jar
```

---

## 3. Production Health Monitoring

Verify application readiness and health via Spring Boot Actuator:
```bash
curl http://localhost:8080/actuator/health
```

Expected Output:
```json
{
  "status": "UP",
  "components": {
    "kafkaPublisher": {
      "status": "UP"
    }
  }
}
```
