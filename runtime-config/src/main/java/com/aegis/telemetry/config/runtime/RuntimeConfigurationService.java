package com.aegis.telemetry.config.runtime;

import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class RuntimeConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(RuntimeConfigurationService.class);

    private final RuntimeTelemetrySdk sdk;
    private final AtomicReference<RuntimeConfiguration> configuration;

    public RuntimeConfigurationService(RuntimeTelemetrySdk sdk) {
        this.sdk = Objects.requireNonNull(sdk, "sdk must not be null");
        this.configuration = new AtomicReference<>(RuntimeConfiguration.builder().build());
        applyToSdk(this.configuration.get());
    }

    public RuntimeConfiguration getConfiguration() {
        return configuration.get();
    }

    public RuntimeConfiguration updateConfiguration(RuntimeConfiguration newConfiguration) {
        Objects.requireNonNull(newConfiguration, "newConfiguration must not be null");
        configuration.set(newConfiguration);
        applyToSdk(newConfiguration);
        logger.info("Updated runtime configuration: samplingRate={} capturePayload={} captureThreadInfo={}", newConfiguration.samplingRate(), newConfiguration.capturePayload(), newConfiguration.captureThreadInfo());
        return newConfiguration;
    }

    private void applyToSdk(RuntimeConfiguration runtimeConfiguration) {
        RuntimeSdkConfiguration current = sdk.getConfiguration();
        String applicationName = current == null ? "aegis-telemetry" : current.applicationName();
        String instanceId = current == null ? "default-instance" : current.instanceId();
        String environment = current == null ? "default" : current.environment();
        RuntimeSdkConfiguration sdkConfiguration = RuntimeSdkConfiguration.builder()
                .applicationName(applicationName)
                .instanceId(instanceId)
                .environment(environment)
                .samplingRate(runtimeConfiguration.samplingRate() / 100.0d)
                .capturePayload(runtimeConfiguration.capturePayload())
                .captureThreadInfo(runtimeConfiguration.captureThreadInfo())
                .heartbeatInterval(Duration.ofSeconds(runtimeConfiguration.heartbeatIntervalSeconds()))
                .build();
        sdk.updateConfiguration(sdkConfiguration);
    }
}
