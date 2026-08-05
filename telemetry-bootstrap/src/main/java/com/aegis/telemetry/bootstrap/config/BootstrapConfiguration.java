package com.aegis.telemetry.bootstrap.config;

import com.aegis.telemetry.config.autoconfigure.RuntimeConfigurationAutoConfiguration;
import com.aegis.telemetry.registry.autoconfigure.ServiceRegistryAutoConfiguration;
import com.aegis.telemetry.sdk.autoconfigure.RuntimeSdkAutoConfiguration;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
    @Import({RuntimeSdkAutoConfiguration.class, ServiceRegistryAutoConfiguration.class, RuntimeConfigurationAutoConfiguration.class})
    @EnableConfigurationProperties(BootstrapProperties.class)
public class BootstrapConfiguration {

	@Bean
    @ConditionalOnMissingBean(RuntimeSdkConfiguration.class)
	public RuntimeSdkConfiguration runtimeSdkConfiguration(BootstrapProperties properties) {
		return RuntimeSdkConfiguration.builder()
				.applicationName(properties.getServiceName())
				.instanceId(properties.getInstanceId())
				.environment(properties.getEnvironment())
				.samplingRate(properties.getSamplingRate())
				.capturePayload(properties.isCapturePayload())
				.captureThreadInfo(properties.isCaptureThreadInfo())
				.heartbeatInterval(properties.heartbeatInterval())
				.build();
	}
}
