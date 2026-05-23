//package com.example.proect_lab123.config;
//
//import liquibase.Liquibase;
//import liquibase.database.Database;
//import liquibase.database.DatabaseFactory;
//import liquibase.database.jvm.JdbcConnection;
//import liquibase.resource.ClassLoaderResourceAccessor;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//
///**
// * Liquibase database migration helper
// */
//public class LiquibaseConfig {
//
//    private static final Logger logger = LogManager.getLogger(LiquibaseConfig.class);
//    private static final String CHANGELOG_FILE = "db/changelog/db.changelog-master.xml";
//
//    /**
//     * Run Liquibase migrations on the database
//     */
//    public static void runMigrations(String dbUrl, String username, String password) {
//        logger.info("Starting Liquibase migrations...");
//
//        try (Connection connection = DriverManager.getConnection(dbUrl, username, password)) {
//            Database database = DatabaseFactory.getInstance()
//                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
//
//            Liquibase liquibase = new Liquibase(CHANGELOG_FILE,
//                    new ClassLoaderResourceAccessor(),
//                    database);
//
//            logger.info("Executing Liquibase update...");
//            liquibase.update("");
//
//            logger.info("✓ Liquibase migrations completed successfully");
//
//        } catch (Exception e) {
//            logger.error("Error running Liquibase migrations", e);
//            throw new RuntimeException("Liquibase migration failed", e);
//        }
//    }
//
//    /**
//     * Rollback Liquibase migrations (for testing)
//     */
//    public static void rollbackMigrations(String dbUrl, String username, String password, int steps) {
//        logger.warn("Rolling back {} migrations", steps);
//
//        try (Connection connection = DriverManager.getConnection(dbUrl, username, password)) {
//            Database database = DatabaseFactory.getInstance()
//                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
//
//            Liquibase liquibase = new Liquibase(CHANGELOG_FILE,
//                    new ClassLoaderResourceAccessor(),
//                    database);
//
//            logger.warn("Executing Liquibase rollback...");
//            liquibase.rollback(steps, "");
//
//            logger.warn("✓ Rollback completed");
//
//        } catch (Exception e) {
//            logger.error("Error rolling back Liquibase migrations", e);
//            throw new RuntimeException("Liquibase rollback failed", e);
//        }
//    }
//
//    /**
//     * Get migration status
//     */
//    public static String getMigrationStatus(String dbUrl, String username, String password) {
//        logger.info("Checking migration status...");
//
//        try (Connection connection = DriverManager.getConnection(dbUrl, username, password)) {
//            Database database = DatabaseFactory.getInstance()
//                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
//
//            Liquibase liquibase = new Liquibase(CHANGELOG_FILE,
//                    new ClassLoaderResourceAccessor(),
//                    database);
//
//            // This would show changelog info
//            logger.info("Migration status check completed");
//            return "OK";
//
//        } catch (Exception e) {
//            logger.error("Error checking migration status", e);
//            return "ERROR: " + e.getMessage();
//        }
//    }
//}
//
