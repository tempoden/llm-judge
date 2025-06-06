package com.github.tempoden.llmjudge.backend.concurrency;

import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.*;

public class WaitUtilTest {

    @Test(timeout = 1000)
    public void testWorkloadCompletesBeforeCancel() {
        CompletableFuture<String> workload = new CompletableFuture<>();
        CancellationToken cancel = new CancellationToken();

        try (ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor()){

            executor.schedule(() -> workload.complete("Done"), 100, TimeUnit.MILLISECONDS);

            boolean result = WaitUtil.waitWithCancel(workload, cancel);

            assertTrue("Should return true when workload completes first", result);
            assertFalse("Cancel future should not be done", cancel.isCancelled());
            assertTrue("Workload should be completed", workload.isDone());
        }
    }

    @Test(timeout = 1000)
    public void testCancelCompletesBeforeWorkload() {
        CompletableFuture<String> workload = new CompletableFuture<>();
        CancellationToken cancel = new CancellationToken();

        try (ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor()) {

            executor.schedule(cancel::cancel, 100, TimeUnit.MILLISECONDS);

            boolean result = WaitUtil.waitWithCancel(workload, cancel);

            assertFalse("Should return false when cancel completes first", result);
            assertTrue("Cancel future should be completed", cancel.isCancelled());
            assertTrue("Workload should be cancelled", workload.isCancelled());
        }
    }
}
