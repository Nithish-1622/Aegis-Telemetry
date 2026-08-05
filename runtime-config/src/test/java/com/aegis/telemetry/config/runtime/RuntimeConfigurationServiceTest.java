package com.aegis.telemetry.config.runtime;

import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeConfigurationServiceTest {

    private final RuntimeTelemetrySdk sdk = RuntimeTelemetrySdk.getInstance();

    @AfterEach
    void tearDown() {
        sdk.shutdown();
    }

    @Test
    void exposesDefaultConfiguration() {
        RuntimeConfigurationService service = new RuntimeConfigurationService(sdk);

        assertThat(service.getConfiguration().capturePayload()).isTrue();
        assertThat(service.getConfiguration().captureThreadInfo()).isTrue();
        assertThat(service.getConfiguration().samplingRate()).isEqualTo(100.0d);
    }

    @Test
    void appliesUpdatesToSdk() {
        sdk.initialize(RuntimeSdkConfiguration.builder()
                .applicationName("order-service")
                .instanceId("order-01")
                .environment("production")
                .heartbeatInterval(Duration.ofSeconds(30))
                .build());

        RuntimeConfigurationService service = new RuntimeConfigurationService(sdk);
        RuntimeConfiguration updated = RuntimeConfiguration.builder()
                .capturePayload(false)
                .captureThreadInfo(false)
                .samplingRate(50.0d)
                .heartbeatIntervalSeconds(15L)
                .stackTraceEnabled(false)
                .maximumPayloadSize(4_096)
                .build();

        service.updateConfiguration(updated);

        assertThat(service.getConfiguration()).isEqualTo(updated);
        assertThat(sdk.getConfiguration().capturePayload()).isFalse();
        assertThat(sdk.getConfiguration().captureThreadInfo()).isFalse();
        assertThat(sdk.getConfiguration().samplingRate()).isEqualTo(0.5d);
        assertThat(sdk.getConfiguration().heartbeatInterval()).isEqualTo(Duration.ofSeconds(15L));
    }

    @Test
    void validatesConfigurationValues() {
        RuntimeConfiguration configuration = RuntimeConfiguration.builder()
                .samplingRate(0.0d)
                .heartbeatIntervalSeconds(0L)
                .maximumPayloadSize(0)
                .build();

        assertThat(configuration.samplingRate()).isEqualTo(100.0d);
        assertThat(configuration.heartbeatIntervalSeconds()).isPositive();
        assertThat(configuration.maximumPayloadSize()).isPositive();
    }
}
