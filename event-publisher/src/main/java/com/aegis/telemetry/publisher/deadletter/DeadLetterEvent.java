package com.aegis.telemetry.publisher.deadletter;

import com.aegis.telemetry.contracts.event.RuntimeEvent;

import java.time.Instant;

public record DeadLetterEvent(RuntimeEvent originalEvent, String failureReason, Instant timestamp, int retryCount) {
}
