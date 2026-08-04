package com.aegis.telemetry.publisher.core;

import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.enums.RuntimeStatus;
import com.aegis.telemetry.publisher.deadletter.DeadLetterPublisher;
import com.aegis.telemetry.publisher.metrics.PublisherMetrics;
import com.aegis.telemetry.publisher.retry.PublishRetryPolicy;
import com.aegis.telemetry.publisher.routing.RuntimeTopicRouter;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RuntimeEventPublisherTest {

    @Test
    void publishesEventAndRecordsMetrics() {
        KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
        RuntimeTopicRouter topicRouter = mock(RuntimeTopicRouter.class);
        DeadLetterPublisher deadLetterPublisher = mock(DeadLetterPublisher.class);
        PublisherMetrics metrics = new PublisherMetrics();
        RuntimeEventPublisher publisher = new RuntimeEventPublisher(kafkaTemplate, topicRouter, new PublishRetryPolicy(1, 0L), deadLetterPublisher, metrics);
        RuntimeEvent event = sampleEvent(RuntimeEventType.REQUEST_STARTED);
        when(topicRouter.route(event)).thenReturn("runtime.events");
        when(kafkaTemplate.send(anyString(), any())).thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        publisher.publish(event).join();

        assertThat(metrics.getEventsPublished()).isEqualTo(1);
        assertThat(metrics.getPublishSuccess()).isEqualTo(1);
        assertThat(metrics.getPublishFailure()).isZero();
        verify(deadLetterPublisher, never()).publish(any(), any(), anyInt());
    }

    @Test
    void publishesToDeadLetterWhenRetriesAreExhausted() {
        KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
        RuntimeTopicRouter topicRouter = mock(RuntimeTopicRouter.class);
        DeadLetterPublisher deadLetterPublisher = mock(DeadLetterPublisher.class);
        PublisherMetrics metrics = new PublisherMetrics();
        RuntimeEventPublisher publisher = new RuntimeEventPublisher(kafkaTemplate, topicRouter, new PublishRetryPolicy(1, 0L), deadLetterPublisher, metrics);
        RuntimeEvent event = sampleEvent(RuntimeEventType.ERROR_OCCURRED);
        when(topicRouter.route(event)).thenReturn("runtime.errors");
        CompletableFuture<SendResult<String, Object>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new org.apache.kafka.common.errors.TimeoutException("timeout"));
        when(kafkaTemplate.send(anyString(), any())).thenReturn(failed);
        when(deadLetterPublisher.publish(any(), any(), anyInt())).thenReturn(CompletableFuture.completedFuture(null));

        publisher.publish(event).handle((ignored, throwable) -> null).join();

        assertThat(metrics.getPublishFailure()).isEqualTo(1);
        verify(deadLetterPublisher).publish(any(), any(), anyInt());
    }

    private static RuntimeEvent sampleEvent(RuntimeEventType eventType) {
        return RuntimeEvent.builder()
                .eventId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .traceId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .spanId(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                .parentSpanId(UUID.fromString("44444444-4444-4444-4444-444444444444"))
                .serviceName("order-service")
                .instanceId("instance-1")
                .eventType(eventType)
                .timestamp(Instant.parse("2026-07-21T00:00:00Z"))
                .threadName("main")
                .threadId(1L)
                .latency(5L)
                .status(RuntimeStatus.SUCCESS)
                .payload(Map.of("key", "value"))
                .build();
    }
}
