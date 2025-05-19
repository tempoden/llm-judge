package com.github.tempoden.llmjudge.backend.scorers;

import org.junit.Test;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ScoreCombinerTest {

    @Test
    public void testVotingSingleMajorityElement() {
        List<Integer> input = List.of(1, 1, 3, 4, 1, 2, 1);
        assertEquals(1, ScoreCombiners.votingCombine(input));
    }

    @Test
    public void testVotingTieWithLargerPreferred() {
        List<Integer> input = List.of(1, 1, 5, 6, 7, 6);
        assertEquals(6,  ScoreCombiners.votingCombine(input));
    }

    @Test
    public void testVotingAllUniqueElements() {
        List<Integer> input = List.of(3, 1, 2, 4);
        assertEquals(4,  ScoreCombiners.votingCombine(input));
    }

    @Test
    public void testVotingSingleElementList() {
        List<Integer> input = List.of(0);
        assertEquals(0,  ScoreCombiners.votingCombine(input));
    }

    @Test
    public void testVotingEmptyElementList() {
        List<Integer> input = List.of();
        assertEquals(Integer.MIN_VALUE,  ScoreCombiners.votingCombine(input));
    }

}
