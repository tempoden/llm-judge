package com.github.tempoden.llmjudge.backend.scorers;

import java.util.List;

public interface Scorer {
    int score(ScoringItem item);
    int scoreN(List<ScoringItem> items, ScoresCombiner combiner);
}
