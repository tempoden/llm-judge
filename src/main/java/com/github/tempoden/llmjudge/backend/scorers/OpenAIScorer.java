package com.github.tempoden.llmjudge.backend.scorers;

import com.openai.client.OpenAIClientAsync;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OpenAIScorer implements Scorer {

    private OpenAIClientAsync client;

    @Override
    public int score(ScoringItem item) {
        return 0;
    }

    @Override
    public int scoreN(List<ScoringItem> items, ScoresCombiner combiner) {
        return 0;
    }

    private CompletableFuture<Integer> requestScore(ScoringItem item) {
        return null;
    }

    private static final String JUDGE_PROMPT = """
            You act as a judge for the model. It gives answers on simple questions and you need
            to tell how good the answer was on scale from 0 to 10. You should give the reasoning and provide
            the number of points on the separate string.
            
            Examples:
            
            Input: What is the capital of France?
            Reference: Paris
            Result: Paris
            
            Answer is correct.
            10
            
            
            Input: Two young lovers from feuding families fall in love, but tragically die. Who are they?
            Reference: Romeo and Juliet
            Result: Roman and Julia. Their burrito was not tasty
            
            There are mistakes in their names, and odd information present.
            4
            
            """;
}
