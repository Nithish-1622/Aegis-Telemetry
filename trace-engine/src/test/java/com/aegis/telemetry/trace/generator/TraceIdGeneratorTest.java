package com.aegis.telemetry.trace.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class TraceIdGeneratorTest {

    @Test
    void generatesUniqueIds() {
        Set<String> ids = new HashSet<>();

        for (int index = 0; index < 1_000; index++) {
            String traceId = TraceIdGenerator.generateTraceId();
            assertThat(ids).doesNotContain(traceId);
            ids.add(traceId);
            UUID.fromString(traceId);
        }

        assertThat(ids).hasSize(1_000);
    }

    @Test
    void isThreadSafe() throws Exception {
        int tasks = 1_000;
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        try {
            List<Callable<String>> callables = new ArrayList<>();
            for (int index = 0; index < tasks; index++) {
                callables.add(TraceIdGenerator::generateTraceId);
            }

            List<Future<String>> futures = executorService.invokeAll(callables);
            Set<String> ids = new HashSet<>();
            for (Future<String> future : futures) {
                String traceId = future.get();
                UUID.fromString(traceId);
                ids.add(traceId);
            }

            assertThat(ids).hasSize(tasks);
        } finally {
            executorService.shutdownNow();
        }
    }
}
