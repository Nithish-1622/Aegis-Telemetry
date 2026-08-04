package com.aegis.telemetry.publisher.retry;

import org.apache.kafka.common.errors.RetriableException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.errors.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class PublishRetryPolicy {

    private static final Logger logger = LoggerFactory.getLogger(PublishRetryPolicy.class);

    private final int maxAttempts;
    private final long initialDelayMillis;

    public PublishRetryPolicy(int maxAttempts, long initialDelayMillis) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.initialDelayMillis = Math.max(0L, initialDelayMillis);
    }

    public PublishRetryPolicy(int maxAttempts, Duration initialDelay) {
        this(maxAttempts, initialDelay == null ? 0L : initialDelay.toMillis());
    }

    public <T> CompletableFuture<PublishResult<T>> executePublish(Supplier<CompletableFuture<T>> operation) {
        Objects.requireNonNull(operation, "operation must not be null");
        CompletableFuture<PublishResult<T>> result = new CompletableFuture<>();
        executeAttempt(operation, result, 1);
        return result;
    }

    private <T> void executeAttempt(Supplier<CompletableFuture<T>> operation, CompletableFuture<PublishResult<T>> result, int attempt) {
        CompletableFuture<T> future;
        try {
            future = operation.get();
        } catch (Throwable throwable) {
            handleFailure(operation, result, attempt, unwrap(throwable));
            return;
        }

        future.whenComplete((value, throwable) -> {
            if (throwable == null) {
                result.complete(new PublishResult<>(value, attempt));
                return;
            }
            handleFailure(operation, result, attempt, unwrap(throwable));
        });
    }

    private <T> void handleFailure(Supplier<CompletableFuture<T>> operation, CompletableFuture<PublishResult<T>> result, int attempt, Throwable failure) {
        if (!shouldRetry(failure) || attempt >= maxAttempts) {
            result.completeExceptionally(new PublishRetryException("publish failed after retry attempts", failure, attempt));
            return;
        }

        long delay = initialDelayMillis <= 0L ? 0L : initialDelayMillis * (1L << Math.max(0, attempt - 1));
        logger.debug("Retrying publish attempt {} after {} ms", attempt + 1, delay);
        CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS).execute(() -> executeAttempt(operation, result, attempt + 1));
    }

    private static Throwable unwrap(Throwable throwable) {
        if (throwable instanceof CompletionException completionException && completionException.getCause() != null) {
            return unwrap(completionException.getCause());
        }
        if (throwable.getCause() != null && throwable instanceof RuntimeException) {
            Throwable cause = throwable.getCause();
            if (cause != throwable) {
                return unwrap(cause);
            }
        }
        return throwable;
    }

    private static boolean shouldRetry(Throwable failure) {
        return failure instanceof RetriableException
                || failure instanceof TimeoutException
                || failure instanceof java.util.concurrent.TimeoutException;
    }

    private static boolean isSerializationFailure(Throwable failure) {
        return failure instanceof SerializationException
                || failure instanceof com.fasterxml.jackson.core.JsonProcessingException
                || failure.getCause() instanceof SerializationException
                || failure.getCause() instanceof com.fasterxml.jackson.core.JsonProcessingException;
    }

    public boolean isRetryable(Throwable failure) {
        return !isSerializationFailure(failure) && shouldRetry(failure);
    }
}
