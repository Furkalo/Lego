package org.example.util;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Утиліта для генерації великого тестового файлу з багатьма потоками.
 */
public class LargeFileGenerator {
    
    public static void main(String[] args) throws IOException {
        String basePath = System.getProperty("user.dir");
        
        // Генеруємо великий файл з 50 потоками
        String largeFilePath = basePath + "/samples/large_50_threads.txt";
        generateLargeFile(50, 50, largeFilePath);
        System.out.println("Великий файл згенеровано: " + largeFilePath);
        
        // Генеруємо середній файл з 10 потоками
        String mediumFilePath = basePath + "/samples/medium_10_threads.txt";
        generateLargeFile(10, 10, mediumFilePath);
        System.out.println("Середній файл згенеровано: " + mediumFilePath);
        
        // Генеруємо малий файл з 5 потоками
        String smallFilePath = basePath + "/samples/small_5_threads.txt";
        generateLargeFile(5, 5, smallFilePath);
        System.out.println("Малий файл згенеровано: " + smallFilePath);
    }
    
    public static void generateLargeFile(int threadCount, int operationsPerThread, String outputPath) throws IOException {
        StringBuilder sb = new StringBuilder();
        
        // Генеруємо список змінних (до 100)
        sb.append("VARS: ");
        String[] varNames = {"x", "y", "z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w"};
        for (int i = 0; i < Math.min(varNames.length, 25); i++) {
            if (i > 0) sb.append(", ");
            sb.append(varNames[i]);
        }
        sb.append("\n\n");
        
        // Генеруємо кожен потік
        for (int t = 1; t <= threadCount; t++) {
            sb.append("THREAD T").append(t).append(":\n");
            
            // Генеруємо операції для потоку
            for (int op = 1; op <= operationsPerThread; op++) {
                int nextId = op + 1;
                if (op == operationsPerThread) {
                    nextId = -1; // остання операція
                }
                
                // Чергуємо типи операцій для різноманітності
                int varIndex = (op - 1) % varNames.length;
                String var = varNames[varIndex];
                
                // Остання операція завжди PRINT, щоб був вихід
                if (op == operationsPerThread) {
                    // Остання операція - PRINT значення, яке було присвоєно в першій операції
                    String firstVar = varNames[0];
                    sb.append("  ").append(op).append(": PRINT ").append(firstVar).append(" -> ").append(nextId).append("\n");
                } else if (op % 10 == 0 && op < operationsPerThread) {
                    // Кожна 10-та операція - PRINT
                    sb.append("  ").append(op).append(": PRINT ").append(var).append(" -> ").append(nextId).append("\n");
                } else if (op == 1) {
                    // Перша операція - присвоєння константи
                    sb.append("  ").append(op).append(": ").append(var).append(" = ").append(t * 10).append(" -> ").append(nextId).append("\n");
                } else {
                    // Решта - присвоєння змінної або константи
                    int prevVarIndex = (op - 2) % varNames.length;
                    String prevVar = varNames[prevVarIndex];
                    
                    if (op % 3 == 0) {
                        // Кожна 3-тя - присвоєння константи
                        sb.append("  ").append(op).append(": ").append(var).append(" = ").append(op * t).append(" -> ").append(nextId).append("\n");
                    } else {
                        // Інакше - присвоєння змінної
                        sb.append("  ").append(op).append(": ").append(var).append(" = ").append(prevVar).append(" -> ").append(nextId).append("\n");
                    }
                }
            }
            sb.append("\n");
        }
        
        // Додаємо тестові дані
        sb.append("TEST:\n");
        sb.append("INPUT: \n");
        sb.append("EXPECTED: ");
        // Генеруємо очікуваний вихід (по одному числу від кожного потоку)
        for (int t = 1; t <= threadCount; t++) {
            if (t > 1) sb.append(" ");
            sb.append(t * 10);
        }
        sb.append("\n");
        sb.append("K: 20\n");
        
        // Записуємо у файл
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(sb.toString());
        }
    }
}
