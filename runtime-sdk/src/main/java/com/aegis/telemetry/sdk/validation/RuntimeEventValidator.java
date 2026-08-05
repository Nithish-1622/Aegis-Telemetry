package com.aegis.telemetry.sdk.validation;

import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.contracts.enums.RuntimeStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class RuntimeEventValidator {

    private final Validator validator;

    public RuntimeEventValidator() {
        this(Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator());
    }

    public RuntimeEventValidator(Validator validator) {
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
    }

    public void validate(RuntimeEvent event) {
        Objects.requireNonNull(event, "event must not be null");

        Set<ConstraintViolation<RuntimeEvent>> violations = validator.validate(event);
        StringBuilder message = new StringBuilder();

        if (!violations.isEmpty()) {
            message.append(violations.stream()
                    .map(ConstraintViolation::getPropertyPath)
                    .map(Object::toString)
                    .sorted()
                    .collect(Collectors.joining(", ")));
        }

        appendIfMissing(message, event.traceId() == null || isBlank(event.traceId().toString()), "traceId must be present");
        appendIfMissing(message, event.spanId() == null || isBlank(event.spanId().toString()), "spanId must be present");
        appendIfMissing(message, isBlank(event.serviceName()), "serviceName must not be blank");
        appendIfMissing(message, event.eventType() == null, "eventType must be present");
        appendIfMissing(message, event.status() == null, "status must be present");
        appendIfMissing(message, event.timestamp() == null, "timestamp must be present");
        appendIfMissing(message, event.timestamp() != null && event.timestamp().isAfter(Instant.now()), "timestamp must not be in the future");
        appendIfMissing(message, event.latency() < 0, "latency must not be negative");
        appendIfMissing(message, event.threadId() <= 0, "threadId must be positive");
        appendIfMissing(message, event.traceId() != null && !isUuid(event.traceId().toString()), "traceId must be UUID formatted");
        appendIfMissing(message, event.spanId() != null && !isUuid(event.spanId().toString()), "spanId must be UUID formatted");
        appendIfMissing(message, event.parentSpanId() != null && !isUuid(event.parentSpanId().toString()), "parentSpanId must be UUID formatted");
        appendIfMissing(message, event.parentSpanId() != null && event.parentSpanId().equals(event.spanId()), "parentSpanId must differ from spanId");
        appendIfMissing(message, event.status() != null && !isKnownStatus(event.status()), "status must be a supported RuntimeStatus");

        if (message.length() > 0) {
            throw new RuntimeEventValidationException(message.toString());
        }
    }

    private static void appendIfMissing(StringBuilder builder, boolean condition, String message) {
        if (!condition) {
            return;
        }
        if (builder.length() > 0) {
            builder.append("; ");
        }
        builder.append(message);
    }

    private static boolean isUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean isKnownStatus(RuntimeStatus status) {
        for (RuntimeStatus runtimeStatus : RuntimeStatus.values()) {
            if (runtimeStatus == status) {
                return true;
            }
        }
        return false;
    }
}
