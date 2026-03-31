package org.example.simulation;

import org.example.model.*;

import java.util.*;

/**
 * Наївний симулятор, що перебирає всі можливі міжпотокові виконання
 * до заданої максимальної кількості кроків (maxSteps).
 *
 * Один крок = виконання одного вузла (інструкції або умови) одним потоком.
 */
public class NaiveProgramSimulator implements ProgramSimulator {

    private static final long MAX_VARIANTS_LIMIT = 100000000; // Обмеження на кількість варіантів для безпеки
    
    private ProgressCallback progressCallback;
    private volatile long checkedVariantsCount = 0;
    private volatile int currentMaxStep = 0;

    @Override
    public List<ExecutionResult> simulateAll(ProgramSpecification spec,
                                             TestCase testCase,
                                             int maxSteps,
                                             boolean[] cancelFlag) {
        return simulateAll(spec, testCase, maxSteps, cancelFlag, null);
    }

    public List<ExecutionResult> simulateAll(ProgramSpecification spec,
                                             TestCase testCase,
                                             int maxSteps,
                                             boolean[] cancelFlag,
                                             ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
        this.checkedVariantsCount = 0;
        this.currentMaxStep = 0;
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("maxSteps must be > 0");
        }

        List<ExecutionResult> results = new ArrayList<>();

        List<Flowchart> flowcharts = spec.getFlowcharts();
        int threadCount = flowcharts.size();
        if (threadCount == 0) {
            return results;
        }

        // Індекси змінних за іменами
        Map<String, Integer> varIndex = new HashMap<>();
        List<String> sharedVars = spec.getSharedVariables();
        for (int i = 0; i < sharedVars.size(); i++) {
            varIndex.put(sharedVars.get(i), i);
        }

        // Початковий стан
        int[] pcs = new int[threadCount];
        for (int i = 0; i < threadCount; i++) {
            pcs[i] = flowcharts.get(i).getStartNodeId();
        }
        int[] vars = new int[sharedVars.size()]; // усі змінні ініціалізовані 0
        List<Integer> input = testCase.getInput();

        dfs(spec, flowcharts, varIndex, pcs, vars,
                0, maxSteps,
                0, // позиція у вхідних даних
                new ArrayList<>(), // вихід
                input,
                testCase.getExpectedOutput(),
                cancelFlag,
                results);

