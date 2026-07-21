package com.aegis.telemetry.sdk.autoconfigure;

import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.builder.RuntimeEventBuilder;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import com.aegis.telemetry.sdk.context.RuntimeSdkContext;
import com.aegis.telemetry.sdk.factory.RuntimeEventFactory;
import com.aegis.telemetry.sdk.serialization.RuntimeEventSerializer;
import com.aegis.telemetry.sdk.validation.RuntimeEventValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeSdkAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RuntimeSdkAutoConfiguration.class));

    @Test
    void registersExpectedBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RuntimeSdkConfiguration.class);
            assertThat(context).hasSingleBean(RuntimeSdkContext.class);
            assertThat(context).hasSingleBean(RuntimeEventValidator.class);
            assertThat(context).hasSingleBean(RuntimeEventFactory.class);
            assertThat(context).hasSingleBean(RuntimeEventSerializer.class);
            assertThat(context).hasSingleBean(RuntimeTelemetrySdk.class);
            assertThat(context).hasSingleBean(RuntimeEventBuilder.Builder.class);
        });
    }
}
