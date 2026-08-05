package com.aegis.telemetry.config.runtime;

import com.aegis.telemetry.contracts.config.RuntimeDefaults;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "capturePayload",
        "captureThreadInfo",
        "samplingRate",
        "heartbeatIntervalSeconds",
        "stackTraceEnabled",
        "maximumPayloadSize"
})
public record RuntimeConfiguration(
        boolean capturePayload,
        boolean captureThreadInfo,
        @DecimalMin("0.0") @DecimalMax("100.0") double samplingRate,
        @Min(1) long heartbeatIntervalSeconds,
        boolean stackTraceEnabled,
        @Min(1) int maximumPayloadSize
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public RuntimeConfiguration {
        if (samplingRate <= 0.0d) {
            samplingRate = 100.0d;
        }
        if (heartbeatIntervalSeconds <= 0L) {
            heartbeatIntervalSeconds = RuntimeDefaults.HEARTBEAT_INTERVAL.toSeconds();
        }
        if (maximumPayloadSize <= 0) {
            maximumPayloadSize = RuntimeDefaults.MAXIMUM_PAYLOAD_SIZE;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private boolean capturePayload = RuntimeDefaults.PAYLOAD_CAPTURE_ENABLED;
        private boolean captureThreadInfo = RuntimeDefaults.THREAD_CAPTURE_ENABLED;
        private double samplingRate = 100.0d;
        private long heartbeatIntervalSeconds = RuntimeDefaults.HEARTBEAT_INTERVAL.toSeconds();
        private boolean stackTraceEnabled = true;
        private int maximumPayloadSize = RuntimeDefaults.MAXIMUM_PAYLOAD_SIZE;

        private Builder() {
        }

        public Builder capturePayload(boolean capturePayload) {
            this.capturePayload = capturePayload;
            return this;
        }

        public Builder captureThreadInfo(boolean captureThreadInfo) {
            this.captureThreadInfo = captureThreadInfo;
            return this;
        }

        public Builder samplingRate(double samplingRate) {
            this.samplingRate = samplingRate;
            return this;
        }

        public Builder heartbeatIntervalSeconds(long heartbeatIntervalSeconds) {
            this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
            return this;
        }

        public Builder stackTraceEnabled(boolean stackTraceEnabled) {
            this.stackTraceEnabled = stackTraceEnabled;
            return this;
        }

        public Builder maximumPayloadSize(int maximumPayloadSize) {
            this.maximumPayloadSize = maximumPayloadSize;
            return this;
        }

        public RuntimeConfiguration build() {
            return new RuntimeConfiguration(capturePayload, captureThreadInfo, samplingRate, heartbeatIntervalSeconds, stackTraceEnabled, maximumPayloadSize);
        }
    }
}
