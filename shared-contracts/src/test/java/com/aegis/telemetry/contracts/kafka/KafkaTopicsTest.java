package com.aegis.telemetry.contracts.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class KafkaTopicsTest {

    @Test
    void topicNamesRemainUnchanged() {
        assertThat(KafkaTopics.RUNTIME_EVENTS).isEqualTo("runtime.events");
        assertThat(KafkaTopics.RUNTIME_ERRORS).isEqualTo("runtime.errors");
        assertThat(KafkaTopics.RUNTIME_RETRIES).isEqualTo("runtime.retries");
        assertThat(KafkaTopics.RUNTIME_HEARTBEATS).isEqualTo("runtime.heartbeats");
    }
}
