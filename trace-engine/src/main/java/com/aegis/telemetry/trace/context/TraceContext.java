package com.aegis.telemetry.trace.context;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"traceId", "spanId", "parentSpanId", "serviceName", "createdAt"})
public record TraceContext(
        String traceId,
        String spanId,
        String parentSpanId,
        String serviceName,
        Instant createdAt
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String traceId;
        private String spanId;
        private String parentSpanId;
        private String serviceName;
        private Instant createdAt;

        private Builder() {
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder spanId(String spanId) {
            this.spanId = spanId;
            return this;
        }

        public Builder parentSpanId(String parentSpanId) {
            this.parentSpanId = parentSpanId;
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TraceContext build() {
            return new TraceContext(traceId, spanId, parentSpanId, serviceName, createdAt);
        }
    }
}
