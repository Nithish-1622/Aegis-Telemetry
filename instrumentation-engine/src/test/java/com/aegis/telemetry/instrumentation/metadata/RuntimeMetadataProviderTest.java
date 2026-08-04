package com.aegis.telemetry.instrumentation.metadata;

import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeMetadataProviderTest {

    private final RuntimeTelemetrySdk sdk = RuntimeTelemetrySdk.getInstance();

    @AfterEach
    void tearDown() {
        sdk.shutdown();
    }

    @Test
    void retrievesAndCachesRuntimeMetadata() {
        sdk.initialize(RuntimeSdkConfiguration.builder()
                .applicationName("order-service")
                .instanceId("instance-1")
                .environment("test")
                .heartbeatInterval(Duration.ofSeconds(30))
                .build());

        RuntimeMetadataProvider provider = new RuntimeMetadataProvider(sdk);
        RuntimeMetadataProvider.RuntimeMetadata first = provider.getMetadata();
        RuntimeMetadataProvider.RuntimeMetadata second = provider.getMetadata();

        assertThat(first).isSameAs(second);
        assertThat(first.applicationName()).isEqualTo("order-service");
        assertThat(first.environment()).isEqualTo("test");
        assertThat(first.instanceId()).isEqualTo("instance-1");
        assertThat(first.hostName()).isNotBlank();
        assertThat(first.jvmVersion()).isNotBlank();
        assertThat(first.operatingSystem()).isNotBlank();
        assertThat(first.asMap()).containsEntry("serviceName", "order-service");
    }
}