        return results;
    }

    private void dfs(ProgramSpecification spec,
                     List<Flowchart> flowcharts,
                     Map<String, Integer> varIndex,
                     int[] pcs,
                     int[] vars,
                     int steps,
                     int maxSteps,
                     int inputPos,
                     List<Integer> output,
                     List<Integer> input,
                     List<Integer> expectedOutput,
                     boolean[] cancelFlag,
                     List<ExecutionResult> results) {

        if (cancelFlag != null && cancelFlag.length > 0 && cancelFlag[0]) {
            return;
        }

        if (steps > maxSteps) {
            // Досягли ліміту кроків для цієї гілки
            return;
        }

        // Перевірка завершення всіх потоків
        boolean allDone = true;
        for (int pc : pcs) {
            if (pc >= 0) {
                allDone = false;
                break;
            }
        }
        if (allDone) {
            // Перевірка обмеження на кількість варіантів
            if (checkedVariantsCount >= MAX_VARIANTS_LIMIT) {
                return; // Досягли ліміту
            }
            
            boolean matches = output.equals(expectedOutput);
            results.add(new ExecutionResult(new ArrayList<>(output), matches));
            checkedVariantsCount++;
            currentMaxStep = Math.max(currentMaxStep, steps);
            
            // Оновлюємо прогрес частіше для кращої відповіді
            if (progressCallback != null) {
                // Оновлюємо кожні 10 варіантів або на перших 5
                if (checkedVariantsCount <= 5 || checkedVariantsCount % 10 == 0) {
                    progressCallback.onProgress(checkedVariantsCount, currentMaxStep, maxSteps);
                }
            }
            return;
        }
        
        // Перевірка обмеження перед продовженням пошуку
        if (checkedVariantsCount >= MAX_VARIANTS_LIMIT) {
            return;
        }

        int threadCount = flowcharts.size();

        // Для кожного потоку, який ще не завершився, породжуємо гілку
        for (int t = 0; t < threadCount; t++) {
            int pc = pcs[t];
            if (pc < 0) continue; // потік завершився

            Flowchart f = flowcharts.get(t);

            StatementNode sNode = f.getStatementNodes().get(pc);
            ConditionNode cNode = f.getConditionNodes().get(pc);

            if (sNode == null && cNode == null) {
                // Невідомий вузол - трактуємо як завершення потоку
                int[] newPcs = pcs.clone();
                newPcs[t] = -1;
                dfs(spec, flowcharts, varIndex, newPcs, vars.clone(),
                        steps + 1, maxSteps,
                        inputPos,
                        new ArrayList<>(output),
                        input, expectedOutput, cancelFlag, results);
                continue;
            }

            if (sNode != null) {
                simulateStatementNode(flowcharts, varIndex, pcs, vars,
                        steps, maxSteps,
                        inputPos, output, input, expectedOutput,
                        cancelFlag, results, t, sNode);
            } else {
                simulateConditionNode(flowcharts, varIndex, pcs, vars,
                        steps, maxSteps,
                        inputPos, output, input, expectedOutput,
                        cancelFlag, results, t, cNode);
            }
        }
    }

    private void simulateStatementNode(List<Flowchart> flowcharts,
                                       Map<String, Integer> varIndex,
                                       int[] pcs,
                                       int[] vars,
                                       int steps,
                                       int maxSteps,
                                       int inputPos,
                                       List<Integer> output,
                                       List<Integer> input,
                                       List<Integer> expectedOutput,
                                       boolean[] cancelFlag,
                                       List<ExecutionResult> results,
                                       int threadIndex,
                                       StatementNode node) {

        int[] newPcs = pcs.clone();
        int[] newVars = vars.clone();
        List<Integer> newOutput = new ArrayList<>(output);
        int newInputPos = inputPos;

        switch (node.getType()) {
            case ASSIGN_VAR_VAR: {
                int dst = varIndex.get(node.getTargetVar());
                int src = varIndex.get(node.getSourceVar());
                newVars[dst] = newVars[src];
                newPcs[threadIndex] = node.getNextId();
                break;
            }
            case ASSIGN_CONST: {
                int dst = varIndex.get(node.getTargetVar());
                newVars[dst] = node.getConstant();
                newPcs[threadIndex] = node.getNextId();
                break;
            }
            case INPUT: {
                int dst = varIndex.get(node.getTargetVar());
                if (newInputPos >= input.size()) {
                    // Немає більше вхідних даних – ця гілка неможлива
                    return;
                }
                newVars[dst] = input.get(newInputPos);
                newInputPos++;
                newPcs[threadIndex] = node.getNextId();
                break;
            }
            case PRINT: {
                int src = varIndex.get(node.getTargetVar());
                newOutput.add(newVars[src]);
                newPcs[threadIndex] = node.getNextId();
                break;
            }
            default:
                newPcs[threadIndex] = -1;
        }

        dfs(null, flowcharts, varIndex, newPcs, newVars,
                steps + 1, maxSteps,
                newInputPos,
                newOutput,
                input, expectedOutput, cancelFlag, results);
    }

    private void simulateConditionNode(List<Flowchart> flowcharts,
                                       Map<String, Integer> varIndex,
                                       int[] pcs,
                                       int[] vars,
                                       int steps,
                                       int maxSteps,
                                       int inputPos,
                                       List<Integer> output,
                                       List<Integer> input,
                                       List<Integer> expectedOutput,
                                       boolean[] cancelFlag,
                                       List<ExecutionResult> results,
                                       int threadIndex,
                                       ConditionNode node) {

        int[] newPcs = pcs.clone();
        int[] newVars = vars.clone();

        int v = newVars[varIndex.get(node.getVariable())];
        boolean cond;
        switch (node.getOp()) {
            case EQ:
                cond = (v == node.getConstant());
                break;
            case LT:
                cond = (v < node.getConstant());
                break;
            default:
                cond = false;
        }

        newPcs[threadIndex] = cond ? node.getTrueNextId() : node.getFalseNextId();

        dfs(null, flowcharts, varIndex, newPcs, newVars,
                steps + 1, maxSteps,
                inputPos,
                new ArrayList<>(output),
                input, expectedOutput, cancelFlag, results);
    }
}

