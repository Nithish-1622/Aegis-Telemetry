package com.aegis.telemetry.publisher.retry;

public final class PublishRetryException extends RuntimeException {

    private final int attempts;

    public PublishRetryException(String message, Throwable cause, int attempts) {
        super(message, cause);
        this.attempts = attempts;
    }

    public int getAttempts() {
        return attempts;
    }

    public int getRetryCount() {
        return Math.max(0, attempts - 1);
    }
}
