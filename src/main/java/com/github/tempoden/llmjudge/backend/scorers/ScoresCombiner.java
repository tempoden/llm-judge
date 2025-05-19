package com.github.tempoden.llmjudge.backend.scorers;

import java.util.List;

public interface ScoresCombiner {
    int combineScores(List<Integer> scores);
}
