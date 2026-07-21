package com.aegis.telemetry.sdk.serialization;

import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class RuntimeEventSerializer {

    private final ObjectMapper objectMapper;

    public RuntimeEventSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
    }

    public String toJson(RuntimeEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize RuntimeEvent", exception);
        }
    }

    public RuntimeEvent fromJson(String json) {
        try {
            return objectMapper.readValue(json, RuntimeEvent.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize RuntimeEvent", exception);
        }
    }
}
