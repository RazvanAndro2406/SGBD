//package com.example.proect_lab123.controller;
//
//import com.example.proect_lab123.config.JdbcUtils;
//import com.example.proect_lab123.service.Lab5MigrationService;
//import javafx.application.Platform;
//import javafx.fxml.FXML;
//import javafx.geometry.Insets;
//import javafx.scene.control.*;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.stage.Stage;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * Controller for Lab 5 - Database Schema Evolution
// * Demonstrates:
// * - Liquibase migrations
// * - Optimistic locking
// * - Soft delete
// * - Audit trail
// */
//public class Lab5MigrationController {
//
//    private static final Logger logger = LogManager.getLogger(Lab5MigrationController.class);
//    private Lab5MigrationService service;
//    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
//    private TextArea outputArea;
//
//    public Lab5MigrationController(JdbcUtils dbUtils) {
//        this.service = new Lab5MigrationService(dbUtils);
//        logger.info("Lab5MigrationController initialized");
//    }
//
//    @FXML
//    public void initialize() {
//        // This is called by FXML if the scene is loaded from FXML
//    }
//
//    /**
//     * Create the UI programmatically
//     */
//    public BorderPane createUI() {
//        BorderPane root = new BorderPane();
//        root.setPadding(new Insets(10));
//
//        // Top: Title
//        Label titleLabel = new Label("LAB 5 - Database Schema Evolution & Concurrency Control");
//        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1976d2;");
//        root.setTop(titleLabel);
//
//        // Center: Tabbed interface
//        TabPane tabPane = createTabs();
//        root.setCenter(tabPane);
//
//        return root;
//    }
//
//    private TabPane createTabs() {
//        TabPane tabPane = new TabPane();
//        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
//
//        // Tab 1: Migrations
//        tabPane.getTabs().add(new Tab("Migrări", createMigrationsTab()));
//
//        // Tab 2: Optimistic Locking
//        tabPane.getTabs().add(new Tab("Optimistic Locking", createOptimisticLockingTab()));
//
//        // Tab 3: Soft Delete
//        tabPane.getTabs().add(new Tab("Soft Delete", createSoftDeleteTab()));
//
//        // Tab 4: Projects Table
//        tabPane.getTabs().add(new Tab("Projects Table", createProjectsTab()));
//
//        // Tab 5: Audit Trail
//        tabPane.getTabs().add(new Tab("Audit Trail", createAuditTrailTab()));
//
//        return tabPane;
//    }
//
//    private VBox createMigrationsTab() {
//        VBox vbox = new VBox(10);
//        vbox.setPadding(new Insets(10));
//
//        Label infoLabel = new Label("Database Migrations (Liquibase)");
//        infoLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
//
//        String info = """
//                Migrările Liquibase sunt executate automat la startup.
//
//                Migrări aplicate:
//                1. Initial schema - tabele actors, movies, employees, departments
//                2. Version column - optimistic locking
//                3. Soft delete columns - is_deleted, deleted_at, deleted_by
//                4. Audit columns - created_at, created_by, updated_at, updated_by
//                5. Projects table - nouă tabelă cu FK la departments
//                6. Indexes - pe department_id, is_deleted, is_active
//
//                Fiecare migrare este reversibilă (rollback).
//                """;
//        TextArea infoArea = new TextArea(info);
//        infoArea.setWrapText(true);
//        infoArea.setEditable(false);
//        infoArea.setPrefHeight(200);
//
//        Button showVersionsBtn = new Button("Afișează versiuni coloane");
//        showVersionsBtn.setStyle("-fx-font-size: 12;");
//        showVersionsBtn.setOnAction(e -> runDemo(() -> service.demoVersionColumn()));
//
//        outputArea = new TextArea();
//        outputArea.setWrapText(true);
//        outputArea.setEditable(false);
//        outputArea.setPrefHeight(300);
//
//        VBox content = new VBox(10);
//        content.getChildren().addAll(infoLabel, infoArea, showVersionsBtn, new Separator(), outputArea);
//
//        return content;
//    }
//
//    private VBox createOptimisticLockingTab() {
//        VBox vbox = new VBox(10);
//        vbox.setPadding(new Insets(10));
//
//        Label titleLabel = new Label("Demonstrație Optimistic Locking");
//        titleLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
//
//        String info = """
//                Optimistic Locking previne Lost Update problems.
//
//                Mecanismul:
//                - Fiecare entitate are o coloană VERSION
//                - Când User A și User B încarcă aceeași entitate, au aceeași versiune
//                - User A actualizează și salvează → VERSION crește
//                - User B încearcă să salveze cu versiune veche → OptimisticLockException
//
//                Avantaje:
//                ✓ Permite multiple lectori simultanei
//                ✓ Nu blochează liniile în BD
//                ✓ Performanță mai bună decât pessimistic locking
//
//                Dezavantaje:
//                ✗ Aplicația trebuie să gestioneze conflictele
//                """;
//        TextArea infoArea = new TextArea(info);
//        infoArea.setWrapText(true);
//        infoArea.setEditable(false);
//        infoArea.setPrefHeight(250);
//
//        Button runDemoBtn = new Button("Rulează demonstrație conflict");
//        runDemoBtn.setStyle("-fx-font-size: 12; -fx-padding: 8;");
//        runDemoBtn.setOnAction(e -> runDemo(() -> service.demoOptimisticLockingConflict()));
//
//        outputArea = new TextArea();
//        outputArea.setWrapText(true);
//        outputArea.setEditable(false);
//        outputArea.setPrefHeight(300);
//
//        VBox content = new VBox(10);
//        content.getChildren().addAll(titleLabel, infoArea, runDemoBtn, new Separator(), outputArea);
//
//        return content;
//    }
//
//    private VBox createSoftDeleteTab() {
//        VBox vbox = new VBox(10);
//        vbox.setPadding(new Insets(10));
//
//        Label titleLabel = new Label("Soft Delete vs Hard Delete");
//        titleLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
//
//        String info = """
//                Soft Delete: marchează ca ștearsă, păstrează în baza de date
//                Hard Delete: șterge permanent din baza de date
//
//                Soft Delete Avantaje:
//                ✓ Păstrează datele pentru audit/history
//                ✓ Ușor de restaurare
//                ✓ Integritate referențială mai bună
//                ✓ Raportare historică posibilă
//
//                Soft Delete Dezavantaje:
//                ✗ Baza de date crește în dimensiune
//                ✗ Interogări trebuie să filtreze is_deleted = false
//                ✗ Constrângeri UNIQUE trebuie revizuite
//
//                Implementare:
//                - Coloană is_deleted (BOOLEAN)
//                - Coloane deleted_at și deleted_by (audit)
//                - @Where(clause = "is_deleted = false") în Hibernate
//                """;
//        TextArea infoArea = new TextArea(info);
//        infoArea.setWrapText(true);
//        infoArea.setEditable(false);
//        infoArea.setPrefHeight(280);
//
//        HBox buttonBox = new HBox(10);
//        buttonBox.setPadding(new Insets(10));
//
//        Button softDeleteBtn = new Button("Demonstrație Soft Delete");
//        softDeleteBtn.setStyle("-fx-font-size: 12;");
//        softDeleteBtn.setOnAction(e -> runDemo(() -> service.demoSoftDelete()));
//
//        Button restoreBtn = new Button("Restaurare înregistrări");
//        restoreBtn.setStyle("-fx-font-size: 12;");
//        restoreBtn.setOnAction(e -> runDemo(() -> service.demoRestoreSoftDeleted()));
//
//        buttonBox.getChildren().addAll(softDeleteBtn, restoreBtn);
//
//        outputArea = new TextArea();
//        outputArea.setWrapText(true);
//        outputArea.setEditable(false);
//        outputArea.setPrefHeight(250);
//
//        VBox content = new VBox(10);
//        content.getChildren().addAll(titleLabel, infoArea, buttonBox, new Separator(), outputArea);
//
//        return content;
//    }
//
//    private VBox createProjectsTab() {
//        VBox vbox = new VBox(10);
//        vbox.setPadding(new Insets(10));
//
//        Label titleLabel = new Label("Tabel Projects - Migrare nouă");
//        titleLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
//
//        String info = """
//                Migrare Sarcina C: Adăugare tabel nou
//
//                CREATE TABLE projects (
//                    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
//                    name VARCHAR(255) NOT NULL,
//                    description TEXT,
//                    start_date DATE,
//                    end_date DATE,
//                    department_id INTEGER,
//                    is_active BOOLEAN DEFAULT TRUE,
//                    version INTEGER DEFAULT 0,
//                    is_deleted BOOLEAN DEFAULT FALSE,
//                    deleted_at TIMESTAMP,
//                    deleted_by VARCHAR(100),
//                    created_at TIMESTAMP,
//                    created_by VARCHAR(100),
//                    updated_at TIMESTAMP,
//                    updated_by VARCHAR(100),
//                    FOREIGN KEY (department_id) REFERENCES departments(id)
//                );
//
//                Caracteristici:
//                - FK la departments table
//                - Version column pentru optimistic locking
//                - Soft delete support
//                - Audit trail completa
//                """;
//        TextArea infoArea = new TextArea(info);
//        infoArea.setWrapText(true);
//        infoArea.setEditable(false);
//        infoArea.setPrefHeight(320);
//
//        Button showProjectsBtn = new Button("Afișează Projects");
//        showProjectsBtn.setStyle("-fx-font-size: 12;");
//        showProjectsBtn.setOnAction(e -> runDemo(() -> service.demoProjectsTable()));
//
//        outputArea = new TextArea();
//        outputArea.setWrapText(true);
//        outputArea.setEditable(false);
//        outputArea.setPrefHeight(250);
//
//        VBox content = new VBox(10);
//        content.getChildren().addAll(titleLabel, infoArea, showProjectsBtn, new Separator(), outputArea);
//
//        return content;
//    }
//
//    private VBox createAuditTrailTab() {
//        VBox vbox = new VBox(10);
//        vbox.setPadding(new Insets(10));
//
//        Label titleLabel = new Label("Audit Trail - Migrare D");
//        titleLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
//
//        String info = """
//                Audit Trail Columns:
//                - created_at: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
//                - created_by: VARCHAR(100) - care utilizator a creat
//                - updated_at: TIMESTAMP - ultima actualizare
//                - updated_by: VARCHAR(100) - care utilizator a actualizat
//
//                Pentru soft delete, se adaugă:
//                - deleted_at: TIMESTAMP - când a fost ștearsă
//                - deleted_by: VARCHAR(100) - cine a șters-o
//
//                Beneficii:
//                ✓ Compliență reglementară (audit compliance)
//                ✓ Urmărire modificări date
//                ✓ Investigare incidente
//                ✓ Raportare de responsabilitate
//                ✓ Recuperare date din greșeli
//
//                Implementare Hibernate:
//                @Column(name = "created_at", updatable = false)
//                protected LocalDateTime createdAt = LocalDateTime.now();
//
//                @Column(name = "updated_by")
//                protected String updatedBy;
//                """;
//        TextArea infoArea = new TextArea(info);
//        infoArea.setWrapText(true);
//        infoArea.setEditable(false);
//        infoArea.setPrefHeight(300);
//
//        Button showAuditBtn = new Button("Afișează Audit Trail");
//        showAuditBtn.setStyle("-fx-font-size: 12;");
//        showAuditBtn.setOnAction(e -> runDemo(() -> service.demoAuditTrail()));
//
//        outputArea = new TextArea();
//        outputArea.setWrapText(true);
//        outputArea.setEditable(false);
//        outputArea.setPrefHeight(250);
//
//        VBox content = new VBox(10);
//        content.getChildren().addAll(titleLabel, infoArea, showAuditBtn, new Separator(), outputArea);
//
//        return content;
//    }
//
//    /**
//     * Run a demo asynchronously and update output
//     */
//    private void runDemo(Runnable demo) {
//        executorService.submit(() -> {
//            try {
//                String result = "";
//
//                if (demo != null) {
//                    // Get the result from service methods
//                    // This is a simplified version - you might need to adjust
//                    if (demo.toString().contains("demoVersionColumn")) {
//                        result = service.demoVersionColumn();
//                    } else if (demo.toString().contains("demoOptimisticLockingConflict")) {
//                        result = service.demoOptimisticLockingConflict();
//                    } else if (demo.toString().contains("demoSoftDelete")) {
//                        result = service.demoSoftDelete();
//                    } else if (demo.toString().contains("demoProjectsTable")) {
//                        result = service.demoProjectsTable();
//                    } else if (demo.toString().contains("demoAuditTrail")) {
//                        result = service.demoAuditTrail();
//                    } else if (demo.toString().contains("demoRestoreSoftDeleted")) {
//                        result = service.demoRestoreSoftDeleted();
//                    }
//                }
//
//                final String finalResult = result;
//                Platform.runLater(() -> {
//                    if (outputArea != null) {
//                        outputArea.setText(finalResult);
//                    }
//                });
//
//            } catch (Exception e) {
//                logger.error("Error running demo", e);
//                Platform.runLater(() -> {
//                    if (outputArea != null) {
//                        outputArea.setText("Eroare: " + e.getMessage());
//                    }
//                });
//            }
//        });
//    }
//
//    public void shutdown() {
//        executorService.shutdown();
//        logger.info("Lab5MigrationController shutdown");
//    }
//}
//
