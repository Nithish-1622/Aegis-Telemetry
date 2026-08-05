package com.aegis.telemetry.registry.integration;

import com.aegis.telemetry.registry.config.ServiceRegistryProperties;
import com.aegis.telemetry.registry.heartbeat.HeartbeatService;
import com.aegis.telemetry.registry.model.ServiceInstance;
import com.aegis.telemetry.registry.service.ServiceRegistryService;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RegistryIntegrationBridgeTest {

    private final RuntimeTelemetrySdk sdk = RuntimeTelemetrySdk.getInstance();

    @AfterEach
    void tearDown() {
        sdk.shutdown();
    }

    @Test
    void automaticallyRegistersDeregistersAndSendsHeartbeat() {
        sdk.initialize(RuntimeSdkConfiguration.builder()
                .applicationName("payment-service")
                .instanceId("payment-01")
                .environment("production")
                .heartbeatInterval(Duration.ofSeconds(30))
                .build());

        ServiceRegistryService registryService = new ServiceRegistryService();
        ServiceRegistryProperties properties = new ServiceRegistryProperties();
        properties.setHost("10.0.0.12");
        properties.setPort(8080);
        properties.setDefaultVersion("1.0.0");
        properties.setHeartbeatIntervalMs(10L);
        HeartbeatService heartbeatService = new HeartbeatService(registryService, properties);
        RegistryIntegrationBridge bridge = new RegistryIntegrationBridge(sdk, registryService, heartbeatService, properties);

        bridge.registerOnStartup();
        assertThat(registryService.findByInstance("payment-01")).isPresent();

        bridge.sendHeartbeat();
        ServiceInstance afterHeartbeat = registryService.findByInstance("payment-01").orElseThrow();
        assertThat(afterHeartbeat.lastHeartbeat()).isNotNull();

        bridge.deregisterOnShutdown();
        assertThat(registryService.findByInstance("payment-01")).isEmpty();
    }
}
