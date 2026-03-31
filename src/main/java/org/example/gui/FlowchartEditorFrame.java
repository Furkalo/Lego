package org.example.gui;

import org.example.model.ProgramSpecification;
import org.example.parser.TextFlowchartParser;
import org.example.simulation.ExecutionResult;
import org.example.simulation.NaiveProgramSimulator;
import org.example.simulation.ProgressCallback;
import org.example.simulation.TestCase;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Простіший GUI-редактор, який дозволяє:
 * - створювати новий опис блок-схем (New);
 * - відкривати опис з файлу (Open...);
 * - зберігати опис у файл (Save);
 * - виходити з програми (Exit).
 *
 * Сам опис блок-схем задається як текст у вікні (можна придумати свій формат),
 * але інтерфейс є графічним, як вимагає умова.
 */
public class FlowchartEditorFrame extends JFrame {

    private final JTextArea editorArea;

    public FlowchartEditorFrame() {
        super("Flowchart Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        this.editorArea = new JTextArea();
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        editorArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(editorArea);
        add(scrollPane, BorderLayout.CENTER);

        // Мінімальне меню
        JMenuBar menuBar = new JMenuBar();

        // Меню File
        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open...");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem exitItem = new JMenuItem("Exit");

        newItem.addActionListener(e -> onNew());
        openItem.addActionListener(e -> onOpen());
        saveItem.addActionListener(e -> onSave());
        exitItem.addActionListener(e -> {
            dispose();
            System.exit(0);
        });

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Меню Run
        JMenu runMenu = new JMenu("Run");
        JMenuItem runTestItem = new JMenuItem("Run test...");
        runTestItem.addActionListener(e -> onRunTest());
        runMenu.add(runTestItem);
        menuBar.add(runMenu);

        setJMenuBar(menuBar);

        // При старті можна показати невеликий шаблон/підказку формату
        editorArea.setText(
                "# Тут ви можете описувати блок-схеми для потоків у текстовому форматі.\n" +
                "# Підтримуваний синтаксис:\n" +
                "VARS: x\n" +
                "\n" +
                "THREAD T1:\n" +
                "  1: x = 42 -> 2\n" +
                "  2: PRINT x -> -1\n" +
                "\n" +
                "# Опціонально: тестові дані (якщо є, то Run test не буде питати через діалоги)\n" +
                "TEST:\n" +
                "INPUT: 10\n" +
                "EXPECTED: 42\n" +
                "K: 5\n"
        );
    }

    private void onNew() {
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Очистити поточний опис блок-схеми? Незбережені зміни буде втрачено.",
                "New",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (answer == JOptionPane.OK_OPTION) {
            editorArea.setText("");
        }
    }

    private void onOpen() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append(System.lineSeparator());
                }
                editorArea.setText(sb.toString());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Помилка при відкритті файлу: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onSave() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(editorArea.getText());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Помилка при збереженні файлу: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onRunTest() {
        try {
            String text = editorArea.getText();
            
            // 1. Парсимо текст у блок-схеми
            TextFlowchartParser parser = new TextFlowchartParser();
            ProgramSpecification spec = parser.parse(text);

            // 2. Намагаємося прочитати тестові дані з тексту
            TextFlowchartParser.ParsedTestData testData = parser.parseTestData(text);
            
            List<Integer> input;
            List<Integer> expected;
            int k;

            if (testData != null && testData.isComplete()) {
                // Використовуємо дані з тексту (якщо є повна секція TEST:)
                input = testData.getInput() != null ? testData.getInput() : new ArrayList<>();
                expected = testData.getExpected() != null ? testData.getExpected() : new ArrayList<>();
                k = testData.getK() != null ? testData.getK() : 10;
            } else {
                // Якщо секції TEST: немає або вона неповна - тихо використовуємо значення за замовчуванням,
                // без додаткових діалогів.
                input = new ArrayList<>();
                expected = new ArrayList<>();
                k = 10;
            }

            TestCase testCase = new TestCase(input, expected);
            
            // Створюємо діалог з прогрес-баром
            JDialog progressDialog = new JDialog(this, "Виконання тесту", true);
            progressDialog.setSize(400, 150);
            progressDialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new BorderLayout());
            JLabel statusLabel = new JLabel("Запуск симуляції...");
            statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.add(statusLabel, BorderLayout.NORTH);
            
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            progressBar.setString("0 варіантів перевірено");
            panel.add(progressBar, BorderLayout.CENTER);
            
            JButton cancelButton = new JButton("Скасувати");
            boolean[] cancelFlag = new boolean[]{false};
            cancelButton.addActionListener(e -> {
                cancelFlag[0] = true;
                cancelButton.setEnabled(false);
                statusLabel.setText("Скасування...");
            });
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(cancelButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            progressDialog.add(panel);
            progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            
            // Запускаємо симуляцію в окремому потоці
            SwingWorker<List<ExecutionResult>, Void> worker = new SwingWorker<List<ExecutionResult>, Void>() {
                @Override
                protected List<ExecutionResult> doInBackground() throws Exception {
                    NaiveProgramSimulator simulator = new NaiveProgramSimulator();
                    
                    ProgressCallback callback = (checkedVariants, currentStep, maxSteps) -> {
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText(String.format("Перевірено: %d варіантів, крок: %d/%d", 
                                    checkedVariants, currentStep, maxSteps));
                            // Оновлюємо прогрес-бар (приблизно, бо не знаємо точну кількість)
                            // Використовуємо логарифмічну шкалу для кращого відображення
                            int progress = (int) Math.min(95, Math.log10(Math.max(1, checkedVariants)) * 10);
                            progressBar.setValue(progress);
                            progressBar.setString(String.format("%d варіантів перевірено", checkedVariants));
                        });
                    };
                    
                    return simulator.simulateAll(spec, testCase, k, cancelFlag, callback);
                }
                
                @Override
                protected void done() {
                    progressDialog.dispose();
                    
                    try {
                        List<ExecutionResult> results = get();
                        
                        long total = results.size();
                        long ok = results.stream().filter(ExecutionResult::isMatchesExpected).count();

                        StringBuilder msg = new StringBuilder();
                        msg.append("Перевірено варіантів виконання: ").append(total).append("\n");
                        msg.append("Коректних (вихід збігається з очікуваним): ").append(ok).append("\n");
                        
                        if (total >= 1000000000) {
                            msg.append("\n⚠ Увага: Досягнуто ліміт у 1,000,000 варіантів.\n");
                            msg.append("Результат може бути неповним. Спробуйте зменшити K або кількість потоків.\n");
                        }

                        if (!results.isEmpty()) {
                            msg.append("\nПриклад виходу першого варіанту:\n");
                            msg.append(results.get(0).getOutput());
                        }

                        JOptionPane.showMessageDialog(FlowchartEditorFrame.this,
                                msg.toString(),
                                "Результат тесту",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        if (cancelFlag[0]) {
                            JOptionPane.showMessageDialog(FlowchartEditorFrame.this,
                                    "Тестування скасовано користувачем.",
                                    "Скасовано",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(FlowchartEditorFrame.this,
                                    "Помилка під час запуску тесту:\n" + ex.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            };
            
            worker.execute();
            progressDialog.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Помилка під час запуску тесту:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<Integer> parseIntList(String text) {
        List<Integer> result = new ArrayList<>();
        String[] parts = text.trim().split("\\s+");
        for (String p : parts) {
            if (p.isEmpty()) continue;
            result.add(Integer.parseInt(p));
        }
        return result;
    }
}

