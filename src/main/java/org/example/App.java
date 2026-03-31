package org.example;

import org.example.codegen.JavaCodeGenerator;
import org.example.gui.FlowchartEditorFrame;
import org.example.model.*;

import javax.swing.*;

/**
 * Точка входу для запуску GUI-редактора та тестової генерації коду.
 */
public class App {
    public static void main(String[] args) {
        // 1. Запустити GUI-редактор (мінімальний каркас)
        SwingUtilities.invokeLater(() -> {
            FlowchartEditorFrame frame = new FlowchartEditorFrame();
            frame.setVisible(true);
        });

        // 2. Демонстрація: побудова простої специфікації програми
        //    та генерація Java-коду в консоль.
        ProgramSpecification spec = new ProgramSpecification();
        spec.addSharedVariable("x");

        // Один потік з двома кроками:
        // x = 5; PRINT x;
        Flowchart flowchart = new Flowchart("T1", 1);
        flowchart.addStatementNode(new StatementNode(
                1,
                StatementType.ASSIGN_CONST,
                "x",
                null,
                5,
                2
        ));
        flowchart.addStatementNode(new StatementNode(
                2,
                StatementType.PRINT,
                "x",
                null,
                null,
                -1 // кінець
        ));
        spec.addFlowchart(flowchart);

        JavaCodeGenerator generator = new JavaCodeGenerator();
        String generated = generator.generate(spec, "GeneratedProgram");
        System.out.println("===== Generated Java program =====");
        System.out.println(generated);
        
        // Генеруємо тестові файли різних розмірів
        try {
            org.example.util.LargeFileGenerator.generateLargeFile(50, 50, "samples/large_50_threads.txt");
            System.out.println("\n===== Large test file (50 threads) generated successfully =====");
            
            org.example.util.LargeFileGenerator.generateLargeFile(10, 10, "samples/medium_10_threads.txt");
            System.out.println("===== Medium test file (10 threads) generated successfully =====");
        } catch (Exception ex) {
            System.err.println("Помилка при генерації тестових файлів: " + ex.getMessage());
        }
    }
}
