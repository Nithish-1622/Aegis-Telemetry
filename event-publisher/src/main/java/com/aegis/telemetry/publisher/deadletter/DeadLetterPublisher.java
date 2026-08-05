package com.aegis.telemetry.publisher.deadletter;

import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.publisher.config.KafkaPublisherProperties;
import com.aegis.telemetry.publisher.routing.PublisherTopics;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class DeadLetterPublisher {

    private static final Logger logger = LoggerFactory.getLogger(DeadLetterPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaPublisherProperties properties;

    public DeadLetterPublisher(KafkaTemplate<String, Object> kafkaTemplate, KafkaPublisherProperties properties) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "kafkaTemplate must not be null");
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
    }

    public CompletableFuture<Void> publish(RuntimeEvent originalEvent, Throwable failure, int retryCount) {
        DeadLetterEvent deadLetterEvent = new DeadLetterEvent(originalEvent, resolveReason(failure), Instant.now(), retryCount);
        logger.warn("Publishing runtime event to dead letter topic {} after {} retries", properties.getDeadLetterTopic(), retryCount, failure);
        return kafkaTemplate.send(properties.getDeadLetterTopic(), deadLetterEvent)
                .thenAccept(record -> {
                    if (record != null) {
                        RecordMetadata metadata = record.getRecordMetadata();
                        logger.info("Dead letter published to {} partition {} offset {}", metadata.topic(), metadata.partition(), metadata.offset());
                    }
                });
    }

    private static String resolveReason(Throwable failure) {
        if (failure == null) {
            return "unknown";
        }
        String message = failure.getMessage();
        return message == null || message.isBlank() ? failure.getClass().getName() : message;
    }
}
