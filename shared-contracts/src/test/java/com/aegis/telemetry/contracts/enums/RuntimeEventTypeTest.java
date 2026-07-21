package com.aegis.telemetry.contracts.enums;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class RuntimeEventTypeTest {

    @Test
    void containsAllExpectedValues() {
        assertThat(RuntimeEventType.values()).extracting(Enum::name).containsExactly(
                "REQUEST_STARTED",
                "REQUEST_COMPLETED",
                "SERVICE_CALL_STARTED",
                "SERVICE_CALL_COMPLETED",
                "RETRY_TRIGGERED",
                "ERROR_OCCURRED",
                "HEARTBEAT"
        );
    }
}
