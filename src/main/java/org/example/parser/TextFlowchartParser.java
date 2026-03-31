package org.example.parser;

import org.example.model.ConditionNode;
import org.example.model.ConditionOp;
import org.example.model.Flowchart;
import org.example.model.ProgramSpecification;
import org.example.model.StatementNode;
import org.example.model.StatementType;

import java.util.ArrayList;
import java.util.List;

/**
 * Дуже простий парсер текстового опису блок-схем з вікна редактора.
 *
 * Формат (приклад):
 *
 * VARS: x, y
 * THREAD T1:
 *   1: x = 5 -> 2
 *   2: PRINT x -> -1
 *
 * TEST:
 * INPUT: 10
 * EXPECTED: 10
 * K: 5
 *
 * Підтримувані команди:
 *   V = C         (присвоєння константи)
 *   V1 = V2       (присвоєння змінної)
 *   INPUT V
 *   PRINT V       (вивести значення спільної змінної V на стандартний вивід)
 *   V == C        (умова: значення V рівне C; після "->" два id: trueNextId, falseNextId)
 *   V < C         (умова: значення V менше C; після "->" два id: trueNextId, falseNextId)
 *
 * Для звичайних команд: nextId після "->" — один id або -1 для завершення потоку.
 * Для умов: після "->" два id через кому: trueNextId, falseNextId (наприклад: 3, 4).
 */
public class TextFlowchartParser {

    public static class ParsedTestData {
        private final List<Integer> input;
        private final List<Integer> expected;
        private final Integer k;

        public ParsedTestData(List<Integer> input, List<Integer> expected, Integer k) {
            this.input = input;
            this.expected = expected;
            this.k = k;
        }

        public List<Integer> getInput() {
            return input;
        }

        public List<Integer> getExpected() {
            return expected;
        }

        public Integer getK() {
            return k;
        }

        public boolean isComplete() {
            return input != null && expected != null && k != null;
        }
    }

    public ProgramSpecification parse(String text) {
        ProgramSpecification spec = new ProgramSpecification();

        String[] lines = text.split("\\R");

        Flowchart currentFlowchart = null;
        String currentThreadName = null;
        Integer startNodeId = null;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue; // коментар або порожній рядок
            }

            if (line.startsWith("VARS:")) {
                // Оголошення спільних змінних: VARS: x, y, z
                String varsPart = line.substring("VARS:".length()).trim();
                if (!varsPart.isEmpty()) {
                    String[] vars = varsPart.split(",");
                    for (String v : vars) {
                        String name = v.trim();
                        if (!name.isEmpty()) {
                            spec.addSharedVariable(name);
                        }
                    }
                }
                continue;
            }

            if (line.toUpperCase().startsWith("TEST:")) {
                // Секція TEST: - завершуємо парсинг блок-схем
                // Завершуємо останній потік (якщо був)
                if (currentFlowchart != null) {
                    spec.addFlowchart(currentFlowchart);
                    currentFlowchart = null;
                    currentThreadName = null;
                }
                break; // Виходимо з циклу, бо далі йдуть тестові дані
            }

            if (line.startsWith("THREAD ")) {
                // Завершуємо попередній потік (якщо був)
                if (currentFlowchart != null) {
                    spec.addFlowchart(currentFlowchart);
                }

                // Новий потік
                String namePart = line.substring("THREAD ".length()).trim();
                if (namePart.endsWith(":")) {
                    namePart = namePart.substring(0, namePart.length() - 1).trim();
                }
                currentThreadName = namePart;
                startNodeId = null;
                currentFlowchart = null;
                continue;
            }

            // Решта рядків трактуємо як кроки потоку:
            // <id>: <command> -> <nextId>
            if (currentThreadName == null) {
                throw new IllegalArgumentException("Рядок кроку зустрівся до оголошення THREAD: " + line);
            }

            int colonIdx = line.indexOf(':');
            int arrowIdx = line.lastIndexOf("->");
            if (colonIdx <= 0 || arrowIdx <= colonIdx) {
                throw new IllegalArgumentException("Некоректний формат рядка кроку: " + line);
            }

            String idPart = line.substring(0, colonIdx).trim();
            String commandPart = line.substring(colonIdx + 1, arrowIdx).trim();
            String nextPart = line.substring(arrowIdx + 2).trim();

            int id = Integer.parseInt(idPart);

            if (startNodeId == null) {
                startNodeId = id;
                currentFlowchart = new Flowchart(currentThreadName, startNodeId);
            }

