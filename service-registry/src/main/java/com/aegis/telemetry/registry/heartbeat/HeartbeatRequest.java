package com.aegis.telemetry.registry.heartbeat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

public record HeartbeatRequest(
        @NotBlank String serviceName,
        @NotBlank String instanceId,
        @NotNull Instant timestamp
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
