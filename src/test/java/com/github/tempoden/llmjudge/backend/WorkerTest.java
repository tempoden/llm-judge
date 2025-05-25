// This is an AI slop I have generated with ChatGPT for fun
// I was wondering whether it will be able to generate some
// tests for my worker, and decided to leave it.
// (coverage won't make it better by itself) :D

package com.github.tempoden.llmjudge.backend;

import com.github.tempoden.llmjudge.backend.concurrency.CancellationToken;
import com.github.tempoden.llmjudge.backend.parsing.DataEntry;
import com.github.tempoden.llmjudge.backend.runner.ModelRunner;
import com.github.tempoden.llmjudge.backend.scoring.Scorer;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.*;

import static org.mockito.Mockito.*;

public class WorkerTest {

    private ModelRunner modelRunner;
    private Scorer scorer;
    private Worker.StatusCallback runnerStatus;
    private Worker.StatusCallback scorerStatus;
    private DataEntry workload;
    private CancellationToken token;

    @Before
    public void setUp() {
        modelRunner = mock(ModelRunner.class);
        scorer = mock(Scorer.class);
        runnerStatus = mock(Worker.StatusCallback.class);
        scorerStatus = mock(Worker.StatusCallback.class);

        workload = new DataEntry("input", "expected");
        token = mock(CancellationToken.class);
    }

    @Test
    public void testNormalExecution() {
        when(modelRunner.queryModel("input")).thenReturn("response");
        when(scorer.scoreN(any(), eq(4), any())).thenReturn(42);
        when(token.isCancelled()).thenReturn(false);

        Worker worker = new Worker(modelRunner, runnerStatus, scorer, scorerStatus, workload, 4, token, null);
        worker.run();

        verify(runnerStatus).accept("Evaluating...");
        verify(runnerStatus).accept("response");
        verify(scorerStatus).accept("Evaluating...");
        verify(scorerStatus).accept("42");
    }

    @Test
    public void testCancelledBeforeStart() {
        when(token.isCancelled()).thenReturn(true);

        Worker worker = new Worker(modelRunner, runnerStatus, scorer, scorerStatus, workload, 1, token, null);
        worker.run();

        verifyNoInteractions(runnerStatus, scorerStatus, modelRunner, scorer);
    }

    @Test
    public void testCancelledBeforeScoring() {
        when(token.isCancelled()).thenReturn(false).thenReturn(true); // first false, then true
        when(modelRunner.queryModel("input")).thenReturn("response");

        Worker worker = new Worker(modelRunner, runnerStatus, scorer, scorerStatus, workload, 1, token, null);
        worker.run();

        verify(runnerStatus).accept("Evaluating...");
        verify(runnerStatus).accept("response");

        verify(scorerStatus, never()).accept("Evaluating...");
        verify(scorer, never()).scoreN(any(), anyInt(), any());
    }

    @Test
    public void testThreadedCancellationMidExecution() throws Exception {
        CountDownLatch blockModel = new CountDownLatch(1);
        CountDownLatch blockMain = new CountDownLatch(1);

        when(token.isCancelled()).thenReturn(false);
        when(modelRunner.queryModel("input")).thenAnswer(invocation -> {
            blockMain.countDown(); // Let the main thread know we're here
            blockModel.await();    // Wait until cancellation is triggered
            return "delayed response";
        });

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {

            Worker worker = new Worker(modelRunner, runnerStatus, scorer, scorerStatus, workload, 1, token, null);
            Future<?> future = executor.submit(worker);

            // Wait until modelRunner.queryModel has been entered
            blockMain.await();

            // Simulate cancellation mid-execution
            when(token.isCancelled()).thenReturn(true);
            blockModel.countDown(); // Let the model finish

            future.get(2, TimeUnit.SECONDS); // Wait for worker to complete

            verify(runnerStatus).accept("Evaluating...");
            verify(runnerStatus).accept("delayed response");

            verify(scorerStatus, never()).accept("Evaluating...");
            verify(scorer, never()).scoreN(any(), anyInt(), any());

            executor.shutdown();
        }
    }
}
