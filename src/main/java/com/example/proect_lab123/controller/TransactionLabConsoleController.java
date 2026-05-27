package com.example.proect_lab123.controller;

import com.example.proect_lab123.config.Config;
import com.example.proect_lab123.service.TransactionLabService;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TransactionLabConsoleController {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "transaction-lab-ui-worker");
        thread.setDaemon(true);
        return thread;
    });

    private TransactionLabService service;
    private boolean restrictiveMode = false;

    public void start() {
        // În loc de @FXML initialize()
        Properties props = Config.getProperties();
        String url = props.getProperty("lab.db.url", props.getProperty("db.url"));
        String user = props.getProperty("lab.db.username", props.getProperty("db.username", ""));
        String pass = props.getProperty("lab.db.password", props.getProperty("db.password", ""));

        service = new TransactionLabService(url, user, pass);
        runAsync("Setup initial", () -> service.setupLab(this::appendLog));

        // Meniul din consolă
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            // Așteptăm un timp foarte scurt pentru ca logurile asincrone de la setup/task-ul anterior să nu se amestece cu meniul grafic text
            try { TimeUnit.MILLISECONDS.sleep(200); } catch (InterruptedException ignored) {}

            System.out.println("\n================ MENIU TRANZACTII ================");
            System.out.println("[ Mode restrictiv: " + (restrictiveMode ? "ACTIVAT" : "DEACTIVAT") + " ]");
            System.out.println("1. Schimba modul restrictiv (Toggle CheckBox)");
            System.out.println("2. Resetare date (Reset employees)");
            System.out.println("3. Rulare Dirty Read");
            System.out.println("4. Rulare Non-Repeatable Read");
            System.out.println("5. Rulare Phantom Read");
            System.out.println("6. Rulare Lost Update");
            System.out.println("7. Rulare Deadlock");
            System.out.println("8. Rulare Batch Insert Performance");
            System.out.println("0. Iesire aplicatie");
            System.out.print("Alege o optiune: ");

            String option = scanner.nextLine().trim();

            switch (option) {
                case "1":
                    restrictiveMode = !restrictiveMode;
                    System.out.println("-> Modul restrictiv a fost setat pe: " + restrictiveMode);
                    break;
                case "2":
                    onResetData();
                    break;
                case "3":
                    onRunDirtyRead();
                    break;
                case "4":
                    onRunNonRepeatable();
                    break;
                case "5":
                    onRunPhantom();
                    break;
                case "6":
                    onRunLostUpdate();
                    break;
                case "7":
                    onRunDeadlock();
                    break;
                case "8":
                    onRunInsertPerformance();
                    break;
                case "0":
                    running = false;
                    System.out.println("Se inchide aplicatia...");
                    executor.shutdown();
                    break;
                default:
                    System.out.println("Optiune invalida! Incearca din nou.");
            }
        }
        scanner.close();
    }

    private void onResetData() {
        runAsync("Reset employees", () -> service.resetLabData(this::appendLog));
    }

    private void onRunDirtyRead() {
        runAsync("Dirty Read", () -> service.runDirtyReadDemo(isRestrictive(), this::appendLog));
    }

    private void onRunNonRepeatable() {
        runAsync("Non-Repeatable Read", () -> service.runNonRepeatableReadDemo(isRestrictive(), this::appendLog));
    }

    private void onRunPhantom() {
        runAsync("Phantom Read", () -> service.runPhantomReadDemo(isRestrictive(), this::appendLog));
    }

    private void onRunLostUpdate() {
        runAsync("Lost Update", () -> service.runLostUpdateDemo(isRestrictive(), this::appendLog));
    }

    private void onRunDeadlock() {
        runAsync("Deadlock", () -> service.runDeadlockDemo(this::appendLog));
    }

    private void onRunInsertPerformance() {
        runAsync("Batch Insert Performance", () -> service.runInsertPerformanceDemo(this::appendLog));
    }

    private boolean isRestrictive() {
        return restrictiveMode;
    }

    private void runAsync(String label, ThrowingRunnable task) {
        appendLog("\n=== " + label + " ===");
        executor.submit(() -> {
            try {
                task.run();
                appendLog("=== END " + label + " ===");
            } catch (SQLException e) {
                appendLog("[SQL ERROR] " + e.getMessage());
            } catch (Exception e) {
                appendLog("[ERROR] " + e.getMessage());
            }
        });
    }

    // Înlocuiește Platform.runLater și logArea.appendText cu System.out.println
    private void appendLog(String message) {
        String timestamp = LocalTime.now().format(TIME_FORMAT);
        System.out.println("[" + timestamp + "] " + message);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    // Metoda main pentru a rula direct aplicatia din consola
    public static void main(String[] args) {
        TransactionLabConsoleController controller = new TransactionLabConsoleController();
        controller.start();
    }
}