package com.example.proect_lab123.controller;

import com.example.proect_lab123.config.Config;
import com.example.proect_lab123.config.HibernateOrmConfig;
import com.example.proect_lab123.service.HibernateTransactionService;
import org.hibernate.SessionFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HibernateTransactionConsoleController {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "tx-demo-worker");
        thread.setDaemon(true);
        return thread;
    });

    private HibernateTransactionService service;
    private SessionFactory sessionFactory;
    private boolean restrictiveMode = false;

    public void start() {
        System.out.println(">>> Initializare Hibernate pentru PostgreSQL...");

        // 1. Incarcam proprietatile de baza
        Properties props = Config.getProperties();

        // 2. Extragem datele specifice laboratorului de tranzactii (PostgreSQL)
        String postgresUrl = props.getProperty("lab.db.url", props.getProperty("db.url"));
        String postgresUser = props.getProperty("lab.db.username", props.getProperty("db.username", ""));
        String postgresPass = props.getProperty("lab.db.password", props.getProperty("db.password", ""));

        // 3. Suprascriem proprietatile standard pentru ca Hibernate sa le foloseasca pe acestea
        props.setProperty("db.url", postgresUrl);
        props.setProperty("db.username", postgresUser);
        props.setProperty("db.password", postgresPass);

        // Fortam crearea/actualizarea tabelei employees2
        props.setProperty("hibernate.hbm2ddl.auto", "update");

        // 4. Cream SessionFactory si instantiem noul serviciu
        sessionFactory = HibernateOrmConfig.createSessionFactory(props);
        service = new HibernateTransactionService(sessionFactory);

        runAsync("Setup initial", () -> service.resetData(this::appendLog));

        // 5. Pornim meniul din consola
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            try { TimeUnit.MILLISECONDS.sleep(300); } catch (InterruptedException ignored) {}

            System.out.println("\n================ MENIU ORM TRANZACTII ================");
            System.out.println("[ Mode restrictiv: " + (restrictiveMode ? "ACTIVAT (Prevenire)" : "DEACTIVAT (Permisiv)") + " ]");
            System.out.println("1. Schimba modul restrictiv");
            System.out.println("2. Resetare date (Tabela employees2)");
            System.out.println("3. Rulare Dirty Read Demo");
            System.out.println("4. Rulare Phantom Read Demo");
            System.out.println("0. Iesire aplicatie");
            System.out.print("Alege o optiune: ");

            String option = scanner.nextLine().trim();

            switch (option) {
                case "1":
                    restrictiveMode = !restrictiveMode;
                    System.out.println("-> Modul restrictiv a fost setat pe: " + restrictiveMode);
                    break;
                case "2":
                    runAsync("Resetare Date", () -> service.resetData(this::appendLog));
                    break;
                case "3":
                    runAsync("Dirty Read", () -> service.runDirtyReadDemo(restrictiveMode, this::appendLog));
                    break;
                case "4":
                    runAsync("Phantom Read", () -> service.runPhantomReadDemo(restrictiveMode, this::appendLog));
                    break;
                case "0":
                    running = false;
                    System.out.println("Se inchide aplicatia...");
                    executor.shutdown();
                    HibernateOrmConfig.shutdown(); // Eliberam resursele Hibernate
                    break;
                default:
                    System.out.println("Optiune invalida! Incearca din nou.");
            }
        }
        scanner.close();
    }

    private void runAsync(String label, ThrowingRunnable task) {
        executor.submit(() -> {
            try {
                task.run();
                appendLog("=== END " + label + " ===\n");
            } catch (Exception e) {
                appendLog("[ERROR] " + e.getMessage());
            }
        });
    }

    private void appendLog(String message) {
        String timestamp = LocalTime.now().format(TIME_FORMAT);
        System.out.println("[" + timestamp + "] " + message);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    // Punctul de intrare in aplicatie
    public static void main(String[] args) {
        HibernateTransactionConsoleController controller = new HibernateTransactionConsoleController();
        controller.start();
    }
}