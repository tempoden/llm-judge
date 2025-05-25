package com.github.tempoden.llmjudge.backend;

import com.github.tempoden.llmjudge.backend.concurrency.CancellationToken;
import com.github.tempoden.llmjudge.backend.parsing.DataEntry;
import com.github.tempoden.llmjudge.backend.runner.ModelRunner;
import com.github.tempoden.llmjudge.backend.runner.RunCancelledException;
import com.github.tempoden.llmjudge.backend.runner.RunModelException;
import com.github.tempoden.llmjudge.backend.scoring.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class Worker implements Runnable {

    public interface StatusCallback extends Consumer<String> {}
    public interface ErrorHandler extends Consumer<Throwable> {}

    private final ModelRunner runner;
    private final StatusCallback runnerStatus;

    private final Scorer scorer;
    private final int n;
    private final StatusCallback scorerStatus;

    private final DataEntry workload;

    private final CancellationToken cancellationToken;
    private final ErrorHandler errorHandler;

    public Worker(@NotNull ModelRunner runner,
                  @NotNull StatusCallback runnerStatus,
                  @NotNull Scorer scorer,
                  @NotNull StatusCallback scorerStatus,
                  @NotNull DataEntry workload,
                  int judgeN,
                  CancellationToken cancellationToken,
                  ErrorHandler errorHandler) {
        this.runner = runner;
        this.runnerStatus = runnerStatus;
        this.scorer = scorer;
        this.scorerStatus = scorerStatus;
        this.workload = workload;
        this.n = judgeN;
        this.cancellationToken = cancellationToken;
        this.errorHandler = errorHandler;
    }

    private static final String STARTED = "Evaluating...";
    private static final String CANCELED = "Canceled";
    private static final String ERROR = "Evaluation error";

    @Override
    public void run() {
        if (cancellationToken != null && cancellationToken.isCancelled()) {
            return;
        }

        String response;

        try {
            runnerStatus.accept(STARTED);
            response = runner.queryModel(workload.input());
            runnerStatus.accept(response);
        } catch (RunCancelledException ignored) {
            runnerStatus.accept(CANCELED);
            return;
        } catch (RunModelException e) {
            runnerStatus.accept(ERROR);
            if (errorHandler != null) {
                errorHandler.accept(e);
            }
            throw e;
        }

        if (cancellationToken != null && cancellationToken.isCancelled()) {
            return;
        }

        try {
            scorerStatus.accept(STARTED);
            int score = scorer.scoreN(
                    new ScoringItem(
                            workload.input(),
                            workload.referenceOutput(),
                            response),
                    n,
                    ScoreCombiners::votingCombine
            );
            scorerStatus.accept(Integer.toString(score));
        } catch (ScoringCancelledException ignored) {
            scorerStatus.accept(CANCELED);
        } catch (ScoringException e) {
            scorerStatus.accept(ERROR);
            if (errorHandler != null) {
                errorHandler.accept(e);
            }
            throw e;
        }
    }
}
