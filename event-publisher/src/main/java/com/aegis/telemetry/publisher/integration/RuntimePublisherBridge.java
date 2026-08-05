package com.aegis.telemetry.publisher.integration;

import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.publisher.core.RuntimeEventPublisher;
import com.aegis.telemetry.sdk.integration.RuntimeEventSink;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class RuntimePublisherBridge implements RuntimeEventSink {

    private final RuntimeEventPublisher publisher;

    public RuntimePublisherBridge(RuntimeEventPublisher publisher) {
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
    }

    public CompletableFuture<Void> publish(RuntimeEvent event) {
        return publisher.publish(event);
    }

    public CompletableFuture<Void> publishBatch(List<RuntimeEvent> events) {
        return publisher.publishBatch(events);
    }
}
