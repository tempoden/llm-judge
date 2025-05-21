package com.github.tempoden.llmjudge.backend.scoring;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public interface Scorer {
    // It seems like I will leave it unused
    default int score(@NotNull ScoringItem item) {
        return scoreN(item, 1, ScoreCombiners::votingCombine);
    }

    interface ScoreCombiner extends Function<List<Integer>, Integer> {}
    int scoreN(@NotNull ScoringItem items, int times, @NotNull ScoreCombiner combiner);
}
