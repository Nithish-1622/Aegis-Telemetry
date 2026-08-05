package com.aegis.telemetry.contracts.trace;

public final class TraceHeaders {

    public static final String TRACE_ID = "X-Trace-Id";
    public static final String SPAN_ID = "X-Span-Id";
    public static final String PARENT_SPAN_ID = "X-Parent-Span-Id";

    private TraceHeaders() {
    }
}
