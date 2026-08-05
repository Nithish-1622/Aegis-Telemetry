package com.aegis.telemetry.trace.util;

import java.util.Optional;

import com.aegis.telemetry.trace.context.TraceContext;
import com.aegis.telemetry.trace.context.TraceContextHolder;

public final class TraceUtils {

    private TraceUtils() {
    }

    public static boolean isRootTrace(TraceContext context) {
        return context != null && context.parentSpanId() == null;
    }

    public static boolean hasParent(TraceContext context) {
        return context != null && context.parentSpanId() != null;
    }

    public static Optional<String> currentTraceId() {
        return TraceContextHolder.getContext().map(TraceContext::traceId);
    }

    public static Optional<String> currentSpanId() {
        return TraceContextHolder.getContext().map(TraceContext::spanId);
    }
}
