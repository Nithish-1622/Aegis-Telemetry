package com.aegis.telemetry.publisher.core;

import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.publisher.deadletter.DeadLetterPublisher;
import com.aegis.telemetry.publisher.metrics.PublisherMetrics;
import com.aegis.telemetry.publisher.retry.PublishResult;
import com.aegis.telemetry.publisher.retry.PublishRetryException;
import com.aegis.telemetry.publisher.retry.PublishRetryPolicy;
import com.aegis.telemetry.publisher.routing.RuntimeTopicRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class RuntimeEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(RuntimeEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RuntimeTopicRouter topicRouter;
    private final PublishRetryPolicy retryPolicy;
    private final DeadLetterPublisher deadLetterPublisher;
    private final PublisherMetrics metrics;

    public RuntimeEventPublisher(KafkaTemplate<String, Object> kafkaTemplate, RuntimeTopicRouter topicRouter, PublishRetryPolicy retryPolicy, DeadLetterPublisher deadLetterPublisher, PublisherMetrics metrics) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "kafkaTemplate must not be null");
        this.topicRouter = Objects.requireNonNull(topicRouter, "topicRouter must not be null");
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy must not be null");
        this.deadLetterPublisher = Objects.requireNonNull(deadLetterPublisher, "deadLetterPublisher must not be null");
        this.metrics = Objects.requireNonNull(metrics, "metrics must not be null");
    }

    public CompletableFuture<Void> publish(RuntimeEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        String topic = topicRouter.route(event);
        long startedAt = System.nanoTime();
        metrics.incrementQueueSize();

        CompletableFuture<Void> publishFuture = retryPolicy.executePublish(() -> kafkaTemplate.send(topic, event))
                .handle((ignored, throwable) -> {
                    if (throwable != null) {
                        handleFailure(event, startedAt, throwable);
                        throw toCompletionException(throwable);
                    }
                    handleSuccess((PublishResult<?>) ignored, startedAt);
                    return null;
                });

        return publishFuture
                .whenComplete((ignored, ignoredThrowable) -> metrics.decrementQueueSize());
    }

    public CompletableFuture<Void> publishBatch(List<RuntimeEvent> events) {
        if (events == null || events.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<?>[] futures = events.stream().map(this::publish).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    private void handleSuccess(PublishResult<?> publishResult, long startedAt) {
        metrics.recordSuccess(System.nanoTime() - startedAt, Math.max(0, publishResult.attempts() - 1));
        logger.info("Published runtime event in {} attempt(s)", publishResult.attempts());
    }

    private void handleFailure(RuntimeEvent event, long startedAt, Throwable throwable) {
        Throwable failure = unwrap(throwable);
        int retryCount = failure instanceof PublishRetryException publishRetryException ? publishRetryException.getRetryCount() : 0;
        metrics.recordFailure(System.nanoTime() - startedAt, retryCount);
        logger.warn("Failed to publish runtime event {} after {} retries", event.eventType(), retryCount, failure);
        deadLetterPublisher.publish(event, failure, retryCount)
                .exceptionally(deadLetterFailure -> {
                    logger.error("Dead letter publish failed for event {}", event.eventId(), deadLetterFailure);
                    return null;
                });
    }

    private static RuntimeException toCompletionException(Throwable throwable) {
        if (throwable instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        return new RuntimeException(throwable);
    }

    private static Throwable unwrap(Throwable throwable) {
        if (throwable instanceof java.util.concurrent.CompletionException completionException && completionException.getCause() != null) {
            return unwrap(completionException.getCause());
        }
        if (throwable.getCause() != null && throwable instanceof RuntimeException) {
            return unwrap(throwable.getCause());
        }
        return throwable;
    }
}
