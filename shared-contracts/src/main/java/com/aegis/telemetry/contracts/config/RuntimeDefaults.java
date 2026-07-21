package com.aegis.telemetry.contracts.config;

import java.time.Duration;

public final class RuntimeDefaults {

    public static final double DEFAULT_SAMPLING_RATE = 1.0d;
    public static final boolean PAYLOAD_CAPTURE_ENABLED = true;
    public static final boolean THREAD_CAPTURE_ENABLED = true;
    public static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(30);
    public static final int MAXIMUM_PAYLOAD_SIZE = 10_240;

    private RuntimeDefaults() {
    }
}
