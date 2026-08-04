package com.aegis.telemetry.instrumentation.metadata;

import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class RuntimeMetadataProvider {

    private final RuntimeMetadata runtimeMetadata;

    public RuntimeMetadataProvider(RuntimeTelemetrySdk sdk) {
        RuntimeSdkConfiguration configuration = Objects.requireNonNull(sdk, "sdk must not be null").getConfiguration();
        this.runtimeMetadata = new RuntimeMetadata(
                configuration.applicationName(),
                configuration.environment(),
                configuration.applicationName(),
                configuration.instanceId(),
                resolveHostName(),
                System.getProperty("java.version", "unknown"),
                System.getProperty("os.name", "unknown")
        );
    }

    public RuntimeMetadata getMetadata() {
        return runtimeMetadata;
    }

    public record RuntimeMetadata(
            String applicationName,
            String environment,
            String serviceName,
            String instanceId,
            String hostName,
            String jvmVersion,
            String operatingSystem
    ) {
        public Map<String, Object> asMap() {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("applicationName", applicationName);
            metadata.put("environment", environment);
            metadata.put("serviceName", serviceName);
            metadata.put("instanceId", instanceId);
            metadata.put("hostName", hostName);
            metadata.put("jvmVersion", jvmVersion);
            metadata.put("operatingSystem", operatingSystem);
            return metadata;
        }
    }

    private static String resolveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException exception) {
            return "unknown-host";
        }
    }
}
