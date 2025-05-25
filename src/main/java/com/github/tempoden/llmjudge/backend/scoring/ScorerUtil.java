package com.github.tempoden.llmjudge.backend.scoring;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ScorerUtil {
    public static final String JUDGE_PROMPT = """
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

    public static final int MIN_SCORE = 0;
    public static final int MAX_SCORE = 10;
    public static final Pattern RESPONSE_PATTERN = Pattern.compile("\\[\\[(\\d+)]]");

    public static int parseScore(String judgeResponse) {
        Matcher matcher = RESPONSE_PATTERN.matcher(judgeResponse);

        int score;
        if (matcher.find()) {
            score = Integer.parseInt(matcher.group(1));
        } else {
            throw new ScoringException("Invalid response format. [[score]] of integer type not found");
        }

        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new ScoringException("Invalid score value " + score);
        }

        return score;
    }
}
