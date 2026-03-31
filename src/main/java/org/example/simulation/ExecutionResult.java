package org.example.simulation;

import java.util.List;

/**
 * Результат одного конкретного виконання програми
 * (одна фіксована міжпотокова послідовність кроків).
 */
public class ExecutionResult {
    private final List<Integer> output;
    private final boolean matchesExpected;

    public ExecutionResult(List<Integer> output, boolean matchesExpected) {
        this.output = output;
        this.matchesExpected = matchesExpected;
    }

    public List<Integer> getOutput() {
        return output;
    }

    public boolean isMatchesExpected() {
        return matchesExpected;
    }
}

