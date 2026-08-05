package com.aegis.telemetry.instrumentation.integration;

import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.enums.RuntimeStatus;
import com.aegis.telemetry.instrumentation.metadata.RuntimeMetadataProvider;
import com.aegis.telemetry.instrumentation.thread.ThreadMetadataProvider;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import com.aegis.telemetry.trace.context.TraceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeInstrumentationBridgeTest {

    private final RuntimeTelemetrySdk sdk = RuntimeTelemetrySdk.getInstance();

    @AfterEach
    void tearDown() {
        sdk.shutdown();
    }

    @Test
    void generatesRuntimeEventsUsingSdkAndTraceContext() {
        sdk.initialize(RuntimeSdkConfiguration.builder()
                .applicationName("order-service")
                .instanceId("instance-1")
                .environment("test")
                .heartbeatInterval(Duration.ofSeconds(30))
                .build());

        RuntimeInstrumentationBridge bridge = new RuntimeInstrumentationBridge(sdk, new RuntimeMetadataProvider(sdk), new ThreadMetadataProvider());
        TraceContext traceContext = TraceContext.builder()
                .traceId(UUID.fromString("11111111-1111-1111-1111-111111111111").toString())
                .spanId(UUID.fromString("22222222-2222-2222-2222-222222222222").toString())
                .parentSpanId(UUID.fromString("33333333-3333-3333-3333-333333333333").toString())
                .serviceName("order-service")
                .createdAt(Instant.parse("2026-07-21T00:00:00Z"))
                .build();

        assertThat(bridge.createRequestStarted(traceContext, "GET", "/orders/1", "127.0.0.1")).satisfies(event -> {
            assertThat(event.eventType()).isEqualTo(RuntimeEventType.REQUEST_STARTED);
            assertThat(event.status()).isEqualTo(RuntimeStatus.UNKNOWN);
            assertThat(event.traceId()).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
            assertThat(event.payload()).containsEntry("httpMethod", "GET");
        });

        assertThat(bridge.createRequestCompleted(traceContext, "GET", "/orders/1", 200, "127.0.0.1", 15L)).satisfies(event -> {
            assertThat(event.eventType()).isEqualTo(RuntimeEventType.REQUEST_COMPLETED);
            assertThat(event.status()).isEqualTo(RuntimeStatus.SUCCESS);
            assertThat(event.payload()).containsEntry("statusCode", 200);
            assertThat(event.payload()).containsEntry("latencyMillis", 15L);
        });

        assertThat(bridge.createServiceCallStarted(traceContext, "com.example.OrderService", "placeOrder", new Object[]{"id"})).satisfies(event -> {
            assertThat(event.eventType()).isEqualTo(RuntimeEventType.SERVICE_CALL_STARTED);
            assertThat(event.payload()).containsEntry("argumentCount", 1);
        });

        assertThat(bridge.createServiceCallCompleted(traceContext, "com.example.OrderService", "placeOrder", "ok", 20L)).satisfies(event -> {
            assertThat(event.eventType()).isEqualTo(RuntimeEventType.SERVICE_CALL_COMPLETED);
            assertThat(event.payload()).containsEntry("returnType", "java.lang.String");
        });

        assertThat(bridge.createErrorOccurred(traceContext, new IllegalStateException("boom"), "com.example.OrderService", "placeOrder", 21L, true)).satisfies(event -> {
            assertThat(event.eventType()).isEqualTo(RuntimeEventType.ERROR_OCCURRED);
            assertThat(event.status()).isEqualTo(RuntimeStatus.FAILED);
            assertThat(event.payload()).containsEntry("exceptionClass", IllegalStateException.class.getName());
            assertThat(event.payload()).containsKey("stackTrace");
        });

        assertThat(bridge.createHeartbeat(traceContext)).satisfies(event -> {
            assertThat(event.eventType()).isEqualTo(RuntimeEventType.HEARTBEAT);
            assertThat(event.status()).isEqualTo(RuntimeStatus.SUCCESS);
            assertThat(event.payload()).containsEntry("heartbeat", true);
        });
    }
}
