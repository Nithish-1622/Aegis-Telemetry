package com.aegis.telemetry.registry.heartbeat;

import com.aegis.telemetry.registry.config.ServiceRegistryProperties;
import com.aegis.telemetry.registry.model.ServiceInstance;
import com.aegis.telemetry.registry.model.ServiceStatus;
import com.aegis.telemetry.registry.service.ServiceNotFoundException;
import com.aegis.telemetry.registry.service.ServiceRegistryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeartbeatServiceTest {

    private ServiceRegistryService registryService;
    private HeartbeatService heartbeatService;

    @BeforeEach
    void setUp() {
        registryService = new ServiceRegistryService();
        ServiceRegistryProperties properties = new ServiceRegistryProperties();
        properties.setHeartbeatTimeoutMs(1_000L);
        heartbeatService = new HeartbeatService(registryService, properties);
        registryService.register(sampleInstance("payment-01", Instant.parse("2026-07-21T00:00:00Z")));
    }

    @Test
    void updatesHeartbeatForRegisteredService() {
        ServiceInstance updated = heartbeatService.heartbeat("payment-service", "payment-01", Instant.parse("2026-07-21T00:01:00Z"));

        assertThat(updated.status()).isEqualTo(ServiceStatus.ACTIVE);
        assertThat(updated.lastHeartbeat()).isEqualTo(Instant.parse("2026-07-21T00:01:00Z"));
    }

    @Test
    void throwsWhenServiceIsMissing() {
        assertThatThrownBy(() -> heartbeatService.heartbeat("payment-service", "missing", Instant.now()))
                .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    void marksStaleServicesInactive() {
        registryService.register(sampleInstance("payment-02", Instant.parse("2026-07-20T00:00:00Z")));

        var updated = heartbeatService.detectStaleServices();

        assertThat(updated).anySatisfy(instance -> assertThat(instance.status()).isIn(ServiceStatus.ACTIVE, ServiceStatus.INACTIVE));
        assertThat(registryService.findByInstance("payment-02")).get().extracting(ServiceInstance::status).isEqualTo(ServiceStatus.INACTIVE);
    }

    private static ServiceInstance sampleInstance(String instanceId, Instant lastHeartbeat) {
        return ServiceInstance.builder()
                .serviceName("payment-service")
                .instanceId(instanceId)
                .version("1.0.0")
                .host("10.0.0.12")
                .port(8080)
                .environment("production")
                .status(ServiceStatus.REGISTERED)
                .registeredAt(Instant.parse("2026-07-21T00:00:00Z"))
                .lastHeartbeat(lastHeartbeat)
                .build();
    }
}
