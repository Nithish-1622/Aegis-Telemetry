package com.aegis.telemetry.instrumentation.autoconfigure;

import com.aegis.telemetry.instrumentation.aop.RuntimeMethodInterceptor;
import com.aegis.telemetry.instrumentation.exception.RuntimeExceptionHandler;
import com.aegis.telemetry.instrumentation.integration.RuntimeInstrumentationBridge;
import com.aegis.telemetry.instrumentation.metadata.RuntimeMetadataProvider;
import com.aegis.telemetry.instrumentation.thread.ThreadMetadataProvider;
import com.aegis.telemetry.instrumentation.web.RuntimeRequestInterceptor;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.autoconfigure.RuntimeSdkAutoConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeInstrumentationAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RuntimeSdkAutoConfiguration.class, RuntimeInstrumentationAutoConfiguration.class));

    @AfterEach
    void tearDown() {
        RuntimeTelemetrySdk.getInstance().shutdown();
    }

    @Test
    void registersInstrumentationBeansWhenSdkIsPresent() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RuntimeMetadataProvider.class);
            assertThat(context).hasSingleBean(ThreadMetadataProvider.class);
            assertThat(context).hasSingleBean(RuntimeInstrumentationBridge.class);
            assertThat(context).hasSingleBean(RuntimeExceptionHandler.class);
            assertThat(context).hasSingleBean(RuntimeRequestInterceptor.class);
            assertThat(context).hasSingleBean(RuntimeMethodInterceptor.class);
        });
    }
}
