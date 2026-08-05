package com.aegis.telemetry.contracts.enums;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class RuntimeStatusTest {

    @Test
    void containsAllExpectedValues() {
        assertThat(RuntimeStatus.values()).extracting(Enum::name).containsExactly(
                "SUCCESS",
                "FAILED",
                "RETRYING",
                "TIMEOUT",
                "CANCELLED",
                "UNKNOWN"
        );
    }
}
