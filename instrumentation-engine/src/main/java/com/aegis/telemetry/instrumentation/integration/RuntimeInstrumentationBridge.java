package com.aegis.telemetry.instrumentation.integration;

import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.enums.RuntimeStatus;
import com.aegis.telemetry.instrumentation.metadata.RuntimeMetadataProvider;
import com.aegis.telemetry.instrumentation.thread.ThreadMetadataProvider;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.trace.context.TraceContext;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class RuntimeInstrumentationBridge {

    private final RuntimeTelemetrySdk sdk;
    private final RuntimeMetadataProvider metadataProvider;
    private final ThreadMetadataProvider threadMetadataProvider;

    public RuntimeInstrumentationBridge(RuntimeTelemetrySdk sdk, RuntimeMetadataProvider metadataProvider, ThreadMetadataProvider threadMetadataProvider) {
        this.sdk = Objects.requireNonNull(sdk, "sdk must not be null");
        this.metadataProvider = Objects.requireNonNull(metadataProvider, "metadataProvider must not be null");
        this.threadMetadataProvider = Objects.requireNonNull(threadMetadataProvider, "threadMetadataProvider must not be null");
    }

    public RuntimeEvent createRequestStarted(TraceContext traceContext, String httpMethod, String uri, String remoteAddress) {
        return buildEvent(traceContext, RuntimeEventType.REQUEST_STARTED, RuntimeStatus.UNKNOWN, 0L, payload("httpMethod", httpMethod, "uri", uri, "remoteAddress", remoteAddress));
    }

    public RuntimeEvent createRequestCompleted(TraceContext traceContext, String httpMethod, String uri, int statusCode, String remoteAddress, long latencyMillis) {
        return buildEvent(traceContext, RuntimeEventType.REQUEST_COMPLETED, RuntimeStatus.SUCCESS, latencyMillis, payload("httpMethod", httpMethod, "uri", uri, "statusCode", statusCode, "remoteAddress", remoteAddress, "latencyMillis", latencyMillis));
    }

    public RuntimeEvent createServiceCallStarted(TraceContext traceContext, String className, String methodName, Object[] arguments) {
        return buildEvent(traceContext, RuntimeEventType.SERVICE_CALL_STARTED, RuntimeStatus.UNKNOWN, 0L, payload("className", className, "methodName", methodName, "argumentCount", arguments == null ? 0 : arguments.length));
    }

    public RuntimeEvent createServiceCallCompleted(TraceContext traceContext, String className, String methodName, Object returnValue, long latencyMillis) {
        return buildEvent(traceContext, RuntimeEventType.SERVICE_CALL_COMPLETED, RuntimeStatus.SUCCESS, latencyMillis, payload("className", className, "methodName", methodName, "returnType", returnValue == null ? "void" : returnValue.getClass().getName(), "latencyMillis", latencyMillis));
    }

    public RuntimeEvent createErrorOccurred(TraceContext traceContext, Throwable throwable, String sourceClass, String sourceMethod, long latencyMillis, boolean captureStackTrace) {
        return buildEvent(traceContext, RuntimeEventType.ERROR_OCCURRED, RuntimeStatus.FAILED, latencyMillis, errorPayload(throwable, sourceClass, sourceMethod, latencyMillis, captureStackTrace));
    }

    public RuntimeEvent createHeartbeat(TraceContext traceContext) {
        return buildEvent(traceContext, RuntimeEventType.HEARTBEAT, RuntimeStatus.SUCCESS, 0L, payload("heartbeat", true));
    }

    private RuntimeEvent buildEvent(TraceContext traceContext, RuntimeEventType eventType, RuntimeStatus status, long latencyMillis, Map<String, Object> payload) {
        RuntimeMetadataProvider.RuntimeMetadata runtimeMetadata = metadataProvider.getMetadata();
        ThreadMetadataProvider.ThreadMetadata threadMetadata = threadMetadataProvider.capture();
        Map<String, Object> enrichedPayload = new LinkedHashMap<>(runtimeMetadata.asMap());
        enrichedPayload.put("threadId", threadMetadata.threadId());
        enrichedPayload.put("threadName", threadMetadata.threadName());
        enrichedPayload.put("threadGroup", threadMetadata.threadGroup());
        enrichedPayload.putAll(payload);

        RuntimeEvent event = sdk.newEventBuilder()
                .traceContext(traceContext)
                .serviceName(runtimeMetadata.serviceName())
                .instanceId(runtimeMetadata.instanceId())
                .eventType(eventType)
                .status(status)
                .latency(latencyMillis)
                .payload(enrichedPayload)
                .build();

        return sdk.createEvent(event);
    }

    private static Map<String, Object> payload(Object... values) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            payload.put(String.valueOf(values[index]), values[index + 1]);
        }
        return payload;
    }

    private static Map<String, Object> errorPayload(Throwable throwable, String sourceClass, String sourceMethod, long latencyMillis, boolean captureStackTrace) {
        Map<String, Object> payload = payload(
                "exceptionClass", throwable.getClass().getName(),
                "exceptionMessage", throwable.getMessage(),
                "sourceClass", sourceClass,
                "sourceMethod", sourceMethod,
                "latencyMillis", latencyMillis,
                "timestamp", Instant.now().toString()
        );
        if (captureStackTrace) {
            payload.put("stackTrace", stackTrace(throwable));
        }
        return payload;
    }

    private static String stackTrace(Throwable throwable) {
        java.io.StringWriter stringWriter = new java.io.StringWriter();
        try (java.io.PrintWriter printWriter = new java.io.PrintWriter(stringWriter)) {
            throwable.printStackTrace(printWriter);
        }
        return stringWriter.toString();
    }
}
