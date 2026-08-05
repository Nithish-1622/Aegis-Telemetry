package com.aegis.telemetry.sdk.config;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeSdkConfigurationTest {

    @Test
    void buildsWithDefaultValues() {
        RuntimeSdkConfiguration configuration = RuntimeSdkConfiguration.builder()
                .applicationName("order-service")
                .instanceId("instance-1")
                .environment("dev")
                .build();

        assertThat(configuration.samplingRate()).isEqualTo(1.0d);
        assertThat(configuration.capturePayload()).isTrue();
        assertThat(configuration.captureThreadInfo()).isTrue();
        assertThat(configuration.heartbeatInterval()).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void validatesRequiredFields() {
        var validator = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();

        RuntimeSdkConfiguration configuration = RuntimeSdkConfiguration.builder()
                .applicationName(" ")
                .instanceId(" ")
                .environment(" ")
                .build();

        assertThat(validator.validate(configuration)).isNotEmpty();
    }

    @Test
    void builderRejectsMissingConfigurationWhenValidatedElsewhere() {
        RuntimeSdkConfiguration configuration = RuntimeSdkConfiguration.builder()
                .applicationName("order-service")
                .instanceId("instance-1")
                .environment("test")
                .samplingRate(0.5d)
                .capturePayload(false)
                .captureThreadInfo(false)
                .heartbeatInterval(Duration.ofSeconds(60))
                .build();

        assertThat(configuration.environment()).isEqualTo("test");
    }
}
