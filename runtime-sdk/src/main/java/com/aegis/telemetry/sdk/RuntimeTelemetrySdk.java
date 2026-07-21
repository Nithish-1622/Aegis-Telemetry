package com.aegis.telemetry.sdk;

import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.sdk.builder.RuntimeEventBuilder;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import com.aegis.telemetry.sdk.context.RuntimeSdkContext;
import com.aegis.telemetry.sdk.factory.RuntimeEventFactory;
import com.aegis.telemetry.sdk.serialization.RuntimeEventSerializer;
import com.aegis.telemetry.sdk.validation.RuntimeEventValidator;
import com.aegis.telemetry.trace.context.TraceContext;
import com.aegis.telemetry.trace.context.TraceContextHolder;
import com.aegis.telemetry.trace.factory.TraceContextFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class RuntimeTelemetrySdk {

    private static final RuntimeTelemetrySdk INSTANCE = new RuntimeTelemetrySdk();

    private final RuntimeSdkContext context;
    private final RuntimeEventValidator validator;
    private final RuntimeEventSerializer serializer;
    private final TraceContextFactory traceContextFactory;
    private final RuntimeEventFactory eventFactory;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private RuntimeTelemetrySdk() {
        this.context = new RuntimeSdkContext();
        this.validator = new RuntimeEventValidator();
        this.serializer = new RuntimeEventSerializer(new ObjectMapper());
        this.traceContextFactory = new TraceContextFactory(resolveDefaultApplicationName());
        this.eventFactory = new RuntimeEventFactory(context, traceContextFactory);
    }

    public static RuntimeTelemetrySdk getInstance() {
        return INSTANCE;
    }

    public synchronized void initialize() {
        initialize(defaultConfiguration());
    }

    public synchronized void initialize(RuntimeSdkConfiguration configuration) {
        if (initialized.get()) {
            return;
        }
        RuntimeSdkConfiguration resolvedConfiguration = configuration == null ? defaultConfiguration() : configuration;
        context.initialize(resolvedConfiguration, traceContextFactory.createRootContext(resolvedConfiguration.applicationName()));
        TraceContextHolder.setContext(context.getTraceContext().orElse(null));
        initialized.set(true);
    }

    public synchronized void shutdown() {
        TraceContextHolder.clear();
        context.shutdown();
        initialized.set(false);
    }

    public boolean isInitialized() {
        return initialized.get();
    }

    public RuntimeSdkConfiguration getConfiguration() {
        return context.getConfiguration();
    }

    public Optional<TraceContext> getCurrentTraceContext() {
        return context.getTraceContext().or(() -> TraceContextHolder.getContext());
    }

    public RuntimeSdkContext getContext() {
        return context;
    }

    public RuntimeEventValidator getValidator() {
        return validator;
    }

    public RuntimeEventSerializer getSerializer() {
        return serializer;
    }

    public RuntimeEventFactory getEventFactory() {
        return eventFactory;
    }

    public RuntimeEventBuilder.Builder newEventBuilder() {
        return RuntimeEventBuilder.builder();
    }

    public RuntimeEvent createEvent(RuntimeEvent event) {
        validator.validate(event);
        return event;
    }

    private static RuntimeSdkConfiguration defaultConfiguration() {
        return RuntimeSdkConfiguration.builder()
                .applicationName(resolveDefaultApplicationName())
                .instanceId(resolveDefaultInstanceId())
                .environment(resolveDefaultEnvironment())
                .build();
    }

    private static String resolveDefaultApplicationName() {
        return System.getProperty("aegis.telemetry.application-name", "aegis-telemetry");
    }

    private static String resolveDefaultInstanceId() {
        return System.getProperty("aegis.telemetry.instance-id", UUID.randomUUID().toString());
    }

    private static String resolveDefaultEnvironment() {
        return System.getProperty("aegis.telemetry.environment", "default");
    }
}
