package com.github.tempoden.llmjudge.backend.scoring;

import com.github.tempoden.llmjudge.backend.concurrency.CancellationToken;
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
    public static final ChatModel defaultModel = ChatModel.GPT_4_1;

    private final OpenAIClientAsync client;
    private final CancellationToken cancel;

    public OpenAIScorer(@NotNull OpenAIClientAsync client) {
        this.client = client;
        this.cancel = null;
    }

    public OpenAIScorer(@NotNull OpenAIClientAsync client, @NotNull CancellationToken cancel) {
        this.client = client;
        this.cancel = cancel;
    }

    @Override
    public int scoreN(@NotNull ScoringItem item, int times, @NotNull ScoreCombiner combiner) {
        if (cancel != null && cancel.isCancelled()) {
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
                .instructions(ScorerUtil.JUDGE_PROMPT)
                .input("""
                        Input: %s
                        Reference: %s
                        Result: %s
                        """.formatted(item.question(), item.reference(), item.answer()))
                .model(defaultModel)
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

       return ScorerUtil.parseScore(content.asOutputText().text());
    }
}
