package com.github.tempoden.llmjudge.backend;

import com.github.tempoden.llmjudge.backend.concurrency.CancellationToken;
import com.github.tempoden.llmjudge.backend.parsing.DataEntry;
import com.github.tempoden.llmjudge.backend.runner.ModelRunner;
import com.github.tempoden.llmjudge.backend.scoring.Scorer;
import com.github.tempoden.llmjudge.backend.scoring.ScoringItem;

import java.util.function.Consumer;

public class Worker implements Runnable {

    public interface StatusCallback extends Consumer<String> {}

    private final ModelRunner runner;
    private final StatusCallback runnerStatus;

    private final Scorer scorer;
    private final StatusCallback scorerStatus;

    private final DataEntry workload;

    private final CancellationToken cancellationToken;

    public Worker(ModelRunner runner,
                  StatusCallback runnerStatus,
                  Scorer scorer,
                  StatusCallback scorerStatus,
                  DataEntry workload,
                  CancellationToken cancellationToken) {
        this.runner = runner;
        this.runnerStatus = runnerStatus;
        this.scorer = scorer;
        this.scorerStatus = scorerStatus;
        this.workload = workload;
        this.cancellationToken = cancellationToken;
    }

    private static final String STARTED = "Evaluating...";

    @Override
    public void run() {
        if (cancellationToken.isCancelled()) {
            return;
        }

        runnerStatus.accept(STARTED);
        String response = runner.queryModel(workload.input());
        runnerStatus.accept(response);

        if (cancellationToken.isCancelled()) {
            return;
        }

        scorerStatus.accept(STARTED);
        int score = scorer.score(new ScoringItem(
                workload.input(),
                workload.referenceOutput(),
                response
        ));
        scorerStatus.accept(Integer.toString(score));
    }
}
