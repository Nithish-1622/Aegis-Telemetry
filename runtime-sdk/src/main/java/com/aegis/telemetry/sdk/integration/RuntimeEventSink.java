package com.aegis.telemetry.sdk.integration;

import com.aegis.telemetry.contracts.event.RuntimeEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RuntimeEventSink {

    CompletableFuture<Void> publish(RuntimeEvent event);

    CompletableFuture<Void> publishBatch(List<RuntimeEvent> events);
}