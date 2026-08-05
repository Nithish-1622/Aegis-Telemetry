package com.aegis.telemetry.registry.heartbeat;

import com.aegis.telemetry.registry.model.ServiceInstance;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/services")
public class HeartbeatController {

    private final HeartbeatService heartbeatService;

    public HeartbeatController(HeartbeatService heartbeatService) {
        this.heartbeatService = heartbeatService;
    }

    @PostMapping("/heartbeat")
    public ServiceInstance heartbeat(@Valid @RequestBody HeartbeatRequest request) {
        return heartbeatService.heartbeat(request.serviceName(), request.instanceId(), request.timestamp());
    }
}
