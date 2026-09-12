error id: file:///D:/Projects/Aegis-Telemetry/failure-simulator/src/main/java/com/aegis/telemetry/simulator/TelemetryFailureSimulator.java:java/util/UUID#randomUUID().
file:///D:/Projects/Aegis-Telemetry/failure-simulator/src/main/java/com/aegis/telemetry/simulator/TelemetryFailureSimulator.java
empty definition using pc, found symbol in pc: java/util/UUID#randomUUID().
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 4136
uri: file:///D:/Projects/Aegis-Telemetry/failure-simulator/src/main/java/com/aegis/telemetry/simulator/TelemetryFailureSimulator.java
text:
```scala
package com.aegis.telemetry.simulator;

import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.enums.RuntimeStatus;
import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.publisher.core.RuntimeEventPublisher;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.trace.context.TraceContext;
import com.aegis.telemetry.trace.factory.TraceContextFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TelemetryFailureSimulator {

    public RuntimeEvent createOversizedPayloadEvent(RuntimeTelemetrySdk sdk, int payloadSizeBytes) {
        StringBuilder builder = new StringBuilder(payloadSizeBytes);
        for (int i = 0; i < payloadSizeBytes; i++) {
            builder.append('X');
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("largeData", builder.toString());

        TraceContextFactory factory = new TraceContextFactory(sdk.getConfiguration().applicationName());
        TraceContext traceContext = factory.createRootContext(sdk.getConfiguration().applicationName());

        return RuntimeEvent.builder()
                .eventId(UUID.randomUUID())
                .traceId(UUID.fromString(traceContext.traceId()))
                .spanId(UUID.fromString(traceContext.spanId()))
                .serviceName(sdk.getConfiguration().applicationName())
                .instanceId(sdk.getConfiguration().instanceId())
                .eventType(RuntimeEventType.ERROR_OCCURRED)
                .timestamp(Instant.now())
                .threadName(Thread.currentThread().getName())
                .threadId(Thread.currentThread().threadId())
                .latency(500L)
                .status(RuntimeStatus.FAILED)
                .payload(payload)
                .build();
    }

    public RuntimeEvent simulateExceptionCascade(RuntimeTelemetrySdk sdk, Throwable exception) {
        TraceContextFactory factory = new TraceContextFactory(sdk.getConfiguration().applicationName());
        TraceContext traceContext = factory.createRootContext(sdk.getConfiguration().applicationName());

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String stackTrace = sw.toString();

        String errorMessage = exception.getMessage() != null ? exception.getMessage() : exception.getClass().getSimpleName();

        return RuntimeEvent.builder()
                .eventId(UUID.randomUUID())
                .traceId(UUID.fromString(traceContext.traceId()))
                .spanId(UUID.fromString(traceContext.spanId()))
                .serviceName(sdk.getConfiguration().applicationName())
                .instanceId(sdk.getConfiguration().instanceId())
                .eventType(RuntimeEventType.ERROR_OCCURRED)
                .timestamp(Instant.now())
                .threadName(Thread.currentThread().getName())
                .threadId(Thread.currentThread().threadId())
                .latency(120L)
                .status(RuntimeStatus.FAILED)
                .payload(Map.of(
                        "exceptionType", exception.getClass().getName(),
                        "errorMessage", errorMessage,
                        "stackTrace", stackTrace
                ))
                .build();
    }

    public BurstResult simulateHighThroughputBurst(RuntimeTelemetrySdk sdk, RuntimeEventPublisher publisher, int eventCount) {
        long startTime = System.nanoTime();
        int published = 0;
        int failed = 0;

        TraceContextFactory factory = new TraceContextFactory(sdk.getConfiguration().applicationName());
        TraceContext traceContext = factory.createRootContext(sdk.getConfiguration().applicationName());

        for (int i = 0; i < eventCount; i++) {
            RuntimeEvent event = RuntimeEvent.builder()
                    .eventId(UUID.rando@@mUUID())
                    .traceId(UUID.fromString(traceContext.traceId()))
                    .spanId(UUID.fromString(traceContext.spanId()))
                    .serviceName(sdk.getConfiguration().applicationName())
                    .instanceId(sdk.getConfiguration().instanceId())
                    .eventType(RuntimeEventType.HEARTBEAT)
                    .timestamp(Instant.now())
                    .threadName(Thread.currentThread().getName())
                    .threadId(Thread.currentThread().threadId())
                    .latency(5L)
                    .status(RuntimeStatus.SUCCESS)
                    .payload(Map.of("sequence", i))
                    .build();

            try {
                publisher.publish(event);
                published++;
            } catch (Exception e) {
                failed++;
            }
        }

        long durationNs = System.nanoTime() - startTime;
        double durationMs = durationNs / 1_000_000.0;
        return new BurstResult(eventCount, published, failed, durationMs);
    }

    public record BurstResult(int totalEvents, int published, int failed, double durationMs) {}
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: java/util/UUID#randomUUID().