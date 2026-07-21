package com.aegis.telemetry.contracts.event;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.enums.RuntimeStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "eventId",
        "traceId",
        "spanId",
        "parentSpanId",
        "serviceName",
        "instanceId",
        "eventType",
        "timestamp",
        "threadName",
        "threadId",
        "latency",
        "status",
        "payload"
})
public record RuntimeEvent(
        @NotNull UUID eventId,
        @NotNull UUID traceId,
        @NotNull UUID spanId,
        UUID parentSpanId,
        @NotBlank String serviceName,
        String instanceId,
        @NotNull RuntimeEventType eventType,
        @NotNull Instant timestamp,
        String threadName,
        @Positive long threadId,
        @PositiveOrZero long latency,
        @NotNull RuntimeStatus status,
        Map<String, Object> payload
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public RuntimeEvent {
        payload = payload == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(payload));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID eventId;
        private UUID traceId;
        private UUID spanId;
        private UUID parentSpanId;
        private String serviceName;
        private String instanceId;
        private RuntimeEventType eventType;
        private Instant timestamp;
        private String threadName;
        private Long threadId;
        private Long latency;
        private RuntimeStatus status;
        private Map<String, Object> payload = Map.of();

        private Builder() {
        }

        public Builder eventId(UUID eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder traceId(UUID traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder spanId(UUID spanId) {
            this.spanId = spanId;
            return this;
        }

        public Builder parentSpanId(UUID parentSpanId) {
            this.parentSpanId = parentSpanId;
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

        public Builder threadId(long threadId) {
            this.threadId = threadId;
            return this;
        }

        public Builder latency(long latency) {
            this.latency = latency;
            return this;
        }

        public Builder status(RuntimeStatus status) {
            this.status = status;
            return this;
        }

        public Builder payload(Map<String, Object> payload) {
            this.payload = payload == null ? Map.of() : payload;
            return this;
        }

        public RuntimeEvent build() {
            return new RuntimeEvent(eventId, traceId, spanId, parentSpanId, serviceName, instanceId, eventType, timestamp, threadName, threadId == null ? 0L : threadId, latency == null ? 0L : latency, status, payload);
        }
    }
}
