package com.aegis.telemetry.registry.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "serviceId",
        "serviceName",
        "instanceId",
        "version",
        "host",
        "port",
        "environment",
        "status",
        "registeredAt",
        "lastHeartbeat"
})
public record ServiceInstance(
        @NotBlank String serviceId,
        @NotBlank String serviceName,
        @NotBlank String instanceId,
        @NotBlank String version,
        @NotBlank String host,
        @Min(1) int port,
        @NotBlank String environment,
        @NotNull ServiceStatus status,
        @NotNull Instant registeredAt,
        Instant lastHeartbeat
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public ServiceInstance {
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(registeredAt, "registeredAt must not be null");
        if (serviceId == null || serviceId.isBlank()) {
            serviceId = "svc-" + UUID.randomUUID();
        }
        if (instanceId == null || instanceId.isBlank()) {
            instanceId = serviceId;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .serviceId(serviceId)
                .serviceName(serviceName)
                .instanceId(instanceId)
                .version(version)
                .host(host)
                .port(port)
                .environment(environment)
                .status(status)
                .registeredAt(registeredAt)
                .lastHeartbeat(lastHeartbeat);
    }

    public static final class Builder {

        private String serviceId;
        private String serviceName;
        private String instanceId;
        private String version;
        private String host;
        private int port;
        private String environment;
        private ServiceStatus status = ServiceStatus.REGISTERED;
        private Instant registeredAt = Instant.now();
        private Instant lastHeartbeat;

        private Builder() {
        }

        public Builder serviceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder status(ServiceStatus status) {
            this.status = status;
            return this;
        }

        public Builder registeredAt(Instant registeredAt) {
            this.registeredAt = registeredAt;
            return this;
        }

        public Builder lastHeartbeat(Instant lastHeartbeat) {
            this.lastHeartbeat = lastHeartbeat;
            return this;
        }

        public ServiceInstance build() {
            return new ServiceInstance(serviceId, serviceName, instanceId, version, host, port, environment, status, registeredAt, lastHeartbeat);
        }
    }
}
