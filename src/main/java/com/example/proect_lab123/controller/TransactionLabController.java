package com.example.proect_lab123.controller;

import com.example.proect_lab123.config.Config;
import com.example.proect_lab123.service.TransactionLabService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionLabController {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    private TextArea logArea;

    @FXML
    private CheckBox restrictiveModeCheckBox;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "transaction-lab-ui-worker");
        thread.setDaemon(true);
        return thread;
    });

    private TransactionLabService service;

    @FXML
    public void initialize() {
        Properties props = Config.getProperties();
        String url = props.getProperty("lab.db.url", props.getProperty("db.url"));
        String user = props.getProperty("lab.db.username", props.getProperty("db.username", ""));
        String pass = props.getProperty("lab.db.password", props.getProperty("db.password", ""));

        service = new TransactionLabService(url, user, pass);
        runAsync("Setup initial", () -> service.setupLab(this::appendLog));
    }

    @FXML
    private void onResetData() {
        runAsync("Reset employees", () -> service.resetLabData(this::appendLog));
    }

    @FXML
    private void onRunDirtyRead() {
        runAsync("Dirty Read", () -> service.runDirtyReadDemo(isRestrictive(), this::appendLog));
    }

    @FXML
    private void onRunNonRepeatable() {
        runAsync("Non-Repeatable Read", () -> service.runNonRepeatableReadDemo(isRestrictive(), this::appendLog));
    }

    @FXML
    private void onRunPhantom() {
        runAsync("Phantom Read", () -> service.runPhantomReadDemo(isRestrictive(), this::appendLog));
    }

    @FXML
    private void onRunLostUpdate() {
        runAsync("Lost Update", () -> service.runLostUpdateDemo(isRestrictive(), this::appendLog));
    }

    @FXML
    private void onRunDeadlock() {
        runAsync("Deadlock", () -> service.runDeadlockDemo(this::appendLog));
    }

    @FXML
    private void onRunInsertPerformance() {
        runAsync("Batch Insert Performance", () -> service.runInsertPerformanceDemo(this::appendLog));
    }

    @FXML
    private void onClearLog() {
        logArea.clear();
    }

    private boolean isRestrictive() {
        return restrictiveModeCheckBox != null && restrictiveModeCheckBox.isSelected();
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

    private void appendLog(String message) {
        Platform.runLater(() -> {
            String timestamp = LocalTime.now().format(TIME_FORMAT);
            logArea.appendText("[" + timestamp + "] " + message + System.lineSeparator());
        });
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}


