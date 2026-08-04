package com.aegis.telemetry.publisher.health;

import com.aegis.telemetry.contracts.kafka.KafkaTopics;
import com.aegis.telemetry.publisher.metrics.PublisherMetrics;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class KafkaPublisherHealthIndicator implements HealthIndicator {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PublisherMetrics metrics;

    public KafkaPublisherHealthIndicator(KafkaTemplate<String, Object> kafkaTemplate, PublisherMetrics metrics) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "kafkaTemplate must not be null");
        this.metrics = Objects.requireNonNull(metrics, "metrics must not be null");
    }

    @Override
    public Health health() {
        try (Producer<String, Object> producer = kafkaTemplate.getProducerFactory().createProducer()) {
            producer.partitionsFor(KafkaTopics.RUNTIME_EVENTS);
            Status status = determineStatus();
            return Health.status(status).withDetails(details()).build();
        } catch (Exception exception) {
            return Health.down(exception).withDetails(details()).build();
        }
    }

    private Status determineStatus() {
        if (metrics.getLastFailedPublish() > metrics.getLastSuccessfulPublish()) {
            return new Status("DEGRADED");
        }
        return Status.UP;
    }

    private Map<String, Object> details() {
        Map<String, Object> details = new HashMap<>();
        details.put("eventsPublished", metrics.getEventsPublished());
        details.put("publishSuccess", metrics.getPublishSuccess());
        details.put("publishFailure", metrics.getPublishFailure());
        details.put("retryCount", metrics.getRetryCount());
        details.put("averagePublishTimeMillis", metrics.getAveragePublishTimeMillis());
        details.put("currentQueueSize", metrics.getCurrentQueueSize());
        details.put("lastSuccessfulPublish", metrics.getLastSuccessfulPublish());
        details.put("lastFailedPublish", metrics.getLastFailedPublish());
        return details;
    }
}
