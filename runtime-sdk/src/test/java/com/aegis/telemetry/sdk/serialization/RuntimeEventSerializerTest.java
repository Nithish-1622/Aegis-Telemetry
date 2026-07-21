package com.aegis.telemetry.sdk.serialization;

import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.enums.RuntimeStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeEventSerializerTest {

    private final RuntimeEventSerializer serializer = new RuntimeEventSerializer(new ObjectMapper());

    @Test
    void serializesAndDeserializesInstantAndPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("b", 2);
        payload.put("a", 1);

        RuntimeEvent event = RuntimeEvent.builder()
                .eventId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .traceId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .spanId(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                .serviceName("service")
                .eventType(RuntimeEventType.REQUEST_STARTED)
                .timestamp(Instant.parse("2026-07-21T00:00:00Z"))
                .threadId(1L)
                .latency(0L)
                .status(RuntimeStatus.UNKNOWN)
                .payload(payload)
                .build();

        String json = serializer.toJson(event);
        RuntimeEvent restored = serializer.fromJson(json);

        assertThat(restored).isEqualTo(event);
        assertThat(json).contains("2026-07-21T00:00:00Z");
    }

    @Test
    void ignoresUnknownFields() {
        String json = """
                {
                  "eventId":"11111111-1111-1111-1111-111111111111",
                  "traceId":"22222222-2222-2222-2222-222222222222",
                  "spanId":"33333333-3333-3333-3333-333333333333",
                  "serviceName":"service",
                  "eventType":"REQUEST_STARTED",
                  "timestamp":"2026-07-21T00:00:00Z",
                  "threadId":1,
                  "latency":0,
                  "status":"UNKNOWN",
                  "unknownField":"ignored"
                }
                """;

        RuntimeEvent event = serializer.fromJson(json);

        assertThat(event.serviceName()).isEqualTo("service");
        assertThat(event.payload()).isEmpty();
    }
}
