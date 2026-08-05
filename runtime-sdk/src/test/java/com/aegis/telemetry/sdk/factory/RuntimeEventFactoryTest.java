package com.aegis.telemetry.sdk.factory;

import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.enums.RuntimeStatus;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeEventFactoryTest {

    private final RuntimeTelemetrySdk sdk = RuntimeTelemetrySdk.getInstance();

    @AfterEach
    void tearDown() {
        sdk.shutdown();
    }

    @Test
    void createsAllSupportedRuntimeEvents() {
        sdk.initialize(RuntimeSdkConfiguration.builder()
                .applicationName("order-service")
                .instanceId("instance-1")
                .environment("test")
                .samplingRate(1.0d)
                .capturePayload(true)
                .captureThreadInfo(true)
                .heartbeatInterval(Duration.ofSeconds(30))
                .build());

        RuntimeEventFactory factory = sdk.getEventFactory();

        assertThat(factory.createRequestStarted().eventType()).isEqualTo(RuntimeEventType.REQUEST_STARTED);
        assertThat(factory.createRequestStarted().status()).isEqualTo(RuntimeStatus.UNKNOWN);
        assertThat(factory.createRequestCompleted().eventType()).isEqualTo(RuntimeEventType.REQUEST_COMPLETED);
        assertThat(factory.createRequestCompleted().status()).isEqualTo(RuntimeStatus.SUCCESS);
        assertThat(factory.createServiceCallStarted().eventType()).isEqualTo(RuntimeEventType.SERVICE_CALL_STARTED);
        assertThat(factory.createServiceCallCompleted().eventType()).isEqualTo(RuntimeEventType.SERVICE_CALL_COMPLETED);
        assertThat(factory.createRetryTriggered().eventType()).isEqualTo(RuntimeEventType.RETRY_TRIGGERED);
        assertThat(factory.createErrorOccurred().eventType()).isEqualTo(RuntimeEventType.ERROR_OCCURRED);
        assertThat(factory.createHeartbeat().eventType()).isEqualTo(RuntimeEventType.HEARTBEAT);
    }
}
