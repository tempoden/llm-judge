package com.github.tempoden.llmjudge.backend.scoring;

import org.junit.Test;

import static org.junit.Assert.*;

public class ScoreParsingTest {

    @Test
    public void testValidScoreWithinRange() {
        assertEquals(5, parseScore("[[5]]"));
        assertEquals(0, parseScore("[[0]]"));
        assertEquals(10, parseScore("[[10]]"));
    }

    @Test(expected = ScoringException.class)
    public void testScoreBelowMinimum() {
        parseScore("[[-1]]");
    }

    @Test(expected = ScoringException.class)
    public void testScoreAboveMaximum() {
        parseScore("[[11]]");
    }

    @Test(expected = ScoringException.class)
    public void testMissingScore() {
        parseScore("No score here");
    }

    @Test(expected = ScoringException.class)
    public void testMalformedPattern1() {
        parseScore("[[not_a_number]]");
    }

    @Test(expected = ScoringException.class)
    public void testMalformedPattern2() {
        parseScore("[[1]");
    }

    // Utility method to access static method directly
    private int parseScore(String input) {
        return ScorerUtil.parseScore(input);
    }
}

