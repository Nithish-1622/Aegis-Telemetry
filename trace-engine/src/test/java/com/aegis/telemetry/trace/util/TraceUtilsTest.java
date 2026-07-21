package com.aegis.telemetry.trace.util;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.aegis.telemetry.trace.context.TraceContext;
import com.aegis.telemetry.trace.context.TraceContextHolder;

class TraceUtilsTest {

    @AfterEach
    void tearDown() {
        TraceContextHolder.clear();
    }

    @Test
    void detectsRootAndParentContexts() {
        TraceContext root = context(null);
        TraceContext child = context("span-1");

        assertThat(TraceUtils.isRootTrace(root)).isTrue();
        assertThat(TraceUtils.hasParent(root)).isFalse();
        assertThat(TraceUtils.isRootTrace(child)).isFalse();
        assertThat(TraceUtils.hasParent(child)).isTrue();
    }

    @Test
    void returnsCurrentTraceIdentifiers() {
        TraceContextHolder.setContext(TraceContext.builder()
                .traceId("trace-1")
                .spanId("span-1")
                .serviceName("service")
                .createdAt(Instant.parse("2026-07-21T00:00:00Z"))
                .build());

        assertThat(TraceUtils.currentTraceId()).contains("trace-1");
        assertThat(TraceUtils.currentSpanId()).contains("span-1");
    }

    private static TraceContext context(String parentSpanId) {
        return TraceContext.builder()
                .traceId("trace-1")
                .spanId("span-1")
                .parentSpanId(parentSpanId)
                .serviceName("service")
                .createdAt(Instant.parse("2026-07-21T00:00:00Z"))
                .build();
    }
}
