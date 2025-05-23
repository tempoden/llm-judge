package com.github.tempoden.llmjudge.backend.concurrency;

import java.util.concurrent.CompletableFuture;

public class CancellationToken {
    CompletableFuture<Void> impl = new CompletableFuture<>();

    public boolean isCancelled() {
        return impl.isDone();
    }

    public void cancel() {
        impl.complete(null);
    }
}
