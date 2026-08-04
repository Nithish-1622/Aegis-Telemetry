package com.aegis.telemetry.instrumentation.metrics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LatencyTrackerTest {

    @Test
    void measuresElapsedTime() throws Exception {
        LatencyTracker tracker = LatencyTracker.start();
        Thread.sleep(25L);
        long elapsed = tracker.stop();

        assertThat(elapsed).isGreaterThanOrEqualTo(0L);
        assertThat(tracker.elapsed()).isGreaterThanOrEqualTo(elapsed);
    }
}
