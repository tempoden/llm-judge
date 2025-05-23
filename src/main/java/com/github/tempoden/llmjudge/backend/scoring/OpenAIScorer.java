package com.github.tempoden.llmjudge.backend.scoring;

import com.github.tempoden.llmjudge.backend.concurrency.WaitUtil;

import com.openai.client.OpenAIClientAsync;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenAIScorer implements Scorer {
    private final OpenAIClientAsync client;
    private final CompletableFuture<Void> cancel;

    public OpenAIScorer(@NotNull OpenAIClientAsync client) {
        this.client = client;
        this.cancel = null;
    }

    public OpenAIScorer(@NotNull OpenAIClientAsync client, @NotNull CompletableFuture<Void> cancel) {
        this.client = client;
        this.cancel = cancel;
    }

    @Override
    public int scoreN(@NotNull ScoringItem item, int times, @NotNull ScoreCombiner combiner) {
        if (cancel != null && cancel.isDone()) {
            throw new ScoringCancelledException();
        }

        List<CompletableFuture<Response>> responseHandles = new ArrayList<>(times);
        for (int i = 0; i < times; i++) {
            responseHandles.add(sendRequest(item));
        }

        CompletableFuture<Void> waitAll = CompletableFuture.allOf(responseHandles.toArray(new CompletableFuture[]{}));

        if (cancel == null) {
            waitAll.join();
        } else {
            boolean isDone = WaitUtil.waitWithCancel(waitAll, cancel);

            if (!isDone) {
                waitAll.cancel(true);
                responseHandles.forEach(rh -> rh.cancel(true));
                throw new ScoringCancelledException();
            }
        }

        // I would rather not deal with CompletableFuture chains cancellation
        // and perform parsing synchronously
        List<Integer> scores = responseHandles.stream()
                .map(CompletableFuture::join)
                .map(this::parseScore)
                .toList();

        return combiner.apply(scores);
    }

    private CompletableFuture<Response> sendRequest(ScoringItem item) {
        ResponseCreateParams params = ResponseCreateParams.builder()
                .instructions(JUDGE_PROMPT)
                .input("""
                        Input: %s
                        Reference: %s
                        Result: %s
                        """.formatted(item.question(), item.reference(), item.answer()))
                .model(ChatModel.GPT_4_1)
                .build();

        return client.responses().create(params);
    }

    private int parseScore(Response response) {
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
            throw new RuntimeException("No match found");
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
