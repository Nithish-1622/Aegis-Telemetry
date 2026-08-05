package com.aegis.telemetry.bootstrap.config;

import com.aegis.telemetry.contracts.config.RuntimeDefaults;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "aegis.runtime")
public class BootstrapProperties {

    @NotBlank
    private String serviceName = "aegis-telemetry";

    @NotBlank
    private String instanceId = "telemetry-001";

    @NotBlank
    private String environment = "bootstrap";

    private boolean capturePayload = RuntimeDefaults.PAYLOAD_CAPTURE_ENABLED;

    private boolean captureThreadInfo = RuntimeDefaults.THREAD_CAPTURE_ENABLED;

    private double samplingRate = RuntimeDefaults.DEFAULT_SAMPLING_RATE;

    private long heartbeatIntervalSeconds = RuntimeDefaults.HEARTBEAT_INTERVAL.toSeconds();

    private int maximumPayloadSize = RuntimeDefaults.MAXIMUM_PAYLOAD_SIZE;

    private boolean stackTraceEnabled = true;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public boolean isCapturePayload() {
        return capturePayload;
    }

    public void setCapturePayload(boolean capturePayload) {
        this.capturePayload = capturePayload;
    }

    public boolean isCaptureThreadInfo() {
        return captureThreadInfo;
    }

    public void setCaptureThreadInfo(boolean captureThreadInfo) {
        this.captureThreadInfo = captureThreadInfo;
    }

    public double getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(double samplingRate) {
        this.samplingRate = samplingRate;
    }

    public long getHeartbeatIntervalSeconds() {
        return heartbeatIntervalSeconds;
    }

    public void setHeartbeatIntervalSeconds(long heartbeatIntervalSeconds) {
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
    }

    public int getMaximumPayloadSize() {
        return maximumPayloadSize;
    }

    public void setMaximumPayloadSize(int maximumPayloadSize) {
        this.maximumPayloadSize = maximumPayloadSize;
    }

    public boolean isStackTraceEnabled() {
        return stackTraceEnabled;
    }

    public void setStackTraceEnabled(boolean stackTraceEnabled) {
        this.stackTraceEnabled = stackTraceEnabled;
    }

    public Duration heartbeatInterval() {
        return Duration.ofSeconds(heartbeatIntervalSeconds);
    }
}
