package com.aegis.telemetry.trace.factory;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.aegis.telemetry.contracts.trace.TraceHeaders;
import com.aegis.telemetry.trace.context.TraceContext;
import com.aegis.telemetry.trace.context.TraceContextHolder;

class TraceContextFactoryTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-21T00:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

    private final TraceContextFactory factory = new TraceContextFactory("order-service", FIXED_CLOCK);

    @AfterEach
    void tearDown() {
        TraceContextHolder.clear();
    }

    @Test
    void createsRootContext() {
        TraceContext context = factory.createRootContext();

        assertThat(context.traceId()).isNotBlank();
        assertThat(context.spanId()).isNotBlank();
        assertThat(context.parentSpanId()).isNull();
        assertThat(context.serviceName()).isEqualTo("order-service");
        assertThat(context.createdAt()).isEqualTo(FIXED_INSTANT);
    }

    @Test
    void createsChildContextFromParent() {
        TraceContext parent = TraceContext.builder()
                .traceId("trace-1")
                .spanId("span-1")
                .serviceName("parent-service")
                .createdAt(FIXED_INSTANT)
                .build();

        TraceContext child = factory.createChildContext(parent);

        assertThat(child.traceId()).isEqualTo("trace-1");
        assertThat(child.spanId()).isNotEqualTo("span-1");
        assertThat(child.parentSpanId()).isEqualTo("span-1");
        assertThat(child.serviceName()).isEqualTo("order-service");
        assertThat(child.createdAt()).isEqualTo(FIXED_INSTANT);
    }

    @Test
    void createsContextFromHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(TraceHeaders.TRACE_ID, "trace-1");
        headers.put(TraceHeaders.SPAN_ID, "span-1");
        headers.put(TraceHeaders.PARENT_SPAN_ID, "parent-1");

        TraceContext context = factory.createFromHeaders(headers);

        assertThat(context.traceId()).isEqualTo("trace-1");
        assertThat(context.spanId()).isEqualTo("span-1");
        assertThat(context.parentSpanId()).isEqualTo("parent-1");
        assertThat(context.serviceName()).isEqualTo("order-service");
        assertThat(context.createdAt()).isEqualTo(FIXED_INSTANT);
    }

    @Test
    void fallsBackToRootContextWhenHeadersAreMissing() {
        TraceContext context = factory.createFromHeaders(Map.of());

        assertThat(context.traceId()).isNotBlank();
        assertThat(context.spanId()).isNotBlank();
        assertThat(context.parentSpanId()).isNull();
        assertThat(context.serviceName()).isEqualTo("order-service");
    }
}
