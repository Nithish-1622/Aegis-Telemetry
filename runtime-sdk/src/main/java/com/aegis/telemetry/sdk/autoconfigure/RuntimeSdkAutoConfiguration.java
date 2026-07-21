package com.aegis.telemetry.sdk.autoconfigure;

import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.builder.RuntimeEventBuilder;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import com.aegis.telemetry.sdk.context.RuntimeSdkContext;
import com.aegis.telemetry.sdk.factory.RuntimeEventFactory;
import com.aegis.telemetry.sdk.serialization.RuntimeEventSerializer;
import com.aegis.telemetry.sdk.validation.RuntimeEventValidator;
import com.aegis.telemetry.trace.factory.TraceContextFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class RuntimeSdkAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper runtimeObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RuntimeSdkConfiguration runtimeSdkConfiguration() {
        return RuntimeSdkConfiguration.builder()
                .applicationName(System.getProperty("aegis.telemetry.application-name", "aegis-telemetry"))
                .instanceId(System.getProperty("aegis.telemetry.instance-id", "default-instance"))
                .environment(System.getProperty("aegis.telemetry.environment", "default"))
                .build();
    }

    @Bean
    public RuntimeSdkContext runtimeSdkContext() {
        return new RuntimeSdkContext();
    }

    @Bean
    public RuntimeEventValidator runtimeEventValidator() {
        return new RuntimeEventValidator();
    }

    @Bean
    public TraceContextFactory traceContextFactory(RuntimeSdkConfiguration configuration) {
        return new TraceContextFactory(configuration.applicationName());
    }

    @Bean
    public RuntimeEventFactory runtimeEventFactory(RuntimeSdkContext runtimeSdkContext, TraceContextFactory traceContextFactory) {
        return new RuntimeEventFactory(runtimeSdkContext, traceContextFactory);
    }

    @Bean
    public RuntimeEventSerializer runtimeEventSerializer(ObjectMapper objectMapper) {
        return new RuntimeEventSerializer(objectMapper);
    }

    @Bean
    public RuntimeTelemetrySdk runtimeTelemetrySdk(RuntimeSdkConfiguration configuration) {
        RuntimeTelemetrySdk sdk = RuntimeTelemetrySdk.getInstance();
        sdk.initialize(configuration);
        return sdk;
    }

    @Bean
    public RuntimeEventBuilder.Builder runtimeEventBuilder() {
        return RuntimeEventBuilder.builder();
    }
}
