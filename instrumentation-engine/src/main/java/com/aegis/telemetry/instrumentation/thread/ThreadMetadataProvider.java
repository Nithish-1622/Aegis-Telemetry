package com.aegis.telemetry.instrumentation.thread;

public final class ThreadMetadataProvider {

    public ThreadMetadata capture() {
        Thread thread = Thread.currentThread();
        ThreadGroup group = thread.getThreadGroup();
        return new ThreadMetadata(thread.threadId(), thread.getName(), group == null ? "unknown" : group.getName());
    }

    public record ThreadMetadata(long threadId, String threadName, String threadGroup) {
    }
}
