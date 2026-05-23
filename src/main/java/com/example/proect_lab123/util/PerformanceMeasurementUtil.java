package com.example.proect_lab123.util;

import com.example.proect_lab123.config.DatabaseConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for measuring and comparing database performance metrics.
 */
public class PerformanceMeasurementUtil {
    private static final Logger logger = LogManager.getLogger(PerformanceMeasurementUtil.class);

    /**
     * Measure connection creation performance WITHOUT pooling
     * @param url JDBC URL
     * @param username Database username
     * @param password Database password
     * @param connectionCount Number of connections to create
     * @return Array with [totalTimeMs, avgTimeMs, connectionsPerSecond]
     */
    public static double[] measureConnectionsWithoutPooling(String url, String username, String password, int connectionCount) {
        logger.info("=== Measuring connections WITHOUT pooling ===");
        logger.info("Creating {} connections from scratch (no pooling)", connectionCount);

        List<Connection> connections = new ArrayList<>();
        long startTime = System.nanoTime();

        try {
            for (int i = 0; i < connectionCount; i++) {
                try {
                    Connection conn = DriverManager.getConnection(url, username, password);
                    connections.add(conn);
                    if ((i + 1) % 10 == 0) {
                        logger.debug("Created {} connections", i + 1);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to create connection {}", i, e);
                }
            }

            long endTime = System.nanoTime();
            double totalTimeMs = (double) (endTime - startTime) / 1_000_000;
            double avgTimeMs = (double) totalTimeMs / connectionCount;
            double connectionsPerSecond = (connectionCount * 1000.0) / totalTimeMs;

            logger.info("Results WITHOUT pooling:");
            logger.info("  Total time: {}ms", String.format("%.2f", totalTimeMs));
            logger.info("  Average time per connection: {}ms", String.format("%.2f", avgTimeMs));
            logger.info("  Connections per second: {}", String.format("%.2f", connectionsPerSecond));

            return new double[]{totalTimeMs, avgTimeMs, connectionsPerSecond};
        } finally {
            // Close all connections
            for (Connection conn : connections) {
                try {
                    conn.close();
                } catch (Exception e) {
                    logger.warn("Failed to close connection", e);
                }
            }
        }
    }

    /**
     * Measure connection reuse from pool (simulates HikariCP reuse)
     * @param initialPool Initial pool of connections
     * @param connectionCount Number of reuse operations
     * @return Array with [totalTimeMs, avgTimeMs, connectionsPerSecond]
     */
    public static double[] measureConnectionsWithPooling(List<Connection> initialPool, int connectionCount) {
        logger.info("=== Measuring connections WITH pooling ===");
        logger.info("Reusing {} connections from pool of {} connections", connectionCount, initialPool.size());

        long startTime = System.nanoTime();

        try {
            for (int i = 0; i < connectionCount; i++) {
                // Simulate getting from pool and returning
                Connection conn = initialPool.get(i % initialPool.size());
                if ((i + 1) % 10 == 0) {
                    logger.debug("Reused {} connections", i + 1);
                }
            }

            long endTime = System.nanoTime();
            double totalTimeMs = (double) (endTime - startTime) / 1_000_000;
            double avgTimeMs = (double) totalTimeMs / connectionCount;
            double connectionsPerSecond = (connectionCount * 1000.0) / totalTimeMs;

            logger.info("Results WITH pooling:");
            logger.info("  Total time: {}ms", String.format("%.2f", totalTimeMs));
            logger.info("  Average time per connection: {}ms", String.format("%.4f", avgTimeMs));
            logger.info("  Connections per second: {}", String.format("%.2f", connectionsPerSecond));

            return new double[]{totalTimeMs, avgTimeMs, connectionsPerSecond};
        } finally {
            // Connections stay in pool
        }
    }

    /**
     * Create a pool of connections for pooling tests
     */
    public static List<Connection> createConnectionPool(String url, String username, String password, int poolSize) {
        logger.info("Creating connection pool with size {}", poolSize);
        List<Connection> pool = new ArrayList<>();

        try {
            for (int i = 0; i < poolSize; i++) {
                Connection conn = DriverManager.getConnection(url, username, password);
                pool.add(conn);
                logger.debug("Added connection {} to pool", i + 1);
            }
            logger.info("Connection pool created successfully with {} connections", poolSize);
        } catch (Exception e) {
            logger.error("Failed to create connection pool", e);
            throw new RuntimeException("Error creating connection pool", e);
        }

        return pool;
    }

    /**
     * Close all connections in a pool
     */
    public static void closeConnectionPool(List<Connection> pool) {
        logger.info("Closing connection pool with {} connections", pool.size());
        int closedCount = 0;

        for (Connection conn : pool) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                    closedCount++;
                }
            } catch (Exception e) {
                logger.warn("Failed to close connection", e);
            }
        }

        logger.info("Closed {} connections from pool", closedCount);
    }

    /**
     * Compare performance and return formatted results
     */
    public static String compareResults(double[] withoutPooling, double[] withPooling) {
        double totalTimeWithout = withoutPooling[0];
        double totalTimeWith = withPooling[0];

        // Calculam imbunatatirea
        double improvement = ((totalTimeWithout - totalTimeWith) / totalTimeWithout) * 100;
        double speedup = totalTimeWith > 0 ? totalTimeWithout / totalTimeWith : 0;

        StringBuilder report = new StringBuilder();
        report.append("\n========== PERFORMANCE COMPARISON ==========\n");

        report.append(String.format("WITHOUT Pooling - Total: %.2fms, Avg: %.2fms/conn, Rate: %.2f conn/s\n",
                withoutPooling[0], withoutPooling[1], withoutPooling[2]));

        report.append(String.format("WITH Pooling    - Total: %.2fms, Avg: %.4fms/conn, Rate: %.2f conn/s\n",
                withPooling[0], withPooling[1], withPooling[2]));

        // REPARAT: Am schimbat {0:.1f} in %.1f si % in %%
        report.append(String.format("Improvement: %.1f%% faster (%.1fx speedup)\n", improvement, speedup));
        report.append("============================================\n");

        return report.toString();
    }

    /**
     * Demonstrate connection leak scenario
     */
    public static void demonstrateConnectionLeak(String url, String username, String password, int maxConnections) {
        logger.warn("=== Demonstrating Connection Leak ===");
        logger.warn("Attempting to create {} connections without closing them", maxConnections);

        List<Connection> leakedConnections = new ArrayList<>();
        int createdCount = 0;

        try {
            for (int i = 0; i < maxConnections; i++) {
                try {
                    Connection conn = DriverManager.getConnection(url, username, password);
                    leakedConnections.add(conn);
                    createdCount++;
                    if (i%50==0)
                        logger.warn("Created connection {} (NOT CLOSED - LEAK!)", i + 1);
                } catch (Exception e) {
                    logger.error("Failed to create connection {} - Pool may be exhausted", i + 1);
                    break;
                }
            }
        } finally {
            logger.warn("Total connections created (leaked): {}", createdCount);
            // Close them now (in real scenario, they'd stay open)
            for (Connection conn : leakedConnections) {
                try {
                    conn.close();
                } catch (Exception e) {
                    logger.warn("Failed to close leaked connection", e);
                }
            }
        }
    }

    public static double[] calculateMetrics(long startTime, int count) {
        double totalTimeMs = (System.nanoTime() - startTime) / 1_000_000.0;
        return new double[]{totalTimeMs, totalTimeMs / count, (count * 1000.0) / totalTimeMs};
    }
}