package com.github.tempoden.llmjudge.backend.scorers;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ScoreCombiners {
    public static int votingCombine(List<Integer> scores) {
        Map<Integer, Long> frequencyMap = scores.stream()
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));

        int result = Integer.MIN_VALUE;
        long maxCount = 0;

        // Find the most frequent element (and largest in case of tie)
        for (Map.Entry<Integer, Long> entry : frequencyMap.entrySet()) {
            int value = entry.getKey();
            long count = entry.getValue();

            if (count > maxCount || (count == maxCount && value > result)) {
                maxCount = count;
                result = value;
            }
        }

        return result;
    }
}
