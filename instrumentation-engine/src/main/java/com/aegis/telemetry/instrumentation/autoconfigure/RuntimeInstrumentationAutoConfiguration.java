package com.aegis.telemetry.instrumentation.autoconfigure;

import com.aegis.telemetry.instrumentation.aop.RuntimeMethodInterceptor;
import com.aegis.telemetry.instrumentation.exception.RuntimeExceptionHandler;
import com.aegis.telemetry.instrumentation.integration.RuntimeInstrumentationBridge;
import com.aegis.telemetry.instrumentation.metadata.RuntimeMetadataProvider;
import com.aegis.telemetry.instrumentation.thread.ThreadMetadataProvider;
import com.aegis.telemetry.instrumentation.web.RuntimeRequestInterceptor;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.integration.RuntimeEventSink;
import com.aegis.telemetry.trace.factory.TraceContextFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(afterName = "com.aegis.telemetry.sdk.autoconfigure.RuntimeSdkAutoConfiguration")
@ConditionalOnClass(RuntimeTelemetrySdk.class)
public class RuntimeInstrumentationAutoConfiguration {

    @Bean
    @ConditionalOnBean(RuntimeTelemetrySdk.class)
    public RuntimeMetadataProvider runtimeMetadataProvider(RuntimeTelemetrySdk sdk) {
        return new RuntimeMetadataProvider(sdk);
    }

    @Bean
    public ThreadMetadataProvider threadMetadataProvider() {
        return new ThreadMetadataProvider();
    }

    @Bean
    @ConditionalOnBean(RuntimeTelemetrySdk.class)
    public RuntimeInstrumentationBridge runtimeInstrumentationBridge(RuntimeTelemetrySdk sdk, RuntimeMetadataProvider runtimeMetadataProvider, ThreadMetadataProvider threadMetadataProvider) {
        return new RuntimeInstrumentationBridge(sdk, runtimeMetadataProvider, threadMetadataProvider);
    }

    @Bean
    @ConditionalOnBean(RuntimeTelemetrySdk.class)
    public RuntimeExceptionHandler runtimeExceptionHandler(RuntimeInstrumentationBridge bridge) {
        return new RuntimeExceptionHandler(bridge, Boolean.parseBoolean(System.getProperty("aegis.telemetry.instrumentation.capture-stack-trace", "true")));
    }

    @Bean
    @ConditionalOnBean(RuntimeTelemetrySdk.class)
    public RuntimeRequestInterceptor runtimeRequestInterceptor(RuntimeTelemetrySdk sdk, TraceContextFactory traceContextFactory, RuntimeInstrumentationBridge bridge, RuntimeExceptionHandler exceptionHandler, ObjectProvider<RuntimeEventSink> eventSinkProvider) {
        return new RuntimeRequestInterceptor(sdk, traceContextFactory, bridge, exceptionHandler, eventSinkProvider.getIfAvailable());
    }

    @Bean
    @ConditionalOnBean(RuntimeTelemetrySdk.class)
    public RuntimeMethodInterceptor runtimeMethodInterceptor(RuntimeTelemetrySdk sdk, TraceContextFactory traceContextFactory, RuntimeInstrumentationBridge bridge, RuntimeExceptionHandler exceptionHandler, ObjectProvider<RuntimeEventSink> eventSinkProvider) {
        return new RuntimeMethodInterceptor(sdk, traceContextFactory, bridge, exceptionHandler, eventSinkProvider.getIfAvailable());
    }
}
