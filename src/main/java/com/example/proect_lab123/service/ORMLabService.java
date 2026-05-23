package com.example.proect_lab123.service;

import com.example.proect_lab123.config.DatabaseConfig;
import com.example.proect_lab123.config.HibernateOrmConfig;
import com.example.proect_lab123.domain.Actor;
import com.example.proect_lab123.domain.Movie;
import com.example.proect_lab123.repositoryORM.ActorRepositoryORM;
import com.example.proect_lab123.repositoryORM.MovieRepositoryORM;
import com.example.proect_lab123.util.PerformanceMeasurementUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.example.proect_lab123.util.PerformanceMeasurementUtil.calculateMetrics;

/**
 * ORM Lab Service demonstrating Hibernate ORM usage and connection pooling performance.
 * This service provides demonstrations and tests for Lab 3 requirements.
 * Uses JdbcUtils pattern with Properties for configuration.
 */
public class ORMLabService {
    private static final Logger logger = LogManager.getLogger(ORMLabService.class);

    private Properties props;
    private String databaseUrl;
    private String username;
    private String password;
    private final DataSource dataSource;

    public ORMLabService(Properties props) {
        this.props = props;
        this.databaseUrl = props.getProperty("db.url");
        this.username = props.getProperty("db.username");
        this.password = props.getProperty("db.password");
        this.dataSource = DatabaseConfig.getDataSource(props);
        logger.info("ORMLabService initialized with Properties configuration");
        logger.debug("Database URL: {}", databaseUrl);
    }

    public double[] measureDriverManager(int count) {
        logger.info("Pornire test performanta: DriverManager (fara Pool)");
        long startTime = System.nanoTime();

        for (int i = 0; i < count; i++) {
            try (Connection conn = DriverManager.getConnection(this.databaseUrl, this.username, this.password)) {
                // Do nothing, auto-close prin try-with-resources
            } catch (Exception e) {
                logger.error("Eroare la crearea conexiunii JDBC directe la iteratia {}", i, e);
            }
        }
        return PerformanceMeasurementUtil.calculateMetrics(startTime, count);
    }

    public double[] measureHikari(int count) {
        logger.info("Pornire test performanta: HikariCP (Cu Pool)");
        long startTime = System.nanoTime();

        for (int i = 0; i < count; i++) {
            try (Connection conn = dataSource.getConnection()) {
                // Do nothing, conexiunea se intoarce in pool la close()
            } catch (Exception e) {
                logger.error("Eroare la preluarea conexiunii din pool la iteratia {}", i, e);
            }
        }
        return PerformanceMeasurementUtil.calculateMetrics(startTime, count);
    }
    public void demonstrateLeak() {
        System.out.println("\n=== DEMONSTRATIE CONNECTION LEAK ===");
        System.out.println("Configuratie: Maximum Pool Size = 10, Timeout = 5s");

        List<Connection> leakedConnections = new ArrayList<>();

        try {
            for (int i = 1; i <= 11; i++) {
                System.out.println("Incerc sa scot conexiunea numarul: " + i + "...");

                Connection conn = dataSource.getConnection(); // intentionally not try-with-resources
                leakedConnections.add(conn);

                System.out.println(" Conexiunea " + i + " a fost extrasa cu succes.");
            }
        } catch (Exception e) {
            System.out.println("\n EROARE: Pool epuizat!");
            System.out.println("Mesaj eroare: " + e.getMessage());
            System.out.println("Explicatie: Toate cele 10 conexiuni sunt blocate (leaked). " +
                    "A 11-a cerere a asteptat pana la timeout.");
        } finally {
            // Reset pool state - return all leaked connections
            System.out.println("\n[CLEANUP] Inchidem " + leakedConnections.size() + " conexiuni leaked...");
            for (Connection conn : leakedConnections) {
                try {
                    if (conn != null && !conn.isClosed()) {
                        conn.close(); // returns connection back to HikariCP pool
                    }
                } catch (Exception e) {
                    logger.warn("Failed to close leaked connection", e);
                }
            }
            System.out.println("[CLEANUP] Pool resetat.");
        }
    }
    public void shutdown() {
        DatabaseConfig.closeDataSource();
    }
    /**
     * Task A: Compare connection creation overhead with and without pooling
     */
    public void taskAConnectionOverhead(int connectionCount) {
        logger.info("=== TASK A: Connection Creation Overhead ===");
        logger.info("Testing with {} connections", connectionCount);

        // Test WITHOUT pooling
        double[] resultWithout = PerformanceMeasurementUtil.measureConnectionsWithoutPooling(
                databaseUrl, username, password, connectionCount);

        // Test WITH pooling (simulate HikariCP by reusing connections)
        List<Connection> pool = PerformanceMeasurementUtil.createConnectionPool(
                databaseUrl, username, password, 10); // Create initial pool of 10
        double[] resultWith = PerformanceMeasurementUtil.measureConnectionsWithPooling(
                pool, connectionCount);
        PerformanceMeasurementUtil.closeConnectionPool(pool);

        // Display comparison
        String comparison = PerformanceMeasurementUtil.compareResults(resultWithout, resultWith);
        logger.info(comparison);

        logTaskAResults(resultWithout, resultWith);
    }

    /**
     * Task B: Detect connection leaks
     */
    public void taskBConnectionLeaks(int connections) {
        logger.warn("=== TASK B: Connection Leak Detection ===");
        logger.warn("Scenario 1: Creating connections without closing (LEAK)");


        PerformanceMeasurementUtil.demonstrateConnectionLeak(databaseUrl, username, password, connections);

        logger.info("Scenario 2: Proper resource management (NO LEAK)");
        demonstrateProperResourceManagement(connections);
    }

