package com.aegis.telemetry.contracts.trace;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class TraceHeadersTest {

    @Test
    void headerNamesRemainUnchanged() {
        assertThat(TraceHeaders.TRACE_ID).isEqualTo("X-Trace-Id");
        assertThat(TraceHeaders.SPAN_ID).isEqualTo("X-Span-Id");
        assertThat(TraceHeaders.PARENT_SPAN_ID).isEqualTo("X-Parent-Span-Id");
    }
}
