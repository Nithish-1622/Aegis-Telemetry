package com.aegis.telemetry.sdk.config;

import com.aegis.telemetry.contracts.config.RuntimeDefaults;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "aegis.telemetry.sdk")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "applicationName",
        "instanceId",
        "environment",
        "samplingRate",
        "capturePayload",
        "captureThreadInfo",
        "heartbeatInterval"
})
public record RuntimeSdkConfiguration(
        @NotBlank String applicationName,
        @NotBlank String instanceId,
        @NotBlank String environment,
        @DecimalMin("0.0") @DecimalMax("1.0") double samplingRate,
        boolean capturePayload,
        boolean captureThreadInfo,
        @NotNull Duration heartbeatInterval
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public RuntimeSdkConfiguration {
        if (heartbeatInterval == null) {
            heartbeatInterval = RuntimeDefaults.HEARTBEAT_INTERVAL;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String applicationName;
        private String instanceId;
        private String environment;
        private double samplingRate = RuntimeDefaults.DEFAULT_SAMPLING_RATE;
        private boolean capturePayload = RuntimeDefaults.PAYLOAD_CAPTURE_ENABLED;
        private boolean captureThreadInfo = RuntimeDefaults.THREAD_CAPTURE_ENABLED;
        private Duration heartbeatInterval = RuntimeDefaults.HEARTBEAT_INTERVAL;

        private Builder() {
        }

        public Builder applicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public Builder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder samplingRate(double samplingRate) {
            this.samplingRate = samplingRate;
            return this;
        }

        public Builder capturePayload(boolean capturePayload) {
            this.capturePayload = capturePayload;
            return this;
        }

        public Builder captureThreadInfo(boolean captureThreadInfo) {
            this.captureThreadInfo = captureThreadInfo;
            return this;
        }

        public Builder heartbeatInterval(Duration heartbeatInterval) {
            this.heartbeatInterval = heartbeatInterval;
            return this;
        }

        public RuntimeSdkConfiguration build() {
            return new RuntimeSdkConfiguration(applicationName, instanceId, environment, samplingRate, capturePayload, captureThreadInfo, heartbeatInterval);
        }
    }
}
