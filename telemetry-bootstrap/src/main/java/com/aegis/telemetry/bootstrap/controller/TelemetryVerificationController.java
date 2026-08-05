package com.aegis.telemetry.bootstrap.controller;

import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.enums.RuntimeStatus;
import com.aegis.telemetry.publisher.core.RuntimeEventPublisher;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.serialization.RuntimeEventSerializer;
import com.aegis.telemetry.trace.context.TraceContext;
import com.aegis.telemetry.trace.factory.TraceContextFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/demo")
public class TelemetryVerificationController {

    private final RuntimeTelemetrySdk sdk;
    private final RuntimeEventPublisher publisher;
    private final RuntimeEventSerializer serializer;
    private final TraceContextFactory traceContextFactory;

    public TelemetryVerificationController(RuntimeTelemetrySdk sdk, RuntimeEventPublisher publisher, RuntimeEventSerializer serializer, TraceContextFactory traceContextFactory) {
        this.sdk = sdk;
        this.publisher = publisher;
        this.serializer = serializer;
        this.traceContextFactory = traceContextFactory;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        RuntimeEvent event = createVerificationEvent(RuntimeEventType.REQUEST_STARTED, "ping");
        publisher.publish(event);
        return ResponseEntity.ok(Map.of(
                "message", "pong",
                "traceId", event.traceId().toString(),
                "eventId", event.eventId().toString()
        ));
    }

    @GetMapping("/trace")
    public RuntimeEvent trace() {
        RuntimeEvent event = createVerificationEvent(RuntimeEventType.REQUEST_COMPLETED, "trace");
        publisher.publish(event);
        return event;
    }

    @GetMapping("/error")
    public ResponseEntity<RuntimeEvent> error() {
        RuntimeEvent event = createVerificationEvent(RuntimeEventType.ERROR_OCCURRED, "error");
        publisher.publish(event);
        return ResponseEntity.internalServerError().body(event);
    }

    private RuntimeEvent createVerificationEvent(RuntimeEventType eventType, String endpoint) {
        TraceContext traceContext = traceContextFactory.createRootContext(sdk.getConfiguration().applicationName());
        return RuntimeEvent.builder()
                .eventId(UUID.randomUUID())
                .traceId(UUID.fromString(traceContext.traceId()))
                .spanId(UUID.fromString(traceContext.spanId()))
                .parentSpanId(null)
                .serviceName(sdk.getConfiguration().applicationName())
                .instanceId(sdk.getConfiguration().instanceId())
                .eventType(eventType)
                .timestamp(Instant.now())
                .threadName(Thread.currentThread().getName())
                .threadId(Thread.currentThread().threadId())
                .latency(0L)
                .status(RuntimeStatus.SUCCESS)
                .payload(Map.of("endpoint", endpoint))
                .build();
    }
}
