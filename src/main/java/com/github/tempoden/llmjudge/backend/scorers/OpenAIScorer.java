package com.github.tempoden.llmjudge.backend.scorers;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenAIScorer implements Scorer {

    private final OpenAIClient client;

    public OpenAIScorer(OpenAIClient client) {
        this.client = client;
    }

    @Override
    public int score(ScoringItem item) {
        return 0;
    }

    @Override
    public int scoreN(List<ScoringItem> items, ScoresCombiner combiner) {
        return 0;
    }

    public int requestScore(ScoringItem item) {
        ResponseCreateParams params = ResponseCreateParams.builder()
                .instructions(JUDGE_PROMPT)
                .input("""
                        Input: %s
                        Reference: %s
                        Result: %s
                        """.formatted(item.question(), item.reference(), item.answer()))
                .model(ChatModel.GPT_4_1)
                .build();

        Response response = client.responses().create(params);

        if (response.output().size() != 1) {
            throw new RuntimeException("Unexpected response size");
        }

        var output = response.output().getFirst();
        if (!output.isMessage()) {
            throw new RuntimeException("Unexpected response type");
        }

        if (output.asMessage().content().size() != 1) {
            throw new RuntimeException("Unexpected message content size");
        }

        var content = output.asMessage().content().getFirst();
        if (!content.isOutputText()) {
            throw new RuntimeException("Invalid output text");
        }

        System.out.println(content.asOutputText());

        Pattern pattern = Pattern.compile("\\[\\[(\\d+)]]");
        Matcher matcher = pattern.matcher(content.asOutputText().text());

        int score;
        if (matcher.find()) {
            score = Integer.parseInt(matcher.group(1));
        } else {
            throw new RuntimeException("No match found.");
        }

        if (score < 0 || score > 10) {
            throw new RuntimeException("Invalid score value");
        }

        return score;
    }

    private static final String JUDGE_PROMPT = """
        Please act as an impartial judge and evaluate the quality of the response provided by an AI assistant
        to the user question displayed below. Your evaluation should consider factors such as the helpfulness,
        relevance, accuracy, depth, creativity, and level of detail of the response. Begin your evaluation
        by providing a short explanation. Be as objective as possible. After providing your explanation,
        you must rate the response on a scale of 1 to 10 by strictly following this format: [[rating]],
        for example:
        
        Rating: [[5]]
        
        Examples:
        
        Input: What is the capital of France?
        Reference: Paris
        Result: Paris
        
        Rating: [[10]]
        
        
        Input: Two young lovers from feuding families fall in love, but tragically die. Who are they?
        Reference: Romeo and Juliet
        Result: Roman and Julia. Their burrito was not tasty
        
        Rating: [[3]]
        """;
}
