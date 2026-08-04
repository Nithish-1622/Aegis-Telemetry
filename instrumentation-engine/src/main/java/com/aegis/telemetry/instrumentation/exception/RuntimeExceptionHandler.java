package com.aegis.telemetry.instrumentation.exception;

import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.instrumentation.integration.RuntimeInstrumentationBridge;
import com.aegis.telemetry.trace.context.TraceContext;

import java.util.Objects;

public final class RuntimeExceptionHandler {

    private final RuntimeInstrumentationBridge bridge;
    private final boolean captureStackTrace;

    public RuntimeExceptionHandler(RuntimeInstrumentationBridge bridge) {
        this(bridge, true);
    }

    public RuntimeExceptionHandler(RuntimeInstrumentationBridge bridge, boolean captureStackTrace) {
        this.bridge = Objects.requireNonNull(bridge, "bridge must not be null");
        this.captureStackTrace = captureStackTrace;
    }

    public RuntimeEvent record(Throwable throwable, TraceContext traceContext, String sourceClass, String sourceMethod, long latencyMillis) {
        Objects.requireNonNull(throwable, "throwable must not be null");
        Objects.requireNonNull(traceContext, "traceContext must not be null");
        return bridge.createErrorOccurred(traceContext, throwable, sourceClass, sourceMethod, latencyMillis, captureStackTrace);
    }

    public <T extends Throwable> void recordAndRethrow(T throwable, TraceContext traceContext, String sourceClass, String sourceMethod, long latencyMillis) throws T {
        record(throwable, traceContext, sourceClass, sourceMethod, latencyMillis);
        throw throwable;
    }
}
