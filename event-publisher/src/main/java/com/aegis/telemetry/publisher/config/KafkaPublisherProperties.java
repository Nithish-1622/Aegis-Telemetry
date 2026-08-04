package com.aegis.telemetry.publisher.config;

import com.aegis.telemetry.publisher.routing.PublisherTopics;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "aegis.kafka")
public class KafkaPublisherProperties {

    @NotBlank
    private String bootstrapServers = "localhost:9092";

    private String clientId = "aegis-telemetry";

    @NotBlank
    private String acks = "all";

    @Min(1)
    private int retries = 3;

    @NotBlank
    private String compression = "lz4";

    @Min(0)
    private int lingerMs = 5;

    @Min(1)
    private int batchSize = 16_384;

    @Min(0)
    private long retryDelayMs = 250L;

    @Min(1)
    private int retryCount = 3;

    @NotBlank
    private String deadLetterTopic = PublisherTopics.RUNTIME_DEADLETTER;

    private boolean idempotence = true;

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAcks() {
        return acks;
    }

    public void setAcks(String acks) {
        this.acks = acks;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    public int getLingerMs() {
        return lingerMs;
    }

    public void setLingerMs(int lingerMs) {
        this.lingerMs = lingerMs;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public String getDeadLetterTopic() {
        return deadLetterTopic;
    }

    public void setDeadLetterTopic(String deadLetterTopic) {
        this.deadLetterTopic = deadLetterTopic;
    }

    public boolean isIdempotence() {
        return idempotence;
    }

    public void setIdempotence(boolean idempotence) {
        this.idempotence = idempotence;
    }
}
