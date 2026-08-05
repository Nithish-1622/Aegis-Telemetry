package com.aegis.telemetry.instrumentation.exception;

import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.instrumentation.integration.RuntimeInstrumentationBridge;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuntimeExceptionHandlerTest {

    private final RuntimeTelemetrySdk sdk = RuntimeTelemetrySdk.getInstance();

    @AfterEach
    void tearDown() {
        sdk.shutdown();
    }

    @Test
    void recordsRuntimeAndCheckedExceptionsWithStackTrace() {
        sdk.initialize(RuntimeSdkConfiguration.builder()
                .applicationName("order-service")
                .instanceId("instance-1")
                .environment("test")
                .heartbeatInterval(Duration.ofSeconds(30))
                .build());

        RuntimeExceptionHandler handler = new RuntimeExceptionHandler(new RuntimeInstrumentationBridge(sdk, new RuntimeMetadataProvider(sdk), new ThreadMetadataProvider()));
        TraceContext traceContext = traceContext();

        assertThat(handler.record(new IllegalArgumentException("bad argument"), traceContext, "com.example.OrderService", "placeOrder", 12L))
                .satisfies(event -> {
                    assertThat(event.eventType()).isEqualTo(RuntimeEventType.ERROR_OCCURRED);
                    assertThat(event.payload()).containsEntry("exceptionClass", IllegalArgumentException.class.getName());
                    assertThat(event.payload()).containsKey("stackTrace");
                });

        assertThatThrownBy(() -> handler.recordAndRethrow(new Exception("checked"), traceContext, "com.example.OrderService", "placeOrder", 13L))
                .isInstanceOf(Exception.class)
                .hasMessage("checked");
    }

    private static TraceContext traceContext() {
        return TraceContext.builder()
                .traceId(UUID.fromString("11111111-1111-1111-1111-111111111111").toString())
                .spanId(UUID.fromString("22222222-2222-2222-2222-222222222222").toString())
                .serviceName("order-service")
                .createdAt(Instant.parse("2026-07-21T00:00:00Z"))
                .build();
    }
}
