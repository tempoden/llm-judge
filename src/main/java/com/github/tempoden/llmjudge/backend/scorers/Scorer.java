package com.github.tempoden.llmjudge.backend.scorers;

import java.util.List;
import java.util.function.Function;

public interface Scorer {
    // It seems like I will leave it unused
    default int score(ScoringItem item) {
        return scoreN(item, 1, ScoreCombiners::votingCombine);
    }

    interface ScoreCombiner extends Function<List<Integer>, Integer> {}
    int scoreN(ScoringItem items, int times, ScoreCombiner combiner);
}
