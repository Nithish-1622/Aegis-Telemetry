package com.aegis.telemetry.instrumentation.aop;

import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.instrumentation.exception.RuntimeExceptionHandler;
import com.aegis.telemetry.instrumentation.integration.RuntimeInstrumentationBridge;
import com.aegis.telemetry.instrumentation.metrics.LatencyTracker;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.trace.context.TraceContext;
import com.aegis.telemetry.trace.context.TraceContextHolder;
import com.aegis.telemetry.trace.factory.TraceContextFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Aspect
@Component
public final class RuntimeMethodInterceptor {

    private final RuntimeTelemetrySdk sdk;
    private final TraceContextFactory traceContextFactory;
    private final RuntimeInstrumentationBridge bridge;
    private final RuntimeExceptionHandler exceptionHandler;

    public RuntimeMethodInterceptor(RuntimeTelemetrySdk sdk, TraceContextFactory traceContextFactory, RuntimeInstrumentationBridge bridge, RuntimeExceptionHandler exceptionHandler) {
        this.sdk = Objects.requireNonNull(sdk, "sdk must not be null");
        this.traceContextFactory = Objects.requireNonNull(traceContextFactory, "traceContextFactory must not be null");
        this.bridge = Objects.requireNonNull(bridge, "bridge must not be null");
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler, "exceptionHandler must not be null");
    }

    @Around("@within(org.springframework.stereotype.Service) || @within(org.springframework.stereotype.Repository)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        TraceContext parentTraceContext = sdk.getCurrentTraceContext().orElseGet(() -> TraceContextHolder.getContext().orElseGet(() -> traceContextFactory.createRootContext()));
        TraceContext childTraceContext = traceContextFactory.createChildContext(parentTraceContext);
        TraceContextHolder.setContext(childTraceContext);
        sdk.getContext().updateTraceContext(childTraceContext);

        LatencyTracker tracker = LatencyTracker.start();
        RuntimeEvent startedEvent = bridge.createServiceCallStarted(childTraceContext, signature.getDeclaringTypeName(), signature.getName(), joinPoint.getArgs());
        sdk.createEvent(startedEvent);
        Object result = null;
        try {
            result = joinPoint.proceed();
            RuntimeEvent completedEvent = bridge.createServiceCallCompleted(childTraceContext, signature.getDeclaringTypeName(), signature.getName(), result, tracker.stop());
            sdk.createEvent(completedEvent);
            return result;
        } catch (Throwable throwable) {
            exceptionHandler.record(throwable, childTraceContext, signature.getDeclaringTypeName(), signature.getName(), tracker.stop());
            throw throwable;
        } finally {
            sdk.getContext().updateTraceContext(parentTraceContext);
            TraceContextHolder.setContext(parentTraceContext);
        }
    }
}
