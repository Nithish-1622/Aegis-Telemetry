package com.aegis.telemetry.contracts.event;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.enums.RuntimeStatus;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class RuntimeEventTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        validatorFactory = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void afterAll() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @Test
    void createsImmutableObject() {
        UUID eventId = UUID.randomUUID();
        UUID traceId = UUID.randomUUID();
        UUID spanId = UUID.randomUUID();
        Instant timestamp = Instant.parse("2026-07-21T00:00:00Z");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", "ORD-1");

        RuntimeEvent event = RuntimeEvent.builder()
                .eventId(eventId)
                .traceId(traceId)
                .spanId(spanId)
                .serviceName("order-service")
                .eventType(RuntimeEventType.REQUEST_STARTED)
                .timestamp(timestamp)
                .threadId(42L)
                .latency(0L)
                .status(RuntimeStatus.UNKNOWN)
                .payload(payload)
                .build();

        assertThat(event.eventId()).isEqualTo(eventId);
        assertThat(event.traceId()).isEqualTo(traceId);
        assertThat(event.spanId()).isEqualTo(spanId);
        assertThat(event.timestamp()).isEqualTo(timestamp);
        assertThat(event.payload()).containsEntry("orderId", "ORD-1");
        assertThat(event.payload()).isNotSameAs(payload);
        assertThatThrownBy(() -> event.payload().put("newKey", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void builderCreatesEqualInstances() {
        RuntimeEvent first = sampleEventBuilder().build();
        RuntimeEvent second = sampleEventBuilder().build();

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void serializesAndDeserializes() throws Exception {
        ObjectMapper objectMapper = objectMapper();
        RuntimeEvent event = sampleEventBuilder().build();

        String json = objectMapper.writeValueAsString(event);
        RuntimeEvent restored = objectMapper.readValue(json, RuntimeEvent.class);

        assertThat(restored).isEqualTo(event);
    }

    @Test
    void ignoresUnknownFields() throws Exception {
        ObjectMapper objectMapper = objectMapper();
        String json = """
                {
                  "eventId":"11111111-1111-1111-1111-111111111111",
                  "traceId":"22222222-2222-2222-2222-222222222222",
                  "spanId":"33333333-3333-3333-3333-333333333333",
                  "serviceName":"order-service",
                  "eventType":"REQUEST_STARTED",
                  "timestamp":"2026-07-21T00:00:00Z",
                  "threadId":1,
                  "latency":0,
                  "status":"UNKNOWN",
                  "unknownField":"ignored"
                }
                """;

        RuntimeEvent event = objectMapper.readValue(json, RuntimeEvent.class);

        assertThat(event.eventId()).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(event.payload()).isEmpty();
    }

    @Test
    void validatesRequiredFieldsAndBounds() {
        RuntimeEvent invalid = new RuntimeEvent(
                null,
                null,
                null,
                null,
                " ",
                null,
                null,
                null,
                null,
                0L,
                -1L,
                null,
                Map.of()
        );

        Set<ConstraintViolation<RuntimeEvent>> violations = validator.validate(invalid);

        assertThat(violations).extracting(ConstraintViolation::getPropertyPath).map(Object::toString)
                .contains("eventId", "traceId", "spanId", "serviceName", "eventType", "timestamp", "status", "threadId", "latency");
    }

    @Test
    void deterministicJsonOrderingIncludesPayload() throws Exception {
        ObjectMapper objectMapper = objectMapper();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("b", 2);
        payload.put("a", 1);

        RuntimeEvent event = RuntimeEvent.builder()
                .eventId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .traceId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .spanId(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                .serviceName("order-service")
                .eventType(RuntimeEventType.REQUEST_STARTED)
                .timestamp(Instant.parse("2026-07-21T00:00:00Z"))
                .threadId(1L)
                .latency(0L)
                .status(RuntimeStatus.UNKNOWN)
                .payload(payload)
                .build();

        JsonNode jsonNode = objectMapper.readTree(objectMapper.writeValueAsString(event));

        assertThat(jsonNode.fieldNames()).toIterable().containsExactly(
                "eventId",
                "traceId",
                "spanId",
                "serviceName",
                "eventType",
                "timestamp",
                "threadId",
                "latency",
                "status",
                "payload"
        );
        assertThat(jsonNode.get("payload").fieldNames()).toIterable().containsExactly("b", "a");
    }

    private static RuntimeEvent.Builder sampleEventBuilder() {
        return RuntimeEvent.builder()
                .eventId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .traceId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .spanId(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                .parentSpanId(UUID.fromString("44444444-4444-4444-4444-444444444444"))
                .serviceName("order-service")
                .instanceId("instance-1")
                .eventType(RuntimeEventType.REQUEST_STARTED)
                .timestamp(Instant.parse("2026-07-21T00:00:00Z"))
                .threadName("main")
                .threadId(1L)
                .latency(25L)
                .status(RuntimeStatus.SUCCESS)
                .payload(Map.of("orderId", "ORD-1"));
    }

    private static ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
