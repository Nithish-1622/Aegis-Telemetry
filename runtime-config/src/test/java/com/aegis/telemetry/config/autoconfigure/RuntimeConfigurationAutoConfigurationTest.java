package com.aegis.telemetry.config.autoconfigure;

import com.aegis.telemetry.config.controller.RuntimeConfigurationController;
import com.aegis.telemetry.config.runtime.RuntimeConfigurationService;
import com.aegis.telemetry.sdk.autoconfigure.RuntimeSdkAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeConfigurationAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RuntimeSdkAutoConfiguration.class, RuntimeConfigurationAutoConfiguration.class));

    @Test
    void registersRuntimeConfigurationBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RuntimeConfigurationService.class);
            assertThat(context).hasSingleBean(RuntimeConfigurationController.class);
        });
    }
}
