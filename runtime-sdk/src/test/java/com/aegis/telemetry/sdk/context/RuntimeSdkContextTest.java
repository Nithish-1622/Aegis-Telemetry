package com.aegis.telemetry.sdk.context;

import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import com.aegis.telemetry.trace.context.TraceContext;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeSdkContextTest {

    @Test
    void initializesAndUpdatesConfiguration() throws Exception {
        RuntimeSdkContext context = new RuntimeSdkContext();
        RuntimeSdkConfiguration configuration = RuntimeSdkConfiguration.builder()
                .applicationName("service")
                .instanceId("instance-1")
                .environment("test")
                .heartbeatInterval(Duration.ofSeconds(30))
                .build();
        TraceContext traceContext = TraceContext.builder()
                .traceId("trace-1")
                .spanId("span-1")
                .serviceName("service")
                .createdAt(Instant.parse("2026-07-21T00:00:00Z"))
                .build();

        context.initialize(configuration, traceContext);

        assertThat(context.isInitialized()).isTrue();
        assertThat(context.getConfiguration()).isEqualTo(configuration);
        assertThat(context.getTraceContext()).contains(traceContext);

        RuntimeSdkConfiguration updated = RuntimeSdkConfiguration.builder()
                .applicationName("service-2")
                .instanceId("instance-2")
                .environment("prod")
                .heartbeatInterval(Duration.ofSeconds(60))
                .build();

        context.updateConfiguration(updated);
        assertThat(context.getConfiguration()).isEqualTo(updated);

        AtomicReference<RuntimeSdkConfiguration> captured = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Thread worker = new Thread(() -> {
            captured.set(context.getConfiguration());
            latch.countDown();
        });
        worker.start();

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        worker.join();
        assertThat(captured.get()).isEqualTo(updated);
    }
}
