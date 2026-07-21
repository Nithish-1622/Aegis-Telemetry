package com.aegis.telemetry.trace.generator;

import java.util.UUID;

public final class SpanIdGenerator {

    private SpanIdGenerator() {
    }

    public static String generateSpanId() {
        return UUID.randomUUID().toString();
    }
}
