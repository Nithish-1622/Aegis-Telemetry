package com.aegis.telemetry.registry.service;

import com.aegis.telemetry.registry.model.ServiceInstance;
import com.aegis.telemetry.registry.model.ServiceStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistryService {

    private final ConcurrentHashMap<String, ServiceInstance> instancesById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> instanceIdsByServiceName = new ConcurrentHashMap<>();

    public synchronized ServiceInstance register(ServiceInstance serviceInstance) {
        Objects.requireNonNull(serviceInstance, "serviceInstance must not be null");
        if (instancesById.containsKey(serviceInstance.instanceId())) {
            throw new DuplicateServiceRegistrationException("service instance already registered: " + serviceInstance.instanceId());
        }
        ServiceInstance registeredInstance = serviceInstance.toBuilder()
                .status(ServiceStatus.REGISTERED)
                .registeredAt(serviceInstance.registeredAt() == null ? Instant.now() : serviceInstance.registeredAt())
            .lastHeartbeat(serviceInstance.lastHeartbeat())
                .build();
        instancesById.put(registeredInstance.instanceId(), registeredInstance);
        instanceIdsByServiceName.computeIfAbsent(registeredInstance.serviceName(), ignored -> ConcurrentHashMap.newKeySet())
                .add(registeredInstance.instanceId());
        return registeredInstance;
    }

    public synchronized Optional<ServiceInstance> deregister(String instanceId) {
        Objects.requireNonNull(instanceId, "instanceId must not be null");
        ServiceInstance removed = instancesById.remove(instanceId);
        if (removed == null) {
            return Optional.empty();
        }
        Set<String> serviceInstanceIds = instanceIdsByServiceName.get(removed.serviceName());
        if (serviceInstanceIds != null) {
            serviceInstanceIds.remove(instanceId);
            if (serviceInstanceIds.isEmpty()) {
                instanceIdsByServiceName.remove(removed.serviceName());
            }
        }
        return Optional.of(removed.toBuilder().status(ServiceStatus.DEREGISTERED).build());
    }

    public synchronized ServiceInstance heartbeat(String instanceId, Instant timestamp) {
        Objects.requireNonNull(instanceId, "instanceId must not be null");
        Instant heartbeatTime = timestamp == null ? Instant.now() : timestamp;
        ServiceInstance current = instancesById.get(instanceId);
        if (current == null) {
            throw new ServiceNotFoundException("service instance not found: " + instanceId);
        }
        ServiceInstance updated = current.toBuilder()
                .status(ServiceStatus.ACTIVE)
                .lastHeartbeat(heartbeatTime)
                .build();
        instancesById.put(instanceId, updated);
        return updated;
    }

    public Optional<ServiceInstance> findByInstance(String instanceId) {
        if (instanceId == null || instanceId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(instancesById.get(instanceId));
    }

    public List<ServiceInstance> findByService(String serviceName) {
        if (serviceName == null || serviceName.isBlank()) {
            return List.of();
        }
        Set<String> serviceInstanceIds = instanceIdsByServiceName.get(serviceName);
        if (serviceInstanceIds == null || serviceInstanceIds.isEmpty()) {
            return List.of();
        }
        List<ServiceInstance> instances = new ArrayList<>();
        for (String instanceId : serviceInstanceIds) {
            ServiceInstance serviceInstance = instancesById.get(instanceId);
            if (serviceInstance != null) {
                instances.add(serviceInstance);
            }
        }
        return Collections.unmodifiableList(instances);
    }

    public List<ServiceInstance> listServices() {
        return List.copyOf(instancesById.values());
    }

    public synchronized ServiceInstance updateServiceMetadata(String instanceId, String version, String host, Integer port, String environment) {
        ServiceInstance current = instancesById.get(instanceId);
        if (current == null) {
            throw new ServiceNotFoundException("service instance not found: " + instanceId);
        }
        ServiceInstance updated = current.toBuilder()
                .version(version == null || version.isBlank() ? current.version() : version)
                .host(host == null || host.isBlank() ? current.host() : host)
                .port(port == null ? current.port() : port)
                .environment(environment == null || environment.isBlank() ? current.environment() : environment)
                .build();
        instancesById.put(instanceId, updated);
        return updated;
    }

    public synchronized void markStatus(String instanceId, ServiceStatus status) {
        ServiceInstance current = instancesById.get(instanceId);
        if (current == null) {
            throw new ServiceNotFoundException("service instance not found: " + instanceId);
        }
        instancesById.put(instanceId, current.toBuilder().status(status).build());
    }
}