            ConditionNode condNode = parseConditionNode(id, commandPart, nextPart);
            if (condNode != null) {
                currentFlowchart.addConditionNode(condNode);
            } else {
                int nextId = Integer.parseInt(nextPart.trim());
                StatementNode node = parseStatementNode(id, commandPart, nextId);
                currentFlowchart.addStatementNode(node);
            }
        }

        // Додаємо останній потік, якщо він є
        if (currentFlowchart != null) {
            spec.addFlowchart(currentFlowchart);
        }

        return spec;
    }

    /**
     * Парсить тестові дані з тексту (секція TEST:).
     * Повертає null, якщо тестові дані не знайдено або не повні.
     */
    public ParsedTestData parseTestData(String text) {
        String[] lines = text.split("\\R");
        boolean inTestSection = false;
        List<Integer> input = null;
        List<Integer> expected = null;
        Integer k = null;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            if (line.toUpperCase().startsWith("TEST:")) {
                inTestSection = true;
                continue;
            }

            if (inTestSection) {
                if (line.toUpperCase().startsWith("INPUT:")) {
                    String inputStr = line.substring("INPUT:".length()).trim();
                    input = parseIntList(inputStr);
                } else if (line.toUpperCase().startsWith("EXPECTED:")) {
                    String expectedStr = line.substring("EXPECTED:".length()).trim();
                    expected = parseIntList(expectedStr);
                } else if (line.toUpperCase().startsWith("K:")) {
                    String kStr = line.substring("K:".length()).trim();
                    try {
                        k = Integer.parseInt(kStr);
                        if (k < 1) k = 1;
                        if (k > 20) k = 20;
                    } catch (NumberFormatException ex) {
                        // ігноруємо некоректне K
                    }
                }
            }
        }

        if (input != null || expected != null || k != null) {
            return new ParsedTestData(input, expected, k);
        }
        return null;
    }

    private List<Integer> parseIntList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> result = new ArrayList<>();
        String[] parts = str.trim().split("\\s+");
        for (String part : parts) {
            if (!part.isEmpty()) {
                try {
                    result.add(Integer.parseInt(part));
                } catch (NumberFormatException ex) {
                    // ігноруємо некоректні числа
                }
            }
        }
        return result;
    }

    /**
     * Парсить умовний вузол V==C або V<C.
     * Формат nextPart: "trueNextId, falseNextId" (два цілих через кому).
     * Повертає null, якщо команда не є умовою.
     */
    private ConditionNode parseConditionNode(int id, String command, String nextPart) {
        String trimmed = command.trim();
        if (trimmed.isEmpty()) return null;

        ConditionOp op = null;
        String variable;
        int constant;

        int eq2 = trimmed.indexOf("==");
        if (eq2 >= 0) {
            op = ConditionOp.EQ;
            variable = trimmed.substring(0, eq2).trim();
            String right = trimmed.substring(eq2 + 2).trim();
            try {
                constant = Integer.parseInt(right);
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            int lt = trimmed.indexOf("<");
            if (lt >= 0) {
                op = ConditionOp.LT;
                variable = trimmed.substring(0, lt).trim();
                String right = trimmed.substring(lt + 1).trim();
                try {
                    constant = Integer.parseInt(right);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else {
                return null;
            }
        }

        if (variable.isEmpty()) return null;

        // nextPart має бути "trueNextId, falseNextId"
        String[] nextIds = nextPart.split(",");
        if (nextIds.length != 2) return null;
        try {
            int trueNextId = Integer.parseInt(nextIds[0].trim());
            int falseNextId = Integer.parseInt(nextIds[1].trim());
            return new ConditionNode(id, variable, op, constant, trueNextId, falseNextId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private StatementNode parseStatementNode(int id, String command, int nextId) {
        // Можливі форми:
        // V = C
        // V1 = V2
        // INPUT V
        // PRINT V
        String trimmed = command.trim();

        if (trimmed.toUpperCase().startsWith("INPUT ")) {
            String var = trimmed.substring("INPUT ".length()).trim();
            return new StatementNode(id, StatementType.INPUT, var, null, null, nextId);
        }

        if (trimmed.toUpperCase().startsWith("PRINT ")) {
            String var = trimmed.substring("PRINT ".length()).trim();
            return new StatementNode(id, StatementType.PRINT, var, null, null, nextId);
        }

        // Присвоєння
        int eqIdx = trimmed.indexOf('=');
        if (eqIdx <= 0) {
            throw new IllegalArgumentException("Некоректна команда (очікується '=', INPUT або PRINT): " + command);
        }

        String left = trimmed.substring(0, eqIdx).trim();
        String right = trimmed.substring(eqIdx + 1).trim();

        // Якщо right можна розпарсити як ціле число — константа
        try {
            int constant = Integer.parseInt(right);
            return new StatementNode(id, StatementType.ASSIGN_CONST, left, null, constant, nextId);
        } catch (NumberFormatException ex) {
            // Інакше вважаємо, що це V1 = V2
            return new StatementNode(id, StatementType.ASSIGN_VAR_VAR, left, right, null, nextId);
        }
    }
}

