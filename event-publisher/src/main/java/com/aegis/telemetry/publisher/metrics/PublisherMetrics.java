package com.aegis.telemetry.publisher.metrics;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class PublisherMetrics {

    private final AtomicLong eventsPublished = new AtomicLong();
    private final AtomicLong publishSuccess = new AtomicLong();
    private final AtomicLong publishFailure = new AtomicLong();
    private final AtomicLong retryCount = new AtomicLong();
    private final AtomicLong totalPublishTimeNanos = new AtomicLong();
    private final AtomicInteger currentQueueSize = new AtomicInteger();
    private final AtomicLong lastSuccessfulPublish = new AtomicLong(0L);
    private final AtomicLong lastFailedPublish = new AtomicLong(0L);

    public void incrementQueueSize() {
        currentQueueSize.incrementAndGet();
    }

    public void decrementQueueSize() {
        currentQueueSize.updateAndGet(value -> Math.max(0, value - 1));
    }

    public void recordSuccess(long publishDurationNanos, int retriesUsed) {
        eventsPublished.incrementAndGet();
        publishSuccess.incrementAndGet();
        retryCount.addAndGet(Math.max(0, retriesUsed));
        totalPublishTimeNanos.addAndGet(Math.max(0L, publishDurationNanos));
        lastSuccessfulPublish.set(Instant.now().toEpochMilli());
    }

    public void recordFailure(long publishDurationNanos, int retriesUsed) {
        publishFailure.incrementAndGet();
        retryCount.addAndGet(Math.max(0, retriesUsed));
        totalPublishTimeNanos.addAndGet(Math.max(0L, publishDurationNanos));
        lastFailedPublish.set(Instant.now().toEpochMilli());
    }

    public long getEventsPublished() {
        return eventsPublished.get();
    }

    public long getPublishSuccess() {
        return publishSuccess.get();
    }

    public long getPublishFailure() {
        return publishFailure.get();
    }

    public long getRetryCount() {
        return retryCount.get();
    }

    public double getAveragePublishTimeMillis() {
        long totalAttempts = publishSuccess.get() + publishFailure.get();
        if (totalAttempts == 0L) {
            return 0.0d;
        }
        return totalPublishTimeNanos.get() / 1_000_000.0d / totalAttempts;
    }

    public int getCurrentQueueSize() {
        return currentQueueSize.get();
    }

    public long getLastSuccessfulPublish() {
        return lastSuccessfulPublish.get();
    }

    public long getLastFailedPublish() {
        return lastFailedPublish.get();
    }
}
