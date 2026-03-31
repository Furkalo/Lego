package org.example.codegen;

import org.example.model.*;

import java.util.List;

/**
 * Генерує вихідний Java-код багатопоточної програми з N потоками
 * на основі набору блок-схем (ProgramSpecification).
 *
 * Спрощення:
 * - усі спільні змінні зберігаються в одному масиві int[] vars;
 * - доступ до змінних є несинхронізованим, щоб зберегти
 *   "чисту" недетермінованість між потоками.
 */
public class JavaCodeGenerator {

    public String generate(ProgramSpecification spec, String className) {
        StringBuilder sb = new StringBuilder();

        sb.append("import java.util.*;\n\n");
        sb.append("public class ").append(className).append(" {\n");

        // Оголошення спільних змінних
        List<String> vars = spec.getSharedVariables();
        int varCount = vars.size();
        sb.append("    // Спільні змінні\n");
        sb.append("    private static int[] vars = new int[").append(varCount).append("];\n\n");

        // Допоміжний метод: отримати індекс змінної за ім'ям
        sb.append("    private static int idx(String name) {\n");
        sb.append("        switch (name) {\n");
        for (int i = 0; i < vars.size(); i++) {
            sb.append("            case \"").append(vars.get(i)).append("\": return ").append(i).append(";\n");
        }
        sb.append("            default: throw new IllegalArgumentException(\"Unknown variable: \" + name);\n");
        sb.append("        }\n");
        sb.append("    }\n\n");

        // Опис кожного потоку як окремого Runnable
        List<Flowchart> flowcharts = spec.getFlowcharts();
        for (int i = 0; i < flowcharts.size(); i++) {
            Flowchart f = flowcharts.get(i);
            String threadClassName = "Thread_" + i;
            sb.append("    private static class ").append(threadClassName).append(" implements Runnable {\n");
            sb.append("        @Override\n");
            sb.append("        public void run() {\n");
            sb.append("            Scanner in = new Scanner(System.in);\n");
            sb.append("            int pc = ").append(f.getStartNodeId()).append(";\n");
            sb.append("            while (pc >= 0) {\n");
            sb.append("                switch (pc) {\n");

            // Вузли-інструкції
            for (StatementNode node : f.getStatementNodes().values()) {
                sb.append("                    case ").append(node.getId()).append(":\n");
                emitStatementNode(sb, node);
                sb.append("                        break;\n");
            }

            // Вузли-умови
            for (ConditionNode node : f.getConditionNodes().values()) {
                sb.append("                    case ").append(node.getId()).append(":\n");
                emitConditionNode(sb, node);
                sb.append("                        break;\n");
            }

            sb.append("                    default:\n");
            sb.append("                        pc = -1; // невідомий вузол - завершення\n");
            sb.append("                        break;\n");
            sb.append("                }\n");
            sb.append("            }\n");
            sb.append("        }\n");
            sb.append("    }\n\n");
        }

        // Точка входу main: запуск усіх потоків
        sb.append("    public static void main(String[] args) throws Exception {\n");
        sb.append("        List<Thread> threads = new ArrayList<>();\n");
        for (int i = 0; i < flowcharts.size(); i++) {
            sb.append("        threads.add(new Thread(new Thread_").append(i).append("()));\n");
        }
        sb.append("        for (Thread t : threads) t.start();\n");
        sb.append("        for (Thread t : threads) t.join();\n");
        sb.append("    }\n");

        sb.append("}\n");

        return sb.toString();
    }

    private void emitStatementNode(StringBuilder sb, StatementNode node) {
        switch (node.getType()) {
            case ASSIGN_VAR_VAR:
                sb.append("                        vars[idx(\"")
                        .append(node.getTargetVar())
                        .append("\")] = vars[idx(\"")
                        .append(node.getSourceVar())
                        .append("\")];\n");
                sb.append("                        pc = ").append(node.getNextId()).append(";\n");
                break;
            case ASSIGN_CONST:
                sb.append("                        vars[idx(\"")
                        .append(node.getTargetVar())
                        .append("\")] = ")
                        .append(node.getConstant())
                        .append(";\n");
                sb.append("                        pc = ").append(node.getNextId()).append(";\n");
                break;
            case INPUT:
                sb.append("                        vars[idx(\"")
                        .append(node.getTargetVar())
                        .append("\")] = in.nextInt();\n");
                sb.append("                        pc = ").append(node.getNextId()).append(";\n");
                break;
            case PRINT:
                sb.append("                        System.out.println(vars[idx(\"")
                        .append(node.getTargetVar())
                        .append("\")]);\n");
                sb.append("                        pc = ").append(node.getNextId()).append(";\n");
                break;
            default:
                sb.append("                        pc = -1;\n");
        }
    }

    private void emitConditionNode(StringBuilder sb, ConditionNode node) {
        sb.append("                        if (");
        sb.append("vars[idx(\"").append(node.getVariable()).append("\")] ");
        switch (node.getOp()) {
            case EQ:
                sb.append("== ");
                break;
            case LT:
                sb.append("< ");
                break;
            default:
                sb.append("== ");
        }
        sb.append(node.getConstant()).append(") {\n");
        sb.append("                            pc = ").append(node.getTrueNextId()).append(";\n");
        sb.append("                        } else {\n");
        sb.append("                            pc = ").append(node.getFalseNextId()).append(";\n");
        sb.append("                        }\n");
    }
}

