package com.aegis.telemetry.simulator;

import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.enums.RuntimeStatus;
import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.publisher.core.RuntimeEventPublisher;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import com.aegis.telemetry.sdk.serialization.RuntimeEventSerializer;
import com.aegis.telemetry.trace.context.TraceContext;
import com.aegis.telemetry.trace.factory.TraceContextFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

class PlatformPerformanceBenchmarkTest {

    private static final Logger logger = LoggerFactory.getLogger(PlatformPerformanceBenchmarkTest.class);

    private RuntimeTelemetrySdk sdk;
    private RuntimeEventSerializer serializer;
    private TraceContextFactory traceContextFactory;

    @BeforeEach
    void setUp() {
        sdk = RuntimeTelemetrySdk.getInstance();
        sdk.initialize(RuntimeSdkConfiguration.builder()
                .applicationName("benchmark-service")
                .instanceId("bench-001")
                .environment("benchmark")
                .build());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        serializer = new RuntimeEventSerializer(objectMapper);

        traceContextFactory = new TraceContextFactory("benchmark-service");
    }

    @Test
    void benchmarkEventCreationLatency() {
        int iterations = 10_000;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            TraceContext traceContext = traceContextFactory.createRootContext("benchmark-service");
            RuntimeEvent event = RuntimeEvent.builder()
                    .eventId(UUID.randomUUID())
                    .traceId(UUID.fromString(traceContext.traceId()))
                    .spanId(UUID.fromString(traceContext.spanId()))
                    .serviceName("benchmark-service")
                    .instanceId("bench-001")
                    .eventType(RuntimeEventType.REQUEST_COMPLETED)
                    .timestamp(Instant.now())
                    .threadName(Thread.currentThread().getName())
                    .threadId(Thread.currentThread().threadId())
                    .latency(15L)
                    .status(RuntimeStatus.SUCCESS)
                    .payload(Map.of("iteration", i))
                    .build();
            assertThat(event).isNotNull();
        }

        long totalDurationNs = System.nanoTime() - startTime;
        double avgLatencyNs = (double) totalDurationNs / iterations;
        double avgLatencyUs = avgLatencyNs / 1_000.0;

        logger.info("BENCHMARK [Event Creation]: Created {} events in {} ms | Avg Latency: {} us/event",
                iterations, totalDurationNs / 1_000_000, String.format("%.2f", avgLatencyUs));

        assertThat(avgLatencyUs).isLessThan(100.0); // Under 100 microseconds per event creation
    }

    @Test
    void benchmarkJsonSerializationThroughput() {
        TraceContext traceContext = traceContextFactory.createRootContext("benchmark-service");
        RuntimeEvent event = RuntimeEvent.builder()
                .eventId(UUID.randomUUID())
                .traceId(UUID.fromString(traceContext.traceId()))
                .spanId(UUID.fromString(traceContext.spanId()))
                .serviceName("benchmark-service")
                .instanceId("bench-001")
                .eventType(RuntimeEventType.REQUEST_COMPLETED)
                .timestamp(Instant.now())
                .threadName(Thread.currentThread().getName())
                .threadId(Thread.currentThread().threadId())
                .latency(25L)
                .status(RuntimeStatus.SUCCESS)
                .payload(Map.of("route", "/api/v1/orders", "method", "POST"))
                .build();

        int iterations = 10_000;
        long startTime = System.nanoTime();
        
        for (int i = 0; i < iterations; i++) {
            String json = serializer.toJson(event);
            assertThat(json).isNotEmpty();
        }

        long totalDurationNs = System.nanoTime() - startTime;
        double totalDurationSec = totalDurationNs / 1_000_000_000.0;
        double opsPerSec = iterations / totalDurationSec;

        logger.info("BENCHMARK [JSON Serialization]: Serialized {} events in {} sec | Throughput: {} ops/sec",
                iterations, String.format("%.3f", totalDurationSec), String.format("%.0f", opsPerSec));

        assertThat(opsPerSec).isGreaterThan(1_000.0); // Over 1,000 ops/sec throughput
    }

    @Test
    void benchmarkEventPublisherDrainRate() {
        RuntimeEventPublisher mockPublisher = mock(RuntimeEventPublisher.class);
        TelemetryFailureSimulator simulator = new TelemetryFailureSimulator();

        int eventCount = 1_000;
        TelemetryFailureSimulator.BurstResult result = simulator.simulateHighThroughputBurst(sdk, mockPublisher, eventCount);

        double opsPerSec = (eventCount / result.durationMs()) * 1_000.0;

        logger.info("BENCHMARK [Publisher Burst]: Published {} events in {} ms | Throughput: {} events/sec",
                result.published(), String.format("%.2f", result.durationMs()), String.format("%.0f", opsPerSec));

        assertThat(result.published()).isEqualTo(eventCount);
        assertThat(result.failed()).isZero();
    }
}
