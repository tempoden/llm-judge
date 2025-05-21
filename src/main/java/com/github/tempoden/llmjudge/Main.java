package com.github.tempoden.llmjudge;

import com.github.tempoden.llmjudge.backend.parsing.Content;
import com.github.tempoden.llmjudge.backend.parsing.DataParser;
import com.github.tempoden.llmjudge.backend.parsing.JSONParser;
import com.github.tempoden.llmjudge.backend.runner.ModelRunner;
import com.github.tempoden.llmjudge.backend.runner.PythonRunner;
import com.github.tempoden.llmjudge.backend.scoring.OpenAIScorer;
import com.github.tempoden.llmjudge.backend.scoring.ScoreCombiners;
import com.github.tempoden.llmjudge.backend.scoring.ScoringItem;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Main {
    public static void main(String[] args) {
        ModelRunner runner = new PythonRunner(
                "C:\\Users\\tempo\\anaconda3\\python.exe",
                "C:\\SHAD\\JB\\llm-judge\\misc\\model\\echo.py"
        );

        DataParser parser = new JSONParser();
        try {
            Content content = parser.parse(new FileReader(new File("C:\\SHAD\\JB\\llm-judge\\misc\\dataset\\demo-10.json")));
            var result = content.data().stream()
                    .map(entry -> new ScoringItem(
                            entry.input(),
                            entry.referenceOutput(),
                            runner.queryModel(entry.referenceOutput())
                    )).map(Main::queryChatGPT)
                    .toList();
            System.out.println(result);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        System.out.println(runner.queryModel("Hi little?"));
    }

    public static int queryChatGPT(ScoringItem item) {
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        OpenAIScorer openAI = new OpenAIScorer(client);
        int score = openAI.scoreN((item), 10, ScoreCombiners::votingCombine);

        System.out.println("Score: " + score);

        return score;
    }
}
