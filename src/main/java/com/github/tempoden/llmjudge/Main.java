package com.github.tempoden.llmjudge;

import com.github.tempoden.llmjudge.backend.scoring.OpenAIScorer;
import com.github.tempoden.llmjudge.backend.scoring.ScoreCombiners;
import com.github.tempoden.llmjudge.backend.scoring.ScoringItem;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

public class Main {

    public static void main(String[] args) {
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        OpenAIScorer openAI = new OpenAIScorer(client);
        int score = openAI.scoreN(new ScoringItem(
                "When did the movie karate kid come out?",
                "1984 or 2010",
                "1984"
        ), 10, ScoreCombiners::votingCombine);

        System.out.println("Score: " + score);
    }
}
