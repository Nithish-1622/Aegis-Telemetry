package com.aegis.telemetry.trace.context;

import java.util.Optional;

public final class TraceContextHolder {

    private static final ThreadLocal<TraceContext> CURRENT_CONTEXT = new ThreadLocal<>();

    private TraceContextHolder() {
    }

    public static void setContext(TraceContext context) {
        if (context == null) {
            clear();
            return;
        }
        CURRENT_CONTEXT.set(context);
    }

    public static Optional<TraceContext> getContext() {
        return Optional.ofNullable(CURRENT_CONTEXT.get());
    }

    public static void clear() {
        CURRENT_CONTEXT.remove();
    }

    public static boolean hasContext() {
        return CURRENT_CONTEXT.get() != null;
    }
}
