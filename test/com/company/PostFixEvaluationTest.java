package com.company;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostFixEvaluationTest {

    @Test
    void test_getLocation_With_SingleAlphabetCharacters() {
        PostFixEvaluation postFixEvaluation = new PostFixEvaluation();

        assertEquals("a0", postFixEvaluation.getLocation(1, 0));
        assertEquals("m13", postFixEvaluation.getLocation(13, 13));
        assertEquals("z26", postFixEvaluation.getLocation(26, 26));
    }

    @Test
    void test_getLocation_With_MultipleAlphabetCharacters() {
        PostFixEvaluation postFixEvaluation = new PostFixEvaluation();

        assertEquals("aa0", postFixEvaluation.getLocation(26, 0));
        assertEquals("ab10", postFixEvaluation.getLocation(27, 10));
        assertEquals("ay40", postFixEvaluation.getLocation(50, 40));
    }
}