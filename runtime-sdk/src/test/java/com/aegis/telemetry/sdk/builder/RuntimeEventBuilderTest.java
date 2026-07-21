package com.aegis.telemetry.sdk.builder;

import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.enums.RuntimeStatus;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import com.aegis.telemetry.sdk.validation.RuntimeEventValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuntimeEventBuilderTest {

    private final RuntimeTelemetrySdk sdk = RuntimeTelemetrySdk.getInstance();

    @AfterEach
    void tearDown() {
        sdk.shutdown();
    }

    @Test
    void buildsEventWithFluentApi() {
        sdk.initialize(RuntimeSdkConfiguration.builder()
                .applicationName("payment-service")
                .instanceId("instance-1")
                .environment("test")
                .samplingRate(1.0d)
                .capturePayload(true)
                .captureThreadInfo(true)
                .heartbeatInterval(Duration.ofSeconds(30))
                .build());

        var event = RuntimeEventBuilder.builder()
                .serviceName("payment-service")
                .eventType(RuntimeEventType.REQUEST_STARTED)
                .status(RuntimeStatus.UNKNOWN)
                .payload(Map.of("paymentId", "PAY-1"))
                .build();

        assertThat(event.serviceName()).isEqualTo("payment-service");
        assertThat(event.eventType()).isEqualTo(RuntimeEventType.REQUEST_STARTED);
        assertThat(event.status()).isEqualTo(RuntimeStatus.UNKNOWN);
        assertThat(event.payload()).containsEntry("paymentId", "PAY-1");
    }

    @Test
    void validatesRequiredFields() {
        sdk.initialize(RuntimeSdkConfiguration.builder()
                .applicationName("payment-service")
                .instanceId("instance-1")
                .environment("test")
                .build());

        assertThatThrownBy(() -> RuntimeEventBuilder.builder()
                .serviceName("payment-service")
                .build())
                .isInstanceOf(RuntimeEventValidationException.class)
                .hasMessageContaining("eventType must not be null");
    }

    @Test
    void failsWithoutTraceContextWhenSdkNotInitialized() {
        assertThatThrownBy(() -> RuntimeEventBuilder.builder()
                .serviceName("payment-service")
                .eventType(RuntimeEventType.REQUEST_STARTED)
                .status(RuntimeStatus.UNKNOWN)
                .build())
                .isInstanceOf(RuntimeEventValidationException.class)
                .hasMessageContaining("trace context is required");
    }
}