    /**
     * Demonstrate proper resource management
     */
    private void demonstrateProperResourceManagement(int connectionCount) {
        logger.info("Creating {} connections with proper try-with-resources closure", connectionCount);

        try {
            for (int i = 0; i < connectionCount; i++) {
                try (Connection conn = DriverManager.getConnection(databaseUrl, username, password)) {
                    if(i % 50 == 0)
                        logger.info("Connection {} opened and will be properly closed", i + 1);
                } catch (Exception e) {
                    logger.error("Failed to create connection {}", i + 1, e);
                }
            }
            logger.info("All connections properly closed - NO LEAK");
        } catch (Exception e) {
            logger.error("Error in resource management test", e);
        }
    }

    /**
     * Test CRUD operations using Hibernate ORM
     */
    public void demonstrateCRUDOperations() {
        logger.info("=== ORM CRUD Operations Demo ===");

        try {
            ActorRepositoryORM actorRepo = new ActorRepositoryORM(props);
            MovieRepositoryORM movieRepo = new MovieRepositoryORM(props);

            // Test CREATE
            logger.info("--- Testing CREATE ---");
            testCreate(actorRepo, movieRepo);

            // Test READ
            logger.info("--- Testing READ ---");
            testRead(actorRepo, movieRepo);

            // Test UPDATE
            logger.info("--- Testing UPDATE ---");
            testUpdate(actorRepo);

            // Test DELETE
            logger.info("--- Testing DELETE ---");
            testDelete(actorRepo);

            logger.info("CRUD operations test completed");
        } catch (Exception e) {
            logger.error("Error in CRUD operations test", e);
        }
    }

    /**
     * Test batch operations performance
     */
    public void testBatchOperationsPerformance(int batchSize) {
        logger.info("=== Batch Operations Performance ===");
        logger.info("Testing batch insert with {} records", batchSize);

        try {
            ActorRepositoryORM actorRepo = new ActorRepositoryORM(props);

            // Create test data
            List<Actor> actors = createTestActors(batchSize);

            // Measure batch insert
            long duration = actorRepo.batchInsert(actors);
            logger.info("Batch insert of {} actors completed in {}ms", batchSize, duration);

        } catch (Exception e) {
            logger.error("Error in batch operations test", e);
        }
    }

    // Helper methods
    private void testCreate(ActorRepositoryORM actorRepo, MovieRepositoryORM movieRepo) {
        try {
            Movie movie = new Movie(99L, "Test Movie", "Action", 120.0f);
            movieRepo.save(movie);
            logger.info("Movie created: {}", movie);

            Actor actor = new Actor(99L, "Test Actor", LocalDate.of(1990, 1, 1), 99L);
            actorRepo.save(actor);
            logger.info("Actor created: {}", actor);
        } catch (Exception e) {
            logger.error("Error in CREATE test", e);
        }
    }

    private void testRead(ActorRepositoryORM actorRepo, MovieRepositoryORM movieRepo) {
        try {
            var actor = actorRepo.findOne(99L);
            logger.info("Actor found: {}", actor);

            var movie = movieRepo.findOne(99L);
            logger.info("Movie found: {}", movie);
        } catch (Exception e) {
            logger.error("Error in READ test", e);
        }
    }

    private void testUpdate(ActorRepositoryORM actorRepo) {
        try {
            var optActor = actorRepo.findOne(99L);
            if (optActor.isPresent()) {
                Actor actor = optActor.get();
                actor.setName("Updated Actor");
                actorRepo.update(actor);
                logger.info("Actor updated: {}", actor);
            }
        } catch (Exception e) {
            logger.error("Error in UPDATE test", e);
        }
    }

    private void testDelete(ActorRepositoryORM actorRepo) {
        try {
            var deleted = actorRepo.delete(99L);
            logger.info("Actor deleted: {}", deleted);
        } catch (Exception e) {
            logger.error("Error in DELETE test", e);
        }
    }

    private List<Actor> createTestActors(int count) {
        List<Actor> actors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // Use a constructor that DOES NOT take the 'id' parameter
            Actor actor = new Actor(
                    "Actor " + i,
                    LocalDate.of(1990, 1, 1),
                    null // movieId
            );
            actors.add(actor);
        }
        return actors;
    }

    private void logTaskAResults(double[] without, double[] with) {
        logger.info("\n========== TASK A RESULTS ==========");
        logger.info("Connection Creation Test Results");
        logger.info("Test Configuration:");
        logger.info("  Database: {}", databaseUrl);
        logger.info("  Connections tested: 100");
        logger.info("");

        logger.info("WITHOUT Connection Pooling:");
        // Folosim String.format pentru a controla zecimalele
        logger.info("  Total time: {}ms", String.format("%.2f", without[0]));
        logger.info("  Average time per connection: {}ms", String.format("%.2f", without[1]));
        logger.info("  Rate: {} connections/second", String.format("%.2f", without[2]));

        logger.info("");

        logger.info("WITH Connection Pooling (Simulated HikariCP):");
        logger.info("  Total time: {}ms", String.format("%.2f", with[0]));
        // Aici pastram cele 4 zecimale pentru precizie mare
        logger.info("  Average time per connection: {}ms", String.format("%.4f", with[1]));
        logger.info("  Rate: {} connections/second", String.format("%.2f", with[2]));

        logger.info("");

        double improvement = ((without[0] - with[0]) / without[0]) * 100;
        // Formatam cu o singura zecimala si adaugam manual simbolul %
        logger.info("Improvement: {}% faster with pooling", String.format("%.1f", improvement));
        logger.info("====================================\n");
    }
}