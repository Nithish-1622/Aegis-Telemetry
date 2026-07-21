package com.aegis.telemetry.trace.propagation;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.aegis.telemetry.contracts.trace.TraceHeaders;
import com.aegis.telemetry.trace.context.TraceContext;

class TraceHeaderPropagatorTest {

    private final TraceHeaderPropagator propagator = new TraceHeaderPropagator();

    @Test
    void injectsHeaders() {
        TraceContext context = TraceContext.builder()
                .traceId("trace-1")
                .spanId("span-1")
                .parentSpanId("parent-1")
                .serviceName("service")
                .createdAt(Instant.parse("2026-07-21T00:00:00Z"))
                .build();
        Map<String, String> headers = new HashMap<>();

        propagator.inject(context, headers);

        assertThat(headers).containsEntry(TraceHeaders.TRACE_ID, "trace-1");
        assertThat(headers).containsEntry(TraceHeaders.SPAN_ID, "span-1");
        assertThat(headers).containsEntry(TraceHeaders.PARENT_SPAN_ID, "parent-1");
    }

    @Test
    void extractsHeaders() {
        Map<String, String> headers = Map.of(
                TraceHeaders.TRACE_ID, "trace-1",
                TraceHeaders.SPAN_ID, "span-1",
                TraceHeaders.PARENT_SPAN_ID, "parent-1"
        );

        TraceContext context = propagator.extract(headers).orElseThrow();

        assertThat(context.traceId()).isEqualTo("trace-1");
        assertThat(context.spanId()).isEqualTo("span-1");
        assertThat(context.parentSpanId()).isEqualTo("parent-1");
        assertThat(context.serviceName()).isNull();
        assertThat(context.createdAt()).isNull();
    }

    @Test
    void returnsEmptyWhenHeadersAreMissing() {
        assertThat(propagator.extract(Map.of())).isEmpty();
        assertThat(propagator.extract(null)).isEmpty();
    }

    @Test
    void handlesPartialHeaders() {
        Map<String, String> headers = Map.of(TraceHeaders.TRACE_ID, "trace-1");

        TraceContext context = propagator.extract(headers).orElseThrow();

        assertThat(context.traceId()).isEqualTo("trace-1");
        assertThat(context.spanId()).isNull();
        assertThat(context.parentSpanId()).isNull();
    }
}
