package com.aegis.telemetry.registry.heartbeat;

import com.aegis.telemetry.registry.config.ServiceRegistryProperties;
import com.aegis.telemetry.registry.model.ServiceInstance;
import com.aegis.telemetry.registry.model.ServiceStatus;
import com.aegis.telemetry.registry.service.ServiceNotFoundException;
import com.aegis.telemetry.registry.service.ServiceRegistryService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class HeartbeatService {

    private final ServiceRegistryService registryService;
    private final ServiceRegistryProperties properties;

    public HeartbeatService(ServiceRegistryService registryService, ServiceRegistryProperties properties) {
        this.registryService = Objects.requireNonNull(registryService, "registryService must not be null");
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
    }

    public ServiceInstance heartbeat(String serviceName, String instanceId, Instant timestamp) {
        Objects.requireNonNull(serviceName, "serviceName must not be null");
        Objects.requireNonNull(instanceId, "instanceId must not be null");
        ServiceInstance instance = registryService.findByInstance(instanceId)
                .orElseThrow(() -> new ServiceNotFoundException("service instance not found: " + instanceId));
        if (!serviceName.equals(instance.serviceName())) {
            throw new ServiceNotFoundException("service instance does not match service name: " + serviceName);
        }
        return registryService.heartbeat(instanceId, timestamp == null ? Instant.now() : timestamp);
    }

    public List<ServiceInstance> detectStaleServices() {
        Instant cutoff = Instant.now().minus(Duration.ofMillis(properties.getHeartbeatTimeoutMs()));
        List<ServiceInstance> services = registryService.listServices();
        for (ServiceInstance serviceInstance : services) {
            Instant lastHeartbeat = serviceInstance.lastHeartbeat();
            if (lastHeartbeat == null || lastHeartbeat.isBefore(cutoff)) {
                registryService.markStatus(serviceInstance.instanceId(), ServiceStatus.INACTIVE);
            } else if (serviceInstance.status() != ServiceStatus.UNHEALTHY) {
                registryService.markStatus(serviceInstance.instanceId(), ServiceStatus.ACTIVE);
            }
        }
        return registryService.listServices();
    }
}
