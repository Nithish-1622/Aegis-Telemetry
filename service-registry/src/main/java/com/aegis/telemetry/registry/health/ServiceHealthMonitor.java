package com.aegis.telemetry.registry.health;

import com.aegis.telemetry.registry.model.ServiceInstance;
import com.aegis.telemetry.registry.model.ServiceStatus;
import com.aegis.telemetry.registry.service.ServiceRegistryService;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServiceHealthMonitor {

    private final ServiceRegistryService registryService;
    private final Duration heartbeatTimeout;

    public ServiceHealthMonitor(ServiceRegistryService registryService, Duration heartbeatTimeout) {
        this.registryService = Objects.requireNonNull(registryService, "registryService must not be null");
        this.heartbeatTimeout = Objects.requireNonNull(heartbeatTimeout, "heartbeatTimeout must not be null");
    }

    public List<ServiceInstance> refreshHealth() {
        List<ServiceInstance> currentServices = registryService.listServices();
        Instant cutoff = Instant.now().minus(heartbeatTimeout);
        for (ServiceInstance serviceInstance : currentServices) {
            if (serviceInstance.lastHeartbeat() == null) {
                registryService.markStatus(serviceInstance.instanceId(), ServiceStatus.UNKNOWN);
            } else if (serviceInstance.lastHeartbeat().isBefore(cutoff)) {
                registryService.markStatus(serviceInstance.instanceId(), ServiceStatus.INACTIVE);
            } else {
                registryService.markStatus(serviceInstance.instanceId(), ServiceStatus.ACTIVE);
            }
        }
        return registryService.listServices();
    }

    public List<ServiceInstance> healthyServices() {
        return filterByStatus(ServiceStatus.ACTIVE);
    }

    public List<ServiceInstance> inactiveServices() {
        return filterByStatus(ServiceStatus.INACTIVE);
    }

    public List<ServiceInstance> unhealthyServices() {
        return filterByStatus(ServiceStatus.UNHEALTHY);
    }

    public List<ServiceInstance> unknownServices() {
        return filterByStatus(ServiceStatus.UNKNOWN);
    }

    private List<ServiceInstance> filterByStatus(ServiceStatus status) {
        List<ServiceInstance> matches = new ArrayList<>();
        for (ServiceInstance serviceInstance : registryService.listServices()) {
            if (serviceInstance.status() == status) {
                matches.add(serviceInstance);
            }
        }
        return List.copyOf(matches);
    }
}
