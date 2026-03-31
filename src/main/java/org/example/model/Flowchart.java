package org.example.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Блок-схема, що описує алгоритм одного потоку.
 * Містить орієнтований граф із до 100 вузлів.
 */
public class Flowchart {
    private final String threadName;
    private final int startNodeId;

    // Вузли-інструкції та вузли-умови зберігаються окремо для простоти.
    private final Map<Integer, StatementNode> statementNodes = new HashMap<>();
    private final Map<Integer, ConditionNode> conditionNodes = new HashMap<>();

    public Flowchart(String threadName, int startNodeId) {
        this.threadName = threadName;
        this.startNodeId = startNodeId;
    }

    public String getThreadName() {
        return threadName;
    }

    public int getStartNodeId() {
        return startNodeId;
    }

    public void addStatementNode(StatementNode node) {
        statementNodes.put(node.getId(), node);
    }

    public void addConditionNode(ConditionNode node) {
        conditionNodes.put(node.getId(), node);
    }

    public Map<Integer, StatementNode> getStatementNodes() {
        return Collections.unmodifiableMap(statementNodes);
    }

    public Map<Integer, ConditionNode> getConditionNodes() {
        return Collections.unmodifiableMap(conditionNodes);
    }
}

