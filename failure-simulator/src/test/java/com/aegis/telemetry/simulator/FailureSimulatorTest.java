package com.aegis.telemetry.simulator;

import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.enums.RuntimeStatus;
import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.publisher.core.RuntimeEventPublisher;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import com.aegis.telemetry.sdk.validation.RuntimeEventValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FailureSimulatorTest {

    private TelemetryFailureSimulator simulator;
    private RuntimeTelemetrySdk sdk;
    private RuntimeEventValidator validator;

    @BeforeEach
    void setUp() {
        simulator = new TelemetryFailureSimulator();
        sdk = RuntimeTelemetrySdk.getInstance();
        sdk.initialize(RuntimeSdkConfiguration.builder()
                .applicationName("simulator-test")
                .instanceId("sim-001")
                .environment("test")
                .build());
        validator = new RuntimeEventValidator();
    }

    @Test
    void createOversizedPayloadEventGeneratesEvent() {
        RuntimeEvent event = simulator.createOversizedPayloadEvent(sdk, 2048);
        assertThat(event).isNotNull();
        assertThat(event.eventType()).isEqualTo(RuntimeEventType.ERROR_OCCURRED);
        assertThat(event.payload()).containsKey("largeData");

        assertThatCode(() -> validator.validate(event)).doesNotThrowAnyException();
    }

    @Test
    void simulateExceptionCascadeCapturesStackTrace() {
        Exception cause = new IllegalStateException("Database connection timeout");
        RuntimeException exception = new RuntimeException("Service failed", cause);

        RuntimeEvent event = simulator.simulateExceptionCascade(sdk, exception);

        assertThat(event).isNotNull();
        assertThat(event.eventType()).isEqualTo(RuntimeEventType.ERROR_OCCURRED);
        assertThat(event.status()).isEqualTo(RuntimeStatus.FAILED);
        assertThat(event.payload().get("errorMessage")).isEqualTo("Service failed");
        assertThat((String) event.payload().get("stackTrace")).contains("IllegalStateException: Database connection timeout");
        assertThat((String) event.payload().get("stackTrace")).contains("RuntimeException: Service failed");
    }

    @Test
    void simulateHighThroughputBurstPublishesEvents() {
        RuntimeEventPublisher mockPublisher = mock(RuntimeEventPublisher.class);

        TelemetryFailureSimulator.BurstResult result = simulator.simulateHighThroughputBurst(sdk, mockPublisher, 100);

        assertThat(result.totalEvents()).isEqualTo(100);
        assertThat(result.published()).isEqualTo(100);
        assertThat(result.failed()).isEqualTo(0);
        assertThat(result.durationMs()).isGreaterThan(0.0);
        verify(mockPublisher, org.mockito.Mockito.times(100)).publish(any());
    }
}
