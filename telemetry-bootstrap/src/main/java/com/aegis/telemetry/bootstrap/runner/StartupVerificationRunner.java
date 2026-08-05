package com.aegis.telemetry.bootstrap.runner;

import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.enums.RuntimeStatus;
import com.aegis.telemetry.publisher.core.RuntimeEventPublisher;
import com.aegis.telemetry.registry.integration.RegistryIntegrationBridge;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.serialization.RuntimeEventSerializer;
import com.aegis.telemetry.trace.context.TraceContext;
import com.aegis.telemetry.trace.factory.TraceContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnBean({RuntimeTelemetrySdk.class, RuntimeEventPublisher.class, RegistryIntegrationBridge.class})
public class StartupVerificationRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupVerificationRunner.class);

    private final RuntimeTelemetrySdk sdk;
    private final RuntimeEventPublisher publisher;
    private final RuntimeEventSerializer serializer;
    private final RegistryIntegrationBridge registryIntegrationBridge;
    private final TraceContextFactory traceContextFactory;

    public StartupVerificationRunner(RuntimeTelemetrySdk sdk, RuntimeEventPublisher publisher, RuntimeEventSerializer serializer, RegistryIntegrationBridge registryIntegrationBridge, TraceContextFactory traceContextFactory) {
        this.sdk = sdk;
        this.publisher = publisher;
        this.serializer = serializer;
        this.registryIntegrationBridge = registryIntegrationBridge;
        this.traceContextFactory = traceContextFactory;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!sdk.isInitialized()) {
            throw new IllegalStateException("runtime sdk must be initialized");
        }

        TraceContext traceContext = traceContextFactory.createRootContext(sdk.getConfiguration().applicationName());
        RuntimeEvent event = RuntimeEvent.builder()
                .eventId(UUID.randomUUID())
                .traceId(UUID.fromString(traceContext.traceId()))
                .spanId(UUID.fromString(traceContext.spanId()))
                .parentSpanId(null)
                .serviceName(sdk.getConfiguration().applicationName())
                .instanceId(sdk.getConfiguration().instanceId())
                .eventType(RuntimeEventType.HEARTBEAT)
                .timestamp(Instant.now())
                .threadName(Thread.currentThread().getName())
                .threadId(Thread.currentThread().threadId())
                .latency(0L)
                .status(RuntimeStatus.SUCCESS)
                .payload(Map.of("startupVerification", true))
                .build();

        serializer.toJson(event);
        logger.info("Startup verification passed: sdkInitialized={}, publisherReady={}, registryBridgeReady={}", sdk.isInitialized(), publisher != null, registryIntegrationBridge != null);
    }
}
