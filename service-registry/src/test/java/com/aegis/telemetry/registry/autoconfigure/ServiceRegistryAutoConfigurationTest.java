package com.aegis.telemetry.registry.autoconfigure;

import com.aegis.telemetry.registry.controller.ServiceRegistryController;
import com.aegis.telemetry.registry.heartbeat.HeartbeatController;
import com.aegis.telemetry.registry.health.ServiceHealthMonitor;
import com.aegis.telemetry.registry.integration.RegistryIntegrationBridge;
import com.aegis.telemetry.registry.service.ServiceRegistryService;
import com.aegis.telemetry.sdk.autoconfigure.RuntimeSdkAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceRegistryAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RuntimeSdkAutoConfiguration.class, ServiceRegistryAutoConfiguration.class));

    @Test
    void registersServiceRegistryBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ServiceRegistryService.class);
            assertThat(context).hasSingleBean(ServiceHealthMonitor.class);
            assertThat(context).hasSingleBean(ServiceRegistryController.class);
            assertThat(context).hasSingleBean(HeartbeatController.class);
            assertThat(context).hasSingleBean(RegistryIntegrationBridge.class);
        });
    }
}
