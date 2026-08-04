package com.aegis.telemetry.publisher.retry;

import org.apache.kafka.common.errors.TimeoutException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PublishRetryPolicyTest {

    @Test
    void retriesRetriableFailuresUntilSuccess() {
        PublishRetryPolicy policy = new PublishRetryPolicy(3, 0L);
        AtomicInteger attempts = new AtomicInteger();

        CompletableFuture<PublishResult<String>> result = policy.executePublish(() -> {
            if (attempts.incrementAndGet() == 1) {
                CompletableFuture<String> failed = new CompletableFuture<>();
                failed.completeExceptionally(new TimeoutException("temporary"));
                return failed;
            }
            return CompletableFuture.completedFuture("ok");
        });

        PublishResult<String> publishResult = result.join();
        assertThat(publishResult.value()).isEqualTo("ok");
        assertThat(publishResult.attempts()).isEqualTo(2);
        assertThat(attempts.get()).isEqualTo(2);
    }

    @Test
    void failsFastForSerializationErrors() {
        PublishRetryPolicy policy = new PublishRetryPolicy(3, 0L);
        AtomicInteger attempts = new AtomicInteger();

        CompletableFuture<PublishResult<String>> result = policy.executePublish(() -> {
            attempts.incrementAndGet();
            CompletableFuture<String> failed = new CompletableFuture<>();
            failed.completeExceptionally(new com.fasterxml.jackson.core.JsonProcessingException("bad json") {
            });
            return failed;
        });

        assertThatThrownBy(result::join)
                .hasCauseInstanceOf(PublishRetryException.class);
        assertThat(attempts.get()).isEqualTo(1);
    }
}
