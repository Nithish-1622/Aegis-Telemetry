package com.aegis.telemetry.config.autoconfigure;

import com.aegis.telemetry.config.controller.RuntimeConfigurationController;
import com.aegis.telemetry.config.runtime.RuntimeConfigurationService;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(afterName = "com.aegis.telemetry.sdk.autoconfigure.RuntimeSdkAutoConfiguration")
@ConditionalOnClass(RuntimeTelemetrySdk.class)
public class RuntimeConfigurationAutoConfiguration {

    @Bean
    public RuntimeConfigurationService runtimeConfigurationService(RuntimeTelemetrySdk sdk) {
        return new RuntimeConfigurationService(sdk);
    }

    @Bean
    public RuntimeConfigurationController runtimeConfigurationController(RuntimeConfigurationService runtimeConfigurationService) {
        return new RuntimeConfigurationController(runtimeConfigurationService);
    }
}
