package com.aegis.telemetry.registry.service;

import com.aegis.telemetry.registry.model.ServiceInstance;
import com.aegis.telemetry.registry.model.ServiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceRegistryServiceTest {

    private ServiceRegistryService serviceRegistryService;

    @BeforeEach
    void setUp() {
        serviceRegistryService = new ServiceRegistryService();
    }

    @Test
    void registersAndFindsService() {
        ServiceInstance registered = serviceRegistryService.register(sampleInstance("payment-01"));

        assertThat(registered.status()).isEqualTo(ServiceStatus.REGISTERED);
        assertThat(serviceRegistryService.findByInstance("payment-01")).contains(registered);
        assertThat(serviceRegistryService.findByService("payment-service")).containsExactly(registered);
        assertThat(serviceRegistryService.listServices()).containsExactly(registered);
    }

    @Test
    void preventsDuplicateRegistration() {
        serviceRegistryService.register(sampleInstance("payment-01"));

        assertThatThrownBy(() -> serviceRegistryService.register(sampleInstance("payment-01")))
                .isInstanceOf(DuplicateServiceRegistrationException.class);
    }

    @Test
    void deregistersService() {
        serviceRegistryService.register(sampleInstance("payment-01"));

        assertThat(serviceRegistryService.deregister("payment-01")).isPresent();
        assertThat(serviceRegistryService.findByInstance("payment-01")).isEmpty();
        assertThat(serviceRegistryService.listServices()).isEmpty();
    }

    @Test
    void updatesMetadata() {
        serviceRegistryService.register(sampleInstance("payment-01"));

        ServiceInstance updated = serviceRegistryService.updateServiceMetadata("payment-01", "1.0.1", "10.0.0.99", 9090, "staging");

        assertThat(updated.version()).isEqualTo("1.0.1");
        assertThat(updated.host()).isEqualTo("10.0.0.99");
        assertThat(updated.port()).isEqualTo(9090);
        assertThat(updated.environment()).isEqualTo("staging");
    }

    private static ServiceInstance sampleInstance(String instanceId) {
        return ServiceInstance.builder()
                .serviceName("payment-service")
                .instanceId(instanceId)
                .version("1.0.0")
                .host("10.0.0.12")
                .port(8080)
                .environment("production")
                .status(ServiceStatus.REGISTERED)
                .registeredAt(Instant.parse("2026-07-21T00:00:00Z"))
                .lastHeartbeat(Instant.parse("2026-07-21T00:00:00Z"))
                .build();
    }
}
