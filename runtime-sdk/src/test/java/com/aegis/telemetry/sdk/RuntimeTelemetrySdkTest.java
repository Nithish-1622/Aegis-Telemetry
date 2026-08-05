package com.aegis.telemetry.sdk;

import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeTelemetrySdkTest {

    private final RuntimeTelemetrySdk sdk = RuntimeTelemetrySdk.getInstance();

    @AfterEach
    void tearDown() {
        sdk.shutdown();
    }

    @Test
    void initializesAndShutsDownIdempotently() {
        RuntimeSdkConfiguration configuration = RuntimeSdkConfiguration.builder()
                .applicationName("payment-service")
                .instanceId("instance-1")
                .environment("test")
                .samplingRate(1.0d)
                .capturePayload(true)
                .captureThreadInfo(true)
                .heartbeatInterval(Duration.ofSeconds(30))
                .build();

        sdk.initialize(configuration);
        sdk.initialize(configuration);

        assertThat(sdk.isInitialized()).isTrue();
        assertThat(sdk.getConfiguration()).isEqualTo(configuration);
        assertThat(sdk.getCurrentTraceContext()).isPresent();

        sdk.shutdown();

        assertThat(sdk.isInitialized()).isFalse();
        assertThat(sdk.getCurrentTraceContext()).isEmpty();
    }

    @Test
    void behavesAsSingleton() {
        assertThat(RuntimeTelemetrySdk.getInstance()).isSameAs(sdk);
    }
}
