package com.aegis.telemetry.trace.context;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TraceContextHolderTest {

    @AfterEach
    void tearDown() {
        TraceContextHolder.clear();
    }

    @Test
    void setGetReplaceAndClearContext() {
        TraceContext first = context("trace-1", "span-1");
        TraceContext second = context("trace-2", "span-2");

        TraceContextHolder.setContext(first);
        assertThat(TraceContextHolder.hasContext()).isTrue();
        assertThat(TraceContextHolder.getContext()).contains(first);

        TraceContextHolder.setContext(second);
        assertThat(TraceContextHolder.getContext()).contains(second);

        TraceContextHolder.clear();
        assertThat(TraceContextHolder.hasContext()).isFalse();
        assertThat(TraceContextHolder.getContext()).isEmpty();
    }

    @Test
    void isolatesContextsPerThread() throws Exception {
        TraceContextHolder.setContext(context("main-trace", "main-span"));

        AtomicReference<TraceContext> childThreadContext = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Thread worker = new Thread(() -> {
            childThreadContext.set(TraceContextHolder.getContext().orElse(null));
            TraceContextHolder.setContext(context("worker-trace", "worker-span"));
            latch.countDown();
        });
        worker.start();

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        worker.join();

        assertThat(childThreadContext.get()).isNull();
        assertThat(TraceContextHolder.getContext()).hasValueSatisfying(context -> {
            assertThat(context.traceId()).isEqualTo("main-trace");
            assertThat(context.spanId()).isEqualTo("main-span");
        });
    }

    private static TraceContext context(String traceId, String spanId) {
        return TraceContext.builder()
                .traceId(traceId)
                .spanId(spanId)
                .serviceName("service")
                .createdAt(Instant.parse("2026-07-21T00:00:00Z"))
                .build();
    }
}
