package com.aegis.telemetry.sdk.context;

import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import com.aegis.telemetry.trace.context.TraceContext;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class RuntimeSdkContext {

    private final AtomicReference<RuntimeSdkConfiguration> configuration = new AtomicReference<>();
    private final AtomicReference<TraceContext> traceContext = new AtomicReference<>();
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public RuntimeSdkConfiguration getConfiguration() {
        return configuration.get();
    }

    public Optional<TraceContext> getTraceContext() {
        return Optional.ofNullable(traceContext.get());
    }

    public RuntimeSdkConfiguration updateConfiguration(RuntimeSdkConfiguration configuration) {
        Objects.requireNonNull(configuration, "configuration must not be null");
        this.configuration.set(configuration);
        return configuration;
    }

    public boolean isInitialized() {
        return initialized.get();
    }

    public void initialize(RuntimeSdkConfiguration configuration, TraceContext currentTraceContext) {
        updateConfiguration(configuration);
        this.traceContext.set(currentTraceContext);
        initialized.set(true);
    }

    public void updateTraceContext(TraceContext currentTraceContext) {
        this.traceContext.set(currentTraceContext);
    }

    public void clearTraceContext() {
        this.traceContext.set(null);
    }

    public void shutdown() {
        clearTraceContext();
        initialized.set(false);
    }
}
