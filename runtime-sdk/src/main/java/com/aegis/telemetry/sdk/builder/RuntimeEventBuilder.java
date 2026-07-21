package com.aegis.telemetry.sdk.builder;

import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.enums.RuntimeStatus;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.context.RuntimeSdkContext;
import com.aegis.telemetry.sdk.validation.RuntimeEventValidationException;
import com.aegis.telemetry.sdk.validation.RuntimeEventValidator;
import com.aegis.telemetry.trace.context.TraceContext;
import com.aegis.telemetry.trace.context.TraceContextHolder;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.UUID;

public final class RuntimeEventBuilder {

    private RuntimeEventBuilder() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID eventId;
        private TraceContext traceContext;
        private String serviceName;
        private String instanceId;
        private RuntimeEventType eventType;
        private Instant timestamp;
        private String threadName;
        private Long threadId;
        private Long latency;
        private RuntimeStatus status;
        private Map<String, Object> payload;

        private Builder() {
        }

        public Builder eventId(UUID eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder traceContext(TraceContext traceContext) {
            this.traceContext = traceContext;
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public Builder eventType(RuntimeEventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder threadName(String threadName) {
            this.threadName = threadName;
            return this;
        }

        public Builder threadId(Long threadId) {
            this.threadId = threadId;
            return this;
        }

        public Builder latency(Long latency) {
            this.latency = latency;
            return this;
        }

        public Builder status(RuntimeStatus status) {
            this.status = status;
            return this;
        }

        public Builder payload(Map<String, Object> payload) {
            this.payload = payload;
            return this;
        }

        public RuntimeEvent build() {
            RuntimeTelemetrySdk sdk = RuntimeTelemetrySdk.getInstance();
            RuntimeSdkContext sdkContext = sdk.getContext();
            TraceContext resolvedTraceContext = resolveTraceContext(sdkContext);
            if (eventType == null) {
                throw new RuntimeEventValidationException("eventType must not be null");
            }
            if (status == null) {
                throw new RuntimeEventValidationException("status must not be null");
            }
            RuntimeEvent event = RuntimeEvent.builder()
                    .eventId(eventId == null ? UUID.randomUUID() : eventId)
                    .traceId(parseUuid(resolvedTraceContext.traceId(), "traceId"))
                    .spanId(parseUuid(resolvedTraceContext.spanId(), "spanId"))
                    .parentSpanId(resolvedTraceContext.parentSpanId() == null ? null : parseUuid(resolvedTraceContext.parentSpanId(), "parentSpanId"))
                    .serviceName(resolveServiceName(sdkContext))
                    .instanceId(resolveInstanceId(sdkContext))
                    .eventType(eventType)
                    .timestamp(timestamp == null ? Instant.now() : timestamp)
                    .threadName(resolveThreadName(sdkContext))
                    .threadId(resolveThreadId())
                    .latency(latency == null ? 0L : latency)
                    .status(status)
                    .payload(resolvePayload(sdkContext))
                    .build();

            RuntimeEventValidator validator = sdk.getValidator();
            validator.validate(event);
            return event;
        }

        private TraceContext resolveTraceContext(RuntimeSdkContext sdkContext) {
            if (traceContext != null) {
                return traceContext;
            }
            return TraceContextHolder.getContext()
                    .or(() -> sdkContext.getTraceContext())
                    .orElseThrow(() -> new RuntimeEventValidationException("trace context is required before building runtime events"));
        }

        private String resolveServiceName(RuntimeSdkContext sdkContext) {
            if (serviceName != null && !serviceName.isBlank()) {
                return serviceName;
            }
            return sdkContext.getConfiguration().applicationName();
        }

        private String resolveInstanceId(RuntimeSdkContext sdkContext) {
            if (instanceId != null && !instanceId.isBlank()) {
                return instanceId;
            }
            return sdkContext.getConfiguration().instanceId();
        }

        private String resolveThreadName(RuntimeSdkContext sdkContext) {
            if (!sdkContext.getConfiguration().captureThreadInfo()) {
                return null;
            }
            return threadName == null ? Thread.currentThread().getName() : threadName;
        }

        private long resolveThreadId() {
            return threadId == null ? Thread.currentThread().threadId() : threadId;
        }

        private Map<String, Object> resolvePayload(RuntimeSdkContext sdkContext) {
            if (!sdkContext.getConfiguration().capturePayload()) {
                return Map.of();
            }
            return payload == null ? Map.of() : payload;
        }

        private static UUID parseUuid(String value, String fieldName) {
            try {
                return UUID.fromString(value);
            } catch (RuntimeException exception) {
                throw new RuntimeEventValidationException(fieldName + " must be UUID formatted");
            }
        }
    }
}
