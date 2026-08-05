package com.aegis.telemetry.publisher.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(KafkaPublisherProperties.class)
public class KafkaProducerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper publisherObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaPublisherProperties properties, ObjectMapper publisherObjectMapper) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        configs.put(ProducerConfig.CLIENT_ID_CONFIG, properties.getClientId());
        configs.put(ProducerConfig.ACKS_CONFIG, properties.getAcks());
        configs.put(ProducerConfig.RETRIES_CONFIG, properties.getRetries());
        configs.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, properties.getCompression());
        configs.put(ProducerConfig.LINGER_MS_CONFIG, properties.getLingerMs());
        configs.put(ProducerConfig.BATCH_SIZE_CONFIG, properties.getBatchSize());
        configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, properties.isIdempotence());
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configs.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        JsonSerializer<Object> valueSerializer = new JsonSerializer<>(publisherObjectMapper);
        valueSerializer.setAddTypeInfo(false);
        return new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), valueSerializer);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
