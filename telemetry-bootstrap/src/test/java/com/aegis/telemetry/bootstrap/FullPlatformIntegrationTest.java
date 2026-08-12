package com.aegis.telemetry.bootstrap;

import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.enums.RuntimeStatus;
import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.contracts.kafka.KafkaTopics;

import com.aegis.telemetry.publisher.core.RuntimeEventPublisher;
import com.aegis.telemetry.publisher.routing.RuntimeTopicRouter;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.trace.context.TraceContext;
import com.aegis.telemetry.trace.factory.TraceContextFactory;
import com.aegis.telemetry.trace.propagation.TraceHeaderPropagator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "aegis.runtime.service-name=telemetry-bootstrap-e2e",
        "aegis.runtime.instance-id=e2e-instance-01",
        "aegis.runtime.environment=e2e-test"
})
class FullPlatformIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RuntimeTelemetrySdk sdk;

    @Autowired
    private RuntimeEventPublisher publisher;

    @Autowired
    private ObjectMapper objectMapper;

    private RuntimeTopicRouter topicRouter;
    private TraceContextFactory traceContextFactory;
    private TraceHeaderPropagator traceHeaderPropagator;

    @BeforeEach
    void setUp() {
        restTemplate.getRestTemplate().getMessageConverters().stream()
                .filter(MappingJackson2HttpMessageConverter.class::isInstance)
                .map(MappingJackson2HttpMessageConverter.class::cast)
                .forEach(converter -> converter.getObjectMapper().registerModule(new JavaTimeModule()));

        topicRouter = new RuntimeTopicRouter();
        traceContextFactory = new TraceContextFactory("telemetry-bootstrap-e2e");
        traceHeaderPropagator = new TraceHeaderPropagator();
    }

    @Test
    void scenario1_normalRequestFlow() {
        ResponseEntity<RuntimeEvent> response = restTemplate.getForEntity("http://localhost:" + port + "/demo/trace", RuntimeEvent.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        RuntimeEvent event = response.getBody();
        assertThat(event).isNotNull();
        assertThat(event.eventType()).isEqualTo(RuntimeEventType.REQUEST_COMPLETED);
        assertThat(event.status()).isEqualTo(RuntimeStatus.SUCCESS);
        assertThat(event.serviceName()).isEqualTo("telemetry-bootstrap-e2e");
        assertThat(event.traceId()).isNotNull();

        String targetTopic = topicRouter.route(event);
        assertThat(targetTopic).isEqualTo(KafkaTopics.RUNTIME_EVENTS);
    }

    @Test
    void scenario2_distributedServiceCallContextPropagation() {
        TraceContext rootContext = traceContextFactory.createRootContext("gateway-service");
        Map<String, String> gatewayHeaders = new HashMap<>();
        traceHeaderPropagator.inject(rootContext, gatewayHeaders);

        Optional<TraceContext> extractedGateway = traceHeaderPropagator.extract(gatewayHeaders);
        assertThat(extractedGateway).isPresent();

        TraceContext orderContext = new TraceContextFactory("order-service").createChildContext(extractedGateway.get());

        assertThat(orderContext.traceId()).isEqualTo(rootContext.traceId());
        assertThat(orderContext.parentSpanId()).isEqualTo(rootContext.spanId());
        assertThat(orderContext.spanId()).isNotEqualTo(rootContext.spanId());

        RuntimeEvent event = RuntimeEvent.builder()
                .eventId(UUID.randomUUID())
                .traceId(UUID.fromString(orderContext.traceId()))
                .spanId(UUID.fromString(orderContext.spanId()))
                .parentSpanId(UUID.fromString(orderContext.parentSpanId()))
                .serviceName("order-service")
                .instanceId("order-001")
                .eventType(RuntimeEventType.SERVICE_CALL_COMPLETED)
                .timestamp(Instant.now())
                .threadName(Thread.currentThread().getName())
                .threadId(Thread.currentThread().threadId())
                .latency(45L)
                .status(RuntimeStatus.SUCCESS)
                .payload(Map.of("action", "createOrder"))
                .build();

        publisher.publish(event);
        assertThat(topicRouter.route(event)).isEqualTo(KafkaTopics.RUNTIME_EVENTS);
    }

    @Test
    void scenario3_retryScenario() {
        TraceContext traceContext = traceContextFactory.createRootContext("order-service");

        RuntimeEvent retryEvent = RuntimeEvent.builder()
                .eventId(UUID.randomUUID())
                .traceId(UUID.fromString(traceContext.traceId()))
                .spanId(UUID.fromString(traceContext.spanId()))
                .serviceName("order-service")
                .instanceId("order-001")
                .eventType(RuntimeEventType.RETRY_TRIGGERED)
                .timestamp(Instant.now())
                .threadName(Thread.currentThread().getName())
                .threadId(Thread.currentThread().threadId())
                .latency(150L)
                .status(RuntimeStatus.RETRYING)
                .payload(Map.of("attempt", 2, "maxAttempts", 3))
                .build();

        publisher.publish(retryEvent);
        assertThat(topicRouter.route(retryEvent)).isEqualTo(KafkaTopics.RUNTIME_RETRIES);
    }

    @Test
    void scenario4_failureScenarioWithStackTrace() {
        Exception cause = new IllegalStateException("Database pool exhausted");
        RuntimeException exception = new RuntimeException("Payment processing failed", cause);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String stackTrace = sw.toString();

        TraceContext traceContext = traceContextFactory.createRootContext("payment-service");

        RuntimeEvent errorEvent = RuntimeEvent.builder()
                .eventId(UUID.randomUUID())
                .traceId(UUID.fromString(traceContext.traceId()))
                .spanId(UUID.fromString(traceContext.spanId()))
                .serviceName("payment-service")
                .instanceId("payment-001")
                .eventType(RuntimeEventType.ERROR_OCCURRED)
                .timestamp(Instant.now())
                .threadName(Thread.currentThread().getName())
                .threadId(Thread.currentThread().threadId())
                .latency(850L)
                .status(RuntimeStatus.FAILED)
                .payload(Map.of(
                        "errorMessage", exception.getMessage(),
                        "stackTrace", stackTrace,
                        "exceptionClass", exception.getClass().getName()
                ))
                .build();

        publisher.publish(errorEvent);
        assertThat(topicRouter.route(errorEvent)).isEqualTo(KafkaTopics.RUNTIME_ERRORS);
    }

    @Test
    void scenario5_highLatencyScenario() {
        TraceContext traceContext = traceContextFactory.createRootContext("inventory-service");

        RuntimeEvent latencyEvent = RuntimeEvent.builder()
                .eventId(UUID.randomUUID())
                .traceId(UUID.fromString(traceContext.traceId()))
                .spanId(UUID.fromString(traceContext.spanId()))
                .serviceName("inventory-service")
                .instanceId("inventory-001")
                .eventType(RuntimeEventType.SERVICE_CALL_COMPLETED)
                .timestamp(Instant.now())
                .threadName(Thread.currentThread().getName())
                .threadId(Thread.currentThread().threadId())
                .latency(2450L) // 2.45s high latency
                .status(RuntimeStatus.TIMEOUT)
                .payload(Map.of("slowQuery", "SELECT * FROM inventory FOR UPDATE"))
                .build();

        publisher.publish(latencyEvent);
        assertThat(topicRouter.route(latencyEvent)).isEqualTo(KafkaTopics.RUNTIME_EVENTS);
    }

    @Test
    void scenario6_serviceRecoveryHeartbeat() {
        TraceContext traceContext = traceContextFactory.createRootContext("telemetry-bootstrap-e2e");

        RuntimeEvent heartbeatEvent = RuntimeEvent.builder()
                .eventId(UUID.randomUUID())
                .traceId(UUID.fromString(traceContext.traceId()))
                .spanId(UUID.fromString(traceContext.spanId()))
                .serviceName("telemetry-bootstrap-e2e")
                .instanceId("e2e-instance-01")
                .eventType(RuntimeEventType.HEARTBEAT)
                .timestamp(Instant.now())
                .threadName(Thread.currentThread().getName())
                .threadId(Thread.currentThread().threadId())
                .latency(0L)
                .status(RuntimeStatus.SUCCESS)
                .payload(Map.of("status", "HEALTHY", "uptimeMs", 120000L))
                .build();

        publisher.publish(heartbeatEvent);
        assertThat(topicRouter.route(heartbeatEvent)).isEqualTo(KafkaTopics.RUNTIME_HEARTBEATS);
    }
}
