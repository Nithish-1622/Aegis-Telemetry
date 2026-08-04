package com.aegis.telemetry.instrumentation.thread;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadMetadataProviderTest {

    private final ThreadMetadataProvider provider = new ThreadMetadataProvider();

    @Test
    void extractsCurrentThreadMetadata() {
        ThreadMetadataProvider.ThreadMetadata metadata = provider.capture();

        assertThat(metadata.threadId()).isEqualTo(Thread.currentThread().threadId());
        assertThat(metadata.threadName()).isEqualTo(Thread.currentThread().getName());
        assertThat(metadata.threadGroup()).isEqualTo(Thread.currentThread().getThreadGroup().getName());
    }
}
