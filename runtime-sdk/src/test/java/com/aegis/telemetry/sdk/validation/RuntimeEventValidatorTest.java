package com.aegis.telemetry.sdk.validation;

import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.enums.RuntimeStatus;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuntimeEventValidatorTest {

    private final RuntimeEventValidator validator = new RuntimeEventValidator();
    private final RuntimeTelemetrySdk sdk = RuntimeTelemetrySdk.getInstance();

    @AfterEach
    void tearDown() {
        sdk.shutdown();
    }

    @Test
    void acceptsValidEvent() {
        sdk.initialize(RuntimeSdkConfiguration.builder()
                .applicationName("service")
                .instanceId("instance-1")
                .environment("test")
                .heartbeatInterval(Duration.ofSeconds(30))
                .build());

        RuntimeEvent event = RuntimeEventBuilderHolder.buildValidEvent();

        validator.validate(event);
        assertThat(event.payload()).containsEntry("key", "value");
    }

    @Test
    void rejectsInvalidEventWithDescriptiveMessage() {
        RuntimeEvent invalid = new RuntimeEvent(
                null,
                null,
                null,
                null,
                " ",
                null,
                RuntimeEventType.REQUEST_STARTED,
                Instant.now().plusSeconds(60),
                null,
                0L,
                -1L,
                RuntimeStatus.UNKNOWN,
                Map.of()
        );

        assertThatThrownBy(() -> validator.validate(invalid))
                .isInstanceOf(RuntimeEventValidationException.class)
                .hasMessageContaining("traceId must be present")
                .hasMessageContaining("serviceName must not be blank")
                .hasMessageContaining("timestamp must not be in the future")
                .hasMessageContaining("threadId must be positive")
                .hasMessageContaining("latency must not be negative");
    }

    private static final class RuntimeEventBuilderHolder {
        private static RuntimeEvent buildValidEvent() {
            return RuntimeEvent.builder()
                    .eventId(UUID.randomUUID())
                    .traceId(UUID.randomUUID())
                    .spanId(UUID.randomUUID())
                    .serviceName("service")
                    .eventType(RuntimeEventType.REQUEST_STARTED)
                    .timestamp(Instant.parse("2026-07-21T00:00:00Z"))
                    .threadId(1L)
                    .latency(0L)
                    .status(RuntimeStatus.UNKNOWN)
                    .payload(Map.of("key", "value"))
                    .build();
        }
    }
}
