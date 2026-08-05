package com.aegis.telemetry.publisher.autoconfigure;

import com.aegis.telemetry.publisher.config.KafkaProducerConfiguration;
import com.aegis.telemetry.publisher.config.KafkaPublisherProperties;
import com.aegis.telemetry.publisher.core.RuntimeEventPublisher;
import com.aegis.telemetry.publisher.deadletter.DeadLetterPublisher;
import com.aegis.telemetry.publisher.health.KafkaPublisherHealthIndicator;
import com.aegis.telemetry.publisher.integration.RuntimePublisherBridge;
import com.aegis.telemetry.publisher.metrics.PublisherMetrics;
import com.aegis.telemetry.publisher.retry.PublishRetryPolicy;
import com.aegis.telemetry.publisher.routing.RuntimeTopicRouter;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;

@AutoConfiguration(afterName = "com.aegis.telemetry.sdk.autoconfigure.RuntimeSdkAutoConfiguration")
@ConditionalOnClass({KafkaTemplate.class, RuntimeTelemetrySdk.class})
@Import(KafkaProducerConfiguration.class)
public class RuntimePublisherAutoConfiguration {

    @Bean
    public RuntimeTopicRouter runtimeTopicRouter() {
        return new RuntimeTopicRouter();
    }

    @Bean
    public PublisherMetrics publisherMetrics() {
        return new PublisherMetrics();
    }

    @Bean
    public PublishRetryPolicy publishRetryPolicy(KafkaPublisherProperties properties) {
        return new PublishRetryPolicy(properties.getRetryCount(), Duration.ofMillis(properties.getRetryDelayMs()));
    }

    @Bean
    public DeadLetterPublisher deadLetterPublisher(KafkaTemplate<String, Object> kafkaTemplate, KafkaPublisherProperties properties) {
        return new DeadLetterPublisher(kafkaTemplate, properties);
    }

    @Bean
    public RuntimeEventPublisher runtimeEventPublisher(KafkaTemplate<String, Object> kafkaTemplate, RuntimeTopicRouter topicRouter, PublishRetryPolicy publishRetryPolicy, DeadLetterPublisher deadLetterPublisher, PublisherMetrics metrics) {
        return new RuntimeEventPublisher(kafkaTemplate, topicRouter, publishRetryPolicy, deadLetterPublisher, metrics);
    }

    @Bean
    public RuntimePublisherBridge runtimePublisherBridge(RuntimeEventPublisher runtimeEventPublisher) {
        return new RuntimePublisherBridge(runtimeEventPublisher);
    }

    @Bean
    public HealthContributor kafkaPublisherHealthIndicator(KafkaTemplate<String, Object> kafkaTemplate, PublisherMetrics metrics) {
        return new KafkaPublisherHealthIndicator(kafkaTemplate, metrics);
    }
}
