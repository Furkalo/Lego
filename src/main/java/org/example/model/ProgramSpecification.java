package org.example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Сукупність блок-схем для всіх потоків програми.
 * Містить опис спільних змінних.
 */
public class ProgramSpecification {
    // Імена спільних 32-бітних цілочисельних змінних (до 100).
    private final List<String> sharedVariables = new ArrayList<>();

    // Набір блок-схем (1 <= N <= 100).
    private final List<Flowchart> flowcharts = new ArrayList<>();

    public void addSharedVariable(String name) {
        if (sharedVariables.size() >= 100) {
            throw new IllegalStateException("Максимум 100 спільних змінних");
        }
        sharedVariables.add(name);
    }

    public void addFlowchart(Flowchart flowchart) {
        if (flowcharts.size() >= 100) {
            throw new IllegalStateException("Максимум 100 потоків / блок-схем");
        }
        flowcharts.add(flowchart);
    }

    public List<String> getSharedVariables() {
        return Collections.unmodifiableList(sharedVariables);
    }

    public List<Flowchart> getFlowcharts() {
        return Collections.unmodifiableList(flowcharts);
    }
}

