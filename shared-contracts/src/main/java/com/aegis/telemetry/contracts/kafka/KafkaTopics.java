package com.aegis.telemetry.contracts.kafka;

public final class KafkaTopics {

    public static final String RUNTIME_EVENTS = "runtime.events";
    public static final String RUNTIME_ERRORS = "runtime.errors";
    public static final String RUNTIME_RETRIES = "runtime.retries";
    public static final String RUNTIME_HEARTBEATS = "runtime.heartbeats";

    private KafkaTopics() {
    }
}
