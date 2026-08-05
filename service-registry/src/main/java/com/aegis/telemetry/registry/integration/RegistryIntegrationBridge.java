package com.aegis.telemetry.registry.integration;

import com.aegis.telemetry.registry.config.ServiceRegistryProperties;
import com.aegis.telemetry.registry.heartbeat.HeartbeatService;
import com.aegis.telemetry.registry.model.ServiceInstance;
import com.aegis.telemetry.registry.model.ServiceStatus;
import com.aegis.telemetry.registry.service.ServiceRegistryService;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class RegistryIntegrationBridge {

    private static final Logger logger = LoggerFactory.getLogger(RegistryIntegrationBridge.class);

    private final RuntimeTelemetrySdk sdk;
    private final ServiceRegistryService registryService;
    private final HeartbeatService heartbeatService;
    private final ServiceRegistryProperties properties;
    private final AtomicReference<ServiceInstance> registeredInstance = new AtomicReference<>();

    public RegistryIntegrationBridge(RuntimeTelemetrySdk sdk, ServiceRegistryService registryService, HeartbeatService heartbeatService, ServiceRegistryProperties properties) {
        this.sdk = Objects.requireNonNull(sdk, "sdk must not be null");
        this.registryService = Objects.requireNonNull(registryService, "registryService must not be null");
        this.heartbeatService = Objects.requireNonNull(heartbeatService, "heartbeatService must not be null");
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerOnStartup() {
        if (!sdk.isInitialized()) {
            return;
        }
        if (registeredInstance.get() != null) {
            return;
        }
        var configuration = sdk.getConfiguration();
        ServiceInstance serviceInstance = ServiceInstance.builder()
                .serviceName(configuration.applicationName())
                .instanceId(configuration.instanceId())
                .version(properties.getDefaultVersion())
                .host(properties.getHost())
                .port(properties.getPort())
                .environment(configuration.environment())
                .status(ServiceStatus.REGISTERED)
                .registeredAt(Instant.now())
                .lastHeartbeat(Instant.now())
                .build();
        ServiceInstance registered = registryService.register(serviceInstance);
        registeredInstance.set(registered);
        logger.info("Registered service {} instance {}", registered.serviceName(), registered.instanceId());
    }

    @Scheduled(fixedDelayString = "#{@serviceRegistryProperties.heartbeatIntervalMs}")
    public void sendHeartbeat() {
        ServiceInstance current = registeredInstance.get();
        if (current == null || !sdk.isInitialized()) {
            return;
        }
        ServiceInstance heartbeat = heartbeatService.heartbeat(current.serviceName(), current.instanceId(), Instant.now());
        registeredInstance.set(heartbeat);
    }

    @PreDestroy
    public void deregisterOnShutdown() {
        ServiceInstance current = registeredInstance.getAndSet(null);
        if (current == null) {
            return;
        }
        registryService.deregister(current.instanceId());
        logger.info("Deregistered service {} instance {}", current.serviceName(), current.instanceId());
    }
}
