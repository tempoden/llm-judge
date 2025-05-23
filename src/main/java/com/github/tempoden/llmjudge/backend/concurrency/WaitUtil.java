package com.github.tempoden.llmjudge.backend.concurrency;

import java.util.concurrent.CompletableFuture;

public final class WaitUtil {

    // I haven't found anything suitable for polling, but recently I have implemented an async future/promise
    // library for C++. Also, I have remembered about Go `select` syntax used for cancellation:
    //
    // select {
    //   case <-taskOutput: ...
    //   case <-ctx.Done(): ...
    // }
    //
    // I guess, this construction kind of emulates this behaviour :/

    // returns true if task was completed successfully
    //         false if it was canceled
    public static <T> boolean waitWithCancel(CompletableFuture<T> workload, CancellationToken cancel) {
        CompletableFuture<?> any = CompletableFuture.anyOf(workload, cancel.impl);

        any.join();

        if (cancel.impl.isDone()) {
            workload.cancel(true);
            return false;
        }

        return true;
    }
}
