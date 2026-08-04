package com.aegis.telemetry.publisher.routing;

import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.kafka.KafkaTopics;

public final class RuntimeTopicRouter {

    public String route(RuntimeEvent event) {
        if (event == null || event.eventType() == null) {
            throw new IllegalArgumentException("runtime event type must not be null");
        }
        return route(event.eventType());
    }

    public String route(RuntimeEventType eventType) {
        if (eventType == null) {
            throw new IllegalArgumentException("runtime event type must not be null");
        }

        return switch (eventType) {
            case REQUEST_STARTED, REQUEST_COMPLETED, SERVICE_CALL_STARTED, SERVICE_CALL_COMPLETED -> KafkaTopics.RUNTIME_EVENTS;
            case ERROR_OCCURRED -> KafkaTopics.RUNTIME_ERRORS;
            case RETRY_TRIGGERED -> KafkaTopics.RUNTIME_RETRIES;
            case HEARTBEAT -> KafkaTopics.RUNTIME_HEARTBEATS;
        };
    }
}
