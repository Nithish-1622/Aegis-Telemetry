package com.aegis.telemetry.registry.service;

public final class DuplicateServiceRegistrationException extends RuntimeException {

    public DuplicateServiceRegistrationException(String message) {
        super(message);
    }
}
