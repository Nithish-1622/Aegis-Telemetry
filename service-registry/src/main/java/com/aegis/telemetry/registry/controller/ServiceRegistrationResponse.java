package com.aegis.telemetry.registry.controller;

import java.io.Serial;
import java.io.Serializable;

public record ServiceRegistrationResponse(String serviceId, String status) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
