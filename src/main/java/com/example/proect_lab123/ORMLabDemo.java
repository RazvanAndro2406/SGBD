package com.example.proect_lab123;

import com.example.proect_lab123.config.Config;
import com.example.proect_lab123.service.ORMLabService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Scanner;
import java.util.Properties;

/**
 * ORM Lab Demo - Console Application
 * Demonstrates Hibernate ORM and Connection Pooling features:
 * - Task A: Connection creation overhead (with vs without pooling)
 * - Task B: Connection leak detection
 * - CRUD operations using Hibernate
 * - Batch operations performance
 *
 * Run this class directly to test ORM functionality with SQLite or PostgreSQL.
 */
public class ORMLabDemo {
    private static final Logger logger = LogManager.getLogger(ORMLabDemo.class);
    private static ORMLabService service;

    public static void main(String[] args) {
        logger.info("=== ORM Lab 3 Demo Started ===");
        displayWelcome();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down HikariCP pool...");
            service.shutdown();
        }));

        try {
            // Initialize service with properties
            Properties props = Config.getProperties();
            service = new ORMLabService(props);
            logger.info("ORMLabService initialized successfully");

            // Main menu loop
            Scanner scanner = new Scanner(System.in);
            boolean running = true;

            while (running) {
                displayMenu();
                System.out.print("\nEnter your choice (1-7): ");

                try {
                    String choice = scanner.nextLine().trim();

                    switch (choice) {
                        case "1":
                            taskAConnectionOverhead();
                            break;
                        case "2":
                            taskBConnectionLeaks();
                            break;
                        case "3":
                            demoCRUDOperations();
                            break;
                        case "4":
                            demoBatchOperations();
                            break;
                        case "5":
                            running = false;
                            logger.info("Exiting ORM Lab Demo");
                            System.out.println("\nGoodbye!");
                            break;

                        case "6":
                            taskAConnectionOverheadHikari();
                            break;
                        case "7":
                            taskBConnectionLeaksHikari();
                            break;
                        default:
                            System.out.println("\nInvalid choice. Please enter 1-7.");
                    }
                } catch (Exception e) {
                    logger.error("Error during demo execution", e);
                    System.out.println("\nError: " + e.getMessage());
                    e.printStackTrace();
                }

                if (running) {
                    System.out.println("\n" + "=".repeat(60));
                }
            }

            scanner.close();
        } catch (Exception e) {
            logger.error("Failed to initialize ORM Lab Demo", e);
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void displayWelcome() {
        System.out.println("""
                This demo shows:
                  • Connection creation overhead with and without pooling
                  • Connection leak detection and prevention
                  • CRUD operations using Hibernate ORM
                  • Batch operations performance
                """);
    }

    private static void displayMenu() {
        System.out.println("""
            === ORM Lab Menu ===
            1. Connection Overhead (Pooling)
            2. Connection Leak Test
            3. CRUD Operations Demo
            4. Batch Insert Performance
            5. Exit
            6. Connection Overhead Hikari(Pooling)
            7. Connection Leak Test Hikari(Pooling)
            ====================
            """);
    }

    private static void taskAConnectionOverheadHikari() {
        System.out.println("\n--- TASK A: Benchmark Performanta Conexiuni ---");
        System.out.print("Introdu numarul de conexiuni pentru test (ex: 100): ");
        Scanner scanner = new Scanner(System.in);
        try {
            int count = Integer.parseInt(scanner.nextLine());

            System.out.println("[...] Testam DriverManager (Fara Pool)...");
            double[] resultsWithout = service.measureDriverManager(count);

            System.out.println("[...] Testam HikariCP (Cu Pool)...");
            double[] resultsWith = service.measureHikari(count);

            // 3. Afisam tabelul comparativ folosind utilitarul tau
            String report = com.example.proect_lab123.util.PerformanceMeasurementUtil.compareResults(resultsWithout, resultsWith);
            System.out.println(report);

        } catch (NumberFormatException e) {
            System.out.println("[!] Eroare: Te rog introdu un numar valid.");
        } catch (Exception e) {
            logger.error("Eroare in Task A", e);
            System.out.println("[!] Eroare la executie: " + e.getMessage());
        }
    }

    private static void taskAConnectionOverhead() {
        System.out.println("\n Task A: Connection Creation Overhead Benchmark");
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n--- Task A: Connection Creation Overhead Benchmark ---");
        System.out.print("Enter the number of connections: ");
        int connectionCount;
        try {
            connectionCount = Integer.parseInt(scanner.nextLine());
            service.taskAConnectionOverhead(connectionCount);
            System.out.println("\n Task A completed. Check logs for detailed results.");
        } catch (Exception e) {
            logger.error("Error in Task A", e);
            System.out.println(" Error: " + e.getMessage());
        }
    }

    private static void taskBConnectionLeaksHikari() {
        System.out.println("\n--- TASK B: Demonstratie Connection Leak ---");

        try {
            service.demonstrateLeak();

            System.out.println("\n[INFO] Testul s-a terminat. Observa in log-uri cum a 11-a cerere a dat Timeout.");
        } catch (Exception e) {
            logger.error("Eroare in Task B", e);
            System.out.println("[!] Eroare neasteptata: " + e.getMessage());
        }
    }
    private static void taskBConnectionLeaks() {
        System.out.println("\n Task B: Connection Leak Detection");
        System.out.println("Demonstrates what happens when connections aren't closed:\n");

        System.out.println("Scenario 1: Improper resource management (LEAK)");
        System.out.println("   Connections opened but not closed");
        System.out.println("   Pool becomes exhausted after a few operations\n");

        System.out.println("Scenario 2: Proper resource management (NO LEAK)");
        System.out.println("   Using try-with-resources to auto-close connections");
        System.out.println("   Pool remains healthy\n");

        System.out.println("Note: number of connections to see the effect on the pool: ");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine().trim();
        int connections = input.isEmpty() ? 100 : Integer.parseInt(input);


        try {
            service.taskBConnectionLeaks(connections);
            System.out.println("\n Task B completed. Check logs for detailed results.");
        } catch (Exception e) {
            logger.error("Error in Task B", e);
            System.out.println(" Error: " + e.getMessage());
        }
    }

    private static void demoCRUDOperations() {
        System.out.println("\n  CRUD Operations Demo");
        System.out.println("Demonstrates Hibernate ORM CRUD operations:");
        System.out.println("  Create (INSERT) - Add new actors and movies");
        System.out.println("  Read (SELECT) - Retrieve entities by ID");
        System.out.println("  Update (UPDATE) - Modify existing entities");
        System.out.println("  Delete (DELETE) - Remove entities\n");

        System.out.println("Compare this with raw JDBC vs Hibernate ORM\n");

        try {
            service.demonstrateCRUDOperations();
            System.out.println("\n CRUD demo completed. Check logs for SQL generated by ORM.");
        } catch (Exception e) {
            logger.error("Error in CRUD demo", e);
            System.out.println(" Error: " + e.getMessage());
        }
    }

    private static void demoBatchOperations() {
        System.out.println("\n Batch Insert Operations Performance");
        System.out.println("Demonstrates bulk insert performance with Hibernate:\n");

        System.out.print("Enter number of records to insert (default 100): ");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine().trim();
        int batchSize = input.isEmpty() ? 100 : Integer.parseInt(input);

        System.out.println("\nThis will:");
        System.out.println("  Create " + batchSize + " actor records");
        System.out.println("  Use batch processing for efficiency");
        System.out.println("  Measure total execution time");
        System.out.println("  Calculate records per second\n");

        try {
            service.testBatchOperationsPerformance(batchSize);
            System.out.println("\nBatch operations completed. Check logs for performance metrics.");
        } catch (Exception e) {
            logger.error("Error in batch operations", e);
            System.out.println(" Error: " + e.getMessage());
        }
    }
}