package org.example.model;

/**
 * Вузол, що представляє одну інструкцію (оператор) з єдиною наступною вершиною.
 */
public class StatementNode {
    private final int id;
    private final StatementType type;

    // Імена змінних (спільні для всіх потоків)
    private final String targetVar;   // V або V1
    private final String sourceVar;   // V2 (для ASSIGN_VAR_VAR) або null

    // Константа C (для ASSIGN_CONST)
    private final Integer constant;   // null, якщо не використовується

    // Єдиний наступний вузол (за відсутності розгалуження)
    private final int nextId;

    public StatementNode(int id,
                         StatementType type,
                         String targetVar,
                         String sourceVar,
                         Integer constant,
                         int nextId) {
        this.id = id;
        this.type = type;
        this.targetVar = targetVar;
        this.sourceVar = sourceVar;
        this.constant = constant;
        this.nextId = nextId;
    }

    public int getId() {
        return id;
    }

    public StatementType getType() {
        return type;
    }

    public String getTargetVar() {
        return targetVar;
    }

    public String getSourceVar() {
        return sourceVar;
    }

    public Integer getConstant() {
        return constant;
    }

    public int getNextId() {
        return nextId;
    }
}

