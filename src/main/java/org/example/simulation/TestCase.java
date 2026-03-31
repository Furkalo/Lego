package org.example.simulation;

import java.util.List;

/**
 * Один тест: вхідні дані (для стандартного вводу)
 * та очікуваний вихід (стандартний вивід) після завершення
 * усіх потоків.
 */
public class TestCase {
    private final List<Integer> input;
    private final List<Integer> expectedOutput;

    public TestCase(List<Integer> input, List<Integer> expectedOutput) {
        this.input = input;
        this.expectedOutput = expectedOutput;
    }

    public List<Integer> getInput() {
        return input;
    }

    public List<Integer> getExpectedOutput() {
        return expectedOutput;
    }
}

