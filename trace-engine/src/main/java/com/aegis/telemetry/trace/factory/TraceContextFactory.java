package com.aegis.telemetry.trace.factory;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import com.aegis.telemetry.contracts.trace.TraceHeaders;
import com.aegis.telemetry.trace.context.TraceContext;
import com.aegis.telemetry.trace.context.TraceContextHolder;
import com.aegis.telemetry.trace.generator.SpanIdGenerator;
import com.aegis.telemetry.trace.generator.TraceIdGenerator;

public final class TraceContextFactory {

    private final String serviceName;
    private final Clock clock;

    public TraceContextFactory(String serviceName) {
        this(serviceName, Clock.systemUTC());
    }

    public TraceContextFactory(String serviceName, Clock clock) {
        this.serviceName = serviceName;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public TraceContext createRootContext() {
        return createRootContext(serviceName);
    }

    public TraceContext createRootContext(String serviceName) {
        return TraceContext.builder()
                .traceId(TraceIdGenerator.generateTraceId())
                .spanId(SpanIdGenerator.generateSpanId())
                .parentSpanId(null)
                .serviceName(serviceName)
                .createdAt(Instant.now(clock))
                .build();
    }

    public TraceContext createChildContext() {
        return TraceContextHolder.getContext()
                .map(this::createChildContext)
                .orElseGet(this::createRootContext);
    }

    public TraceContext createChildContext(TraceContext parentContext) {
        Objects.requireNonNull(parentContext, "parentContext must not be null");
        return TraceContext.builder()
                .traceId(parentContext.traceId())
                .spanId(SpanIdGenerator.generateSpanId())
                .parentSpanId(parentContext.spanId())
                .serviceName(serviceName)
                .createdAt(Instant.now(clock))
                .build();
    }

    public TraceContext createFromHeaders() {
        return createFromHeaders(Map.of());
    }

    public TraceContext createFromHeaders(Map<String, String> headers) {
        String traceId = normalize(headers.get(TraceHeaders.TRACE_ID));
        String spanId = normalize(headers.get(TraceHeaders.SPAN_ID));
        String parentSpanId = normalize(headers.get(TraceHeaders.PARENT_SPAN_ID));

        if (traceId == null) {
            return createRootContext();
        }

        return TraceContext.builder()
                .traceId(traceId)
                .spanId(spanId == null ? SpanIdGenerator.generateSpanId() : spanId)
                .parentSpanId(parentSpanId)
                .serviceName(serviceName)
                .createdAt(Instant.now(clock))
                .build();
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
