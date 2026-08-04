package com.aegis.telemetry.publisher.retry;

public record PublishResult<T>(T value, int attempts) {
}
