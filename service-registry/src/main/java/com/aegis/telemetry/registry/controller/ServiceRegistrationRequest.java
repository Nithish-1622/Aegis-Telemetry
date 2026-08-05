package com.aegis.telemetry.registry.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.io.Serial;
import java.io.Serializable;

public record ServiceRegistrationRequest(
        @NotBlank String serviceName,
        @NotBlank String version,
        @NotBlank String host,
        @Min(1) int port,
        @NotBlank String environment
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
