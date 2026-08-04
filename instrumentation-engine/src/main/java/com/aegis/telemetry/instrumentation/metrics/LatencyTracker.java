package com.aegis.telemetry.instrumentation.metrics;

public final class LatencyTracker {

    private final long startNanos;
    private long stopNanos;

    private LatencyTracker() {
        this.startNanos = System.nanoTime();
    }

    public static LatencyTracker start() {
        return new LatencyTracker();
    }

    public long stop() {
        if (stopNanos == 0L) {
            stopNanos = System.nanoTime();
        }
        return elapsed();
    }

    public long elapsed() {
        long endNanos = stopNanos == 0L ? System.nanoTime() : stopNanos;
        return Math.max(0L, (endNanos - startNanos) / 1_000_000L);
    }
}
