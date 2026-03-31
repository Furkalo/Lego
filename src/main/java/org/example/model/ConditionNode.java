package org.example.model;

/**
 * Вузол розгалуження з умовою V == C або V < C.
 * Має два виходи: гілка true та гілка false.
 */
public class ConditionNode {
    private final int id;
    private final String variable; // V
    private final ConditionOp op;  // == або <
    private final int constant;    // C

    private final int trueNextId;
    private final int falseNextId;

    public ConditionNode(int id,
                         String variable,
                         ConditionOp op,
                         int constant,
                         int trueNextId,
                         int falseNextId) {
        this.id = id;
        this.variable = variable;
        this.op = op;
        this.constant = constant;
        this.trueNextId = trueNextId;
        this.falseNextId = falseNextId;
    }

    public int getId() {
        return id;
    }

    public String getVariable() {
        return variable;
    }

    public ConditionOp getOp() {
        return op;
    }

    public int getConstant() {
        return constant;
    }

    public int getTrueNextId() {
        return trueNextId;
    }

    public int getFalseNextId() {
        return falseNextId;
    }
}

