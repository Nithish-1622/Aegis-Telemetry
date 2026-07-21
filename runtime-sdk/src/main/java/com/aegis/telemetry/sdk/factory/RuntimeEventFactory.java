package com.aegis.telemetry.sdk.factory;

import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.enums.RuntimeStatus;
import com.aegis.telemetry.sdk.builder.RuntimeEventBuilder;
import com.aegis.telemetry.sdk.context.RuntimeSdkContext;
import com.aegis.telemetry.trace.context.TraceContext;
import com.aegis.telemetry.trace.context.TraceContextHolder;
import com.aegis.telemetry.trace.factory.TraceContextFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class RuntimeEventFactory {

    private final RuntimeSdkContext sdkContext;
    private final TraceContextFactory traceContextFactory;
    private final Clock clock;

    public RuntimeEventFactory(RuntimeSdkContext sdkContext, TraceContextFactory traceContextFactory) {
        this(sdkContext, traceContextFactory, Clock.systemUTC());
    }

    public RuntimeEventFactory(RuntimeSdkContext sdkContext, TraceContextFactory traceContextFactory, Clock clock) {
        this.sdkContext = Objects.requireNonNull(sdkContext, "sdkContext must not be null");
        this.traceContextFactory = Objects.requireNonNull(traceContextFactory, "traceContextFactory must not be null");
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public RuntimeEvent createRequestStarted() {
        return create(RuntimeEventType.REQUEST_STARTED, RuntimeStatus.UNKNOWN);
    }

    public RuntimeEvent createRequestCompleted() {
        return create(RuntimeEventType.REQUEST_COMPLETED, RuntimeStatus.SUCCESS);
    }

    public RuntimeEvent createServiceCallStarted() {
        return create(RuntimeEventType.SERVICE_CALL_STARTED, RuntimeStatus.UNKNOWN);
    }

    public RuntimeEvent createServiceCallCompleted() {
        return create(RuntimeEventType.SERVICE_CALL_COMPLETED, RuntimeStatus.SUCCESS);
    }

    public RuntimeEvent createRetryTriggered() {
        return create(RuntimeEventType.RETRY_TRIGGERED, RuntimeStatus.RETRYING);
    }

    public RuntimeEvent createErrorOccurred() {
        return create(RuntimeEventType.ERROR_OCCURRED, RuntimeStatus.FAILED);
    }

    public RuntimeEvent createHeartbeat() {
        return create(RuntimeEventType.HEARTBEAT, RuntimeStatus.SUCCESS);
    }

    private RuntimeEvent create(RuntimeEventType eventType, RuntimeStatus status) {
        TraceContext traceContext = currentOrRootTraceContext();
        sdkContext.updateTraceContext(traceContext);
        TraceContextHolder.setContext(traceContext);
        return RuntimeEventBuilder.builder()
                .traceContext(traceContext)
                .eventType(eventType)
                .status(status)
                .timestamp(Instant.now(clock))
                .build();
    }

    private TraceContext currentOrRootTraceContext() {
        return sdkContext.getTraceContext()
                .or(() -> TraceContextHolder.getContext())
                .orElseGet(traceContextFactory::createRootContext);
    }
}
