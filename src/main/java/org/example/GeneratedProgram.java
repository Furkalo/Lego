import java.util.*;

public class GeneratedProgram {
    // Спільні змінні
    private static int[] vars = new int[5];

    private static int idx(String name) {
        switch (name) {
            case "x": return 0;
            case "y": return 1;
            case "z": return 2;
            case "a": return 3;
            case "b": return 4;
            default: throw new IllegalArgumentException("Unknown variable: " + name);
        }
    }

    private static class Thread_0 implements Runnable {
        @Override
        public void run() {
            Scanner in = new Scanner(System.in);
            int pc = 1;
            while (pc >= 0) {
                switch (pc) {
                    case 1:
                        vars[idx("x")] = 10;
                        pc = 2;
                        break;
                    case 2:
                        vars[idx("y")] = vars[idx("x")];
                        pc = 3;
                        break;
                    case 3:
                        vars[idx("z")] = 3;
                        pc = 4;
                        break;
                    case 4:
                        vars[idx("a")] = vars[idx("z")];
                        pc = 5;
                        break;
                    case 5:
                        vars[idx("x")] = vars[idx("a")];
                        pc = 6;
                        break;
                    case 6:
                        System.out.println(vars[idx("x")]);
                        pc = -1;
                        break;
                    default:
                        pc = -1; // невідомий вузол - завершення
                        break;
                }
            }
        }
    }

    private static class Thread_1 implements Runnable {
        @Override
        public void run() {
            Scanner in = new Scanner(System.in);
            int pc = 1;
            while (pc >= 0) {
                switch (pc) {
                    case 1:
                        vars[idx("x")] = 20;
                        pc = 2;
                        break;
                    case 2:
                        vars[idx("y")] = vars[idx("x")];
                        pc = 3;
                        break;
                    case 3:
                        vars[idx("z")] = 6;
                        pc = 4;
                        break;
                    case 4:
                        vars[idx("a")] = vars[idx("z")];
                        pc = 5;
                        break;
                    case 5:
                        System.out.println(vars[idx("x")]);
                        pc = -1;
                        break;
                    default:
                        pc = -1; // невідомий вузол - завершення
                        break;
                }
            }
        }
    }

    private static class Thread_2 implements Runnable {
        @Override
        public void run() {
            Scanner in = new Scanner(System.in);
            int pc = 1;
            while (pc >= 0) {
                switch (pc) {
                    case 1:
                        vars[idx("x")] = 30;
                        pc = 2;
                        break;
                    case 2:
                        vars[idx("y")] = vars[idx("x")];
                        pc = 3;
                        break;
                    case 3:
                        vars[idx("z")] = 9;
                        pc = 4;
                        break;
                    case 4:
                        vars[idx("a")] = vars[idx("z")];
                        pc = 5;
                        break;
                    case 5:
                        System.out.println(vars[idx("x")]);
                        pc = -1;
                        break;
                    default:
                        pc = -1; // невідомий вузол - завершення
                        break;
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        List<Thread> threads = new ArrayList<>();
        threads.add(new Thread(new Thread_0()));
        threads.add(new Thread(new Thread_1()));
        threads.add(new Thread(new Thread_2()));
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
    }
}
