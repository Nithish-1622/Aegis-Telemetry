package com.aegis.telemetry.registry.controller;

import com.aegis.telemetry.registry.model.ServiceInstance;
import com.aegis.telemetry.registry.model.ServiceStatus;
import com.aegis.telemetry.registry.service.ServiceRegistryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/internal/services")
public class ServiceRegistryController {

    private final ServiceRegistryService registryService;

    public ServiceRegistryController(ServiceRegistryService registryService) {
        this.registryService = registryService;
    }

    @PostMapping("/register")
    public ResponseEntity<ServiceRegistrationResponse> register(@Valid @RequestBody ServiceRegistrationRequest request) {
        ServiceInstance serviceInstance = ServiceInstance.builder()
                .serviceName(request.serviceName())
                .version(request.version())
                .host(request.host())
                .port(request.port())
                .environment(request.environment())
                .status(ServiceStatus.REGISTERED)
                .registeredAt(Instant.now())
                .lastHeartbeat(Instant.now())
                .build();
        ServiceInstance registered = registryService.register(serviceInstance);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ServiceRegistrationResponse(registered.serviceId(), registered.status().name()));
    }

    @DeleteMapping("/{instanceId}")
    public ResponseEntity<Void> deregister(@PathVariable String instanceId) {
        registryService.deregister(instanceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<ServiceInstance> listServices() {
        return registryService.listServices();
    }

    @GetMapping("/{serviceName}")
    public List<ServiceInstance> findByService(@PathVariable String serviceName) {
        return registryService.findByService(serviceName);
    }
}
