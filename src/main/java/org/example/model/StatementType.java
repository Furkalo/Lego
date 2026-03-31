package org.example.model;

/**
 * Типи допустимих операторів у вузлах-інструкціях.
 */
public enum StatementType {
    ASSIGN_VAR_VAR, // V1 = V2
    ASSIGN_CONST,   // V = C
    INPUT,          // INPUT V
    PRINT           // PRINT V
}

