package com.aegis.telemetry.registry.health;

import com.aegis.telemetry.registry.config.ServiceRegistryProperties;
import com.aegis.telemetry.registry.model.ServiceInstance;
import com.aegis.telemetry.registry.model.ServiceStatus;
import com.aegis.telemetry.registry.service.ServiceRegistryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceHealthMonitorTest {

    private ServiceRegistryService registryService;
    private ServiceHealthMonitor monitor;

    @BeforeEach
    void setUp() {
        registryService = new ServiceRegistryService();
        monitor = new ServiceHealthMonitor(registryService, Duration.ofMillis(500));
    }

    @Test
    void tracksHealthyInactiveAndUnknownServices() {
        registryService.register(activeInstance("payment-01", Instant.now()));
        registryService.register(staleInstance("payment-02", Instant.now().minusSeconds(5)));
        registryService.register(unknownInstance("payment-03"));

        monitor.refreshHealth();

        assertThat(monitor.healthyServices()).hasSize(1);
        assertThat(monitor.inactiveServices()).hasSize(1);
        assertThat(monitor.unknownServices()).hasSize(1);
    }

    @Test
    void flagsTimedOutHeartbeatAsInactive() {
        registryService.register(staleInstance("payment-02", Instant.now().minusSeconds(5)));

        monitor.refreshHealth();

        assertThat(registryService.findByInstance("payment-02")).get().extracting(ServiceInstance::status).isEqualTo(ServiceStatus.INACTIVE);
    }

    private static ServiceInstance activeInstance(String instanceId, Instant heartbeat) {
        return ServiceInstance.builder()
                .serviceName("payment-service")
                .instanceId(instanceId)
                .version("1.0.0")
                .host("10.0.0.12")
                .port(8080)
                .environment("production")
                .status(ServiceStatus.ACTIVE)
                .registeredAt(Instant.now())
                .lastHeartbeat(heartbeat)
                .build();
    }

    private static ServiceInstance staleInstance(String instanceId, Instant heartbeat) {
        return ServiceInstance.builder()
                .serviceName("payment-service")
                .instanceId(instanceId)
                .version("1.0.0")
                .host("10.0.0.12")
                .port(8080)
                .environment("production")
                .status(ServiceStatus.ACTIVE)
                .registeredAt(Instant.now())
                .lastHeartbeat(heartbeat)
                .build();
    }

    private static ServiceInstance unknownInstance(String instanceId) {
        return ServiceInstance.builder()
                .serviceName("inventory-service")
                .instanceId(instanceId)
                .version("1.0.0")
                .host("10.0.0.13")
                .port(8081)
                .environment("production")
                .status(ServiceStatus.REGISTERED)
                .registeredAt(Instant.now())
                .build();
    }
}
