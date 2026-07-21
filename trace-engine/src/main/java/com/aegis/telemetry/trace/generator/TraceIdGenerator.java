package com.aegis.telemetry.trace.generator;

import java.util.UUID;

public final class TraceIdGenerator {

    private TraceIdGenerator() {
    }

    public static String generateTraceId() {
        return UUID.randomUUID().toString();
    }
}
