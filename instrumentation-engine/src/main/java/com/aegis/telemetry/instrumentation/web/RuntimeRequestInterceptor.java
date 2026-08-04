package com.aegis.telemetry.instrumentation.web;

import com.aegis.telemetry.contracts.trace.TraceHeaders;
import com.aegis.telemetry.instrumentation.exception.RuntimeExceptionHandler;
import com.aegis.telemetry.instrumentation.integration.RuntimeInstrumentationBridge;
import com.aegis.telemetry.instrumentation.metrics.LatencyTracker;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.trace.context.TraceContext;
import com.aegis.telemetry.trace.context.TraceContextHolder;
import com.aegis.telemetry.trace.factory.TraceContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class RuntimeRequestInterceptor implements HandlerInterceptor {

    public static final String TRACE_CONTEXT_ATTRIBUTE = RuntimeRequestInterceptor.class.getName() + ".traceContext";
    public static final String LATENCY_TRACKER_ATTRIBUTE = RuntimeRequestInterceptor.class.getName() + ".latencyTracker";
    public static final String REQUEST_STARTED_EVENT_ATTRIBUTE = RuntimeRequestInterceptor.class.getName() + ".requestStartedEvent";
    public static final String REQUEST_COMPLETED_EVENT_ATTRIBUTE = RuntimeRequestInterceptor.class.getName() + ".requestCompletedEvent";
    public static final String ERROR_EVENT_ATTRIBUTE = RuntimeRequestInterceptor.class.getName() + ".errorEvent";

    private final RuntimeTelemetrySdk sdk;
    private final TraceContextFactory traceContextFactory;
    private final RuntimeInstrumentationBridge bridge;
    private final RuntimeExceptionHandler exceptionHandler;

    public RuntimeRequestInterceptor(RuntimeTelemetrySdk sdk, TraceContextFactory traceContextFactory, RuntimeInstrumentationBridge bridge, RuntimeExceptionHandler exceptionHandler) {
        this.sdk = Objects.requireNonNull(sdk, "sdk must not be null");
        this.traceContextFactory = Objects.requireNonNull(traceContextFactory, "traceContextFactory must not be null");
        this.bridge = Objects.requireNonNull(bridge, "bridge must not be null");
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler, "exceptionHandler must not be null");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        TraceContext traceContext = traceContextFactory.createFromHeaders(extractHeaders(request));
        sdk.getContext().updateTraceContext(traceContext);
        TraceContextHolder.setContext(traceContext);
        request.setAttribute(TRACE_CONTEXT_ATTRIBUTE, traceContext);
        request.setAttribute(LATENCY_TRACKER_ATTRIBUTE, LatencyTracker.start());
        request.setAttribute(REQUEST_STARTED_EVENT_ATTRIBUTE, bridge.createRequestStarted(traceContext, request.getMethod(), request.getRequestURI(), request.getRemoteAddr()));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            TraceContext traceContext = resolveTraceContext(request);
            LatencyTracker tracker = (LatencyTracker) request.getAttribute(LATENCY_TRACKER_ATTRIBUTE);
            long latencyMillis = tracker == null ? 0L : tracker.stop();
            request.setAttribute(REQUEST_COMPLETED_EVENT_ATTRIBUTE, bridge.createRequestCompleted(traceContext, request.getMethod(), request.getRequestURI(), response.getStatus(), request.getRemoteAddr(), latencyMillis));
            if (ex != null) {
                request.setAttribute(ERROR_EVENT_ATTRIBUTE, exceptionHandler.record(ex, traceContext, handler == null ? "unknown" : handler.getClass().getName(), "afterCompletion", latencyMillis));
            }
        } finally {
            sdk.getContext().clearTraceContext();
            TraceContextHolder.clear();
        }
    }

    private static Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        String traceId = request.getHeader(TraceHeaders.TRACE_ID);
        String spanId = request.getHeader(TraceHeaders.SPAN_ID);
        String parentSpanId = request.getHeader(TraceHeaders.PARENT_SPAN_ID);
        if (traceId != null) {
            headers.put(TraceHeaders.TRACE_ID, traceId);
        }
        if (spanId != null) {
            headers.put(TraceHeaders.SPAN_ID, spanId);
        }
        if (parentSpanId != null) {
            headers.put(TraceHeaders.PARENT_SPAN_ID, parentSpanId);
        }
        return headers;
    }

    private static TraceContext resolveTraceContext(HttpServletRequest request) {
        Object traceContext = request.getAttribute(TRACE_CONTEXT_ATTRIBUTE);
        if (traceContext instanceof TraceContext context) {
            return context;
        }
        return TraceContextHolder.getContext().orElseThrow(() -> new IllegalStateException("trace context missing during afterCompletion"));
    }
}
