package com.aegis.telemetry.registry.autoconfigure;

import com.aegis.telemetry.registry.config.ServiceRegistryProperties;
import com.aegis.telemetry.registry.controller.ServiceRegistryController;
import com.aegis.telemetry.registry.heartbeat.HeartbeatController;
import com.aegis.telemetry.registry.heartbeat.HeartbeatService;
import com.aegis.telemetry.registry.health.ServiceHealthMonitor;
import com.aegis.telemetry.registry.integration.RegistryIntegrationBridge;
import com.aegis.telemetry.registry.service.ServiceRegistryService;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;

@AutoConfiguration(afterName = "com.aegis.telemetry.sdk.autoconfigure.RuntimeSdkAutoConfiguration")
@ConditionalOnClass(RuntimeTelemetrySdk.class)
@EnableScheduling
@EnableConfigurationProperties(ServiceRegistryProperties.class)
public class ServiceRegistryAutoConfiguration {

    @Bean
    public ServiceRegistryService serviceRegistryService() {
        return new ServiceRegistryService();
    }

    @Bean
    public HeartbeatService heartbeatService(ServiceRegistryService registryService, ServiceRegistryProperties properties) {
        return new HeartbeatService(registryService, properties);
    }

    @Bean
    public ServiceHealthMonitor serviceHealthMonitor(ServiceRegistryService registryService, ServiceRegistryProperties properties) {
        return new ServiceHealthMonitor(registryService, Duration.ofMillis(properties.getHeartbeatTimeoutMs()));
    }

    @Bean
    @ConditionalOnBean(RuntimeTelemetrySdk.class)
    public RegistryIntegrationBridge registryIntegrationBridge(RuntimeTelemetrySdk sdk, ServiceRegistryService registryService, HeartbeatService heartbeatService, ServiceRegistryProperties properties) {
        return new RegistryIntegrationBridge(sdk, registryService, heartbeatService, properties);
    }

    @Bean
    public ServiceRegistryController serviceRegistryController(ServiceRegistryService registryService) {
        return new ServiceRegistryController(registryService);
    }

    @Bean
    public HeartbeatController heartbeatController(HeartbeatService heartbeatService) {
        return new HeartbeatController(heartbeatService);
    }
}
