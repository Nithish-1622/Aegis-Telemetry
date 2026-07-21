package com.aegis.telemetry.trace.generator;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpanIdGeneratorTest {

    @Test
    void generatesUniqueIds() {
        Set<String> ids = new HashSet<>();

        for (int index = 0; index < 1_000; index++) {
            String spanId = SpanIdGenerator.generateSpanId();
            assertThat(ids).doesNotContain(spanId);
            ids.add(spanId);
            UUID.fromString(spanId);
        }

        assertThat(ids).hasSize(1_000);
    }
}
