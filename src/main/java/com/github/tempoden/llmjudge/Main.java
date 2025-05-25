package com.github.tempoden.llmjudge;

import com.github.tempoden.llmjudge.backend.concurrency.CancellationToken;
import com.github.tempoden.llmjudge.backend.parsing.*;
import com.github.tempoden.llmjudge.backend.runner.*;
import com.github.tempoden.llmjudge.backend.scoring.*;

import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;

import java.io.FileNotFoundException;
import java.io.FileReader;

// Run eval without GUI
// Used before adding GUI
public class Main {
    public static void main(String[] args) {
        ModelRunner runner = new PythonRunner(
                "C:\\Users\\tempo\\anaconda3\\python.exe",
                "C:\\SHAD\\JB\\llm-judge\\misc\\model\\echo.py"
        );

        OpenAIClientAsync client = OpenAIOkHttpClientAsync.fromEnv();

        DataParser parser = new JSONParser();
        try {
            Content content = parser.parse(new FileReader("C:\\SHAD\\JB\\llm-judge\\misc\\dataset\\demo-10.json"));
            var result = content.data().stream()
                    .map(entry -> new ScoringItem(
                            entry.input(),
                            entry.referenceOutput(),
                            runner.queryModel(entry.referenceOutput())
                    )).map(item -> queryChatGPT(item, client))
                    .toList();
            System.out.println(result);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        System.out.println(runner.queryModel("Hi little?"));
    }

    public static int queryChatGPT(ScoringItem item, OpenAIClientAsync client) {
        CancellationToken cancel = new CancellationToken();
        OpenAIScorer openAI = new OpenAIScorer(client, cancel);

        try {
            int score = openAI.scoreN((item), 2, ScoreCombiners::votingCombine);
            System.out.println("Score: " + score);
            return score;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        cancel.cancel();
        return 0;
    }
}
