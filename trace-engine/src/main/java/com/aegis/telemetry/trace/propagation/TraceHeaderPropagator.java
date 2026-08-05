package com.aegis.telemetry.trace.propagation;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.aegis.telemetry.contracts.trace.TraceHeaders;
import com.aegis.telemetry.trace.context.TraceContext;

public final class TraceHeaderPropagator {

    public void inject(TraceContext context, Map<String, String> headers) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(headers, "headers must not be null");

        headers.put(TraceHeaders.TRACE_ID, context.traceId());
        headers.put(TraceHeaders.SPAN_ID, context.spanId());

        if (context.parentSpanId() != null) {
            headers.put(TraceHeaders.PARENT_SPAN_ID, context.parentSpanId());
        } else {
            headers.remove(TraceHeaders.PARENT_SPAN_ID);
        }
    }

    public Optional<TraceContext> extract(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return Optional.empty();
        }

        String traceId = normalize(headers.get(TraceHeaders.TRACE_ID));
        if (traceId == null) {
            return Optional.empty();
        }

        return Optional.of(TraceContext.builder()
                .traceId(traceId)
                .spanId(normalize(headers.get(TraceHeaders.SPAN_ID)))
                .parentSpanId(normalize(headers.get(TraceHeaders.PARENT_SPAN_ID)))
                .build());
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
