package com.example.proect_lab123.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TransactionLabService {
    private static final int BENCHMARK_TOTAL_ROWS = 5000;
    private static final int BENCHMARK_COMMIT_BATCH_SIZE = 100;
    private static final int BENCHMARK_EXECUTE_BATCH_SIZE = 50;

    private static final String CREATE_LAB_SQL = """
            CREATE TABLE IF NOT EXISTS employees (
                id INT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                department_id INT NOT NULL,
                salary NUMERIC(12,2) NOT NULL CHECK (salary >= 0)
            );

            CREATE TABLE IF NOT EXISTS employees_benchmark (
                id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                department_id INT NOT NULL,
                salary NUMERIC(12,2) NOT NULL CHECK (salary >= 0)
            )
            """;

    private static final String RESET_LAB_SQL = """
            DELETE FROM employees;
            DELETE FROM employees_benchmark;

            INSERT INTO employees (id, name, department_id, salary) VALUES
                (1, 'Angajat 1', 5, 5000),
                (2, 'Angajat 2', 5, 5200),
                (3, 'Angajat 3', 3, 4600)
            """;

    private final String url;
    private final String username;
    private final String password;

    public TransactionLabService(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public void setupLab(Consumer<String> log) throws SQLException {
        ensureDatabaseExists(log);
        executeScript("db/postgresql/01_create_transaction_lab.sql");
        resetLabData(log);
    }

    private void ensureDatabaseExists(Consumer<String> log) throws SQLException {
        try (Connection ignored = DriverManager.getConnection(url, username, password)) {
            return;
        } catch (SQLException e) {
            if (!"3D000".equals(e.getSQLState())) {
                throw e;
            }
        }

        String dbName = extractDatabaseName(url);
        String adminUrl = replaceDatabaseName(url, "postgres");
        log.accept("[SETUP] Baza '" + dbName + "' nu exista. Incerc creare automata...");

        try (Connection conn = DriverManager.getConnection(adminUrl, username, password);
             Statement statement = conn.createStatement()) {
            statement.execute("CREATE DATABASE " + dbName);
            log.accept("[SETUP] Baza '" + dbName + "' a fost creata.");
        } catch (SQLException createError) {
            if ("42P04".equals(createError.getSQLState())) {
                log.accept("[SETUP] Baza '" + dbName + "' exista deja.");
                return;
            }
            throw createError;
        }
    }

    public void resetLabData(Consumer<String> log) throws SQLException {
        executeScript("db/postgresql/02_reset_transaction_lab.sql");
        log.accept("[SETUP] employees + employees_benchmark resetate la datele initiale.");
        logFinalState(log);
    }

    public void runInsertPerformanceDemo(Consumer<String> log) throws SQLException {
        log.accept("[Batch Performance] Comparam 5000 inserari pe 3 abordari");
        executeScript("db/postgresql/01_create_transaction_lab.sql");

        resetBenchmarkTable();
        long autoCommitNanos = benchmarkAutoCommit();
        int autoCommitRows = countBenchmarkRows();
        log.accept(formatBenchmarkLine("Abordarea 1: Auto-commit (1 tranzactie / insert)", autoCommitNanos, autoCommitRows));

        resetBenchmarkTable();
        long commitPerHundredNanos = benchmarkCommitPerHundred();
        int commitPerHundredRows = countBenchmarkRows();
        log.accept(formatBenchmarkLine("Abordarea 2: Commit la fiecare 100 inserari", commitPerHundredNanos, commitPerHundredRows));

        resetBenchmarkTable();
        long oneTransactionBatchNanos = benchmarkSingleTransactionBatch();
        int oneTransactionBatchRows = countBenchmarkRows();
        log.accept(formatBenchmarkLine("Abordarea 3: Tranzactie unica + executeBatch la 50", oneTransactionBatchNanos, oneTransactionBatchRows));

        long fastest = Math.min(autoCommitNanos, Math.min(commitPerHundredNanos, oneTransactionBatchNanos));
        log.accept("[Batch Performance] Castigator: " + benchmarkWinner(
                autoCommitNanos,
                commitPerHundredNanos,
                oneTransactionBatchNanos,
                fastest));
    }

    public void runDirtyReadDemo(boolean restrictive, Consumer<String> log) throws InterruptedException {
        log.accept("[Dirty Read] Pornire demo in modul: " + modeName(restrictive));
        CountDownLatch aUpdated = new CountDownLatch(1);
        CountDownLatch bRead = new CountDownLatch(1);

        Thread txA = new Thread(() -> {
            try (Connection conn = openConnection(Connection.TRANSACTION_READ_COMMITTED)) {
                log.accept("TxA: BEGIN (READ COMMITTED)");
                updateSalary(conn, 1, BigDecimal.valueOf(10000));
                log.accept("TxA: salary=10000 (necomis)");
                aUpdated.countDown();
                sleepMillis(2500);
                conn.rollback();
                log.accept("TxA: ROLLBACK efectuat.");
            } catch (Exception e) {
                log.accept("TxA ERROR: " + e.getMessage());
            }
        }, "dirty-read-A");

        Thread txB = new Thread(() -> {
            try {
                await(aUpdated);
                int iso = restrictive ? Connection.TRANSACTION_READ_COMMITTED : Connection.TRANSACTION_READ_UNCOMMITTED;
                try (Connection conn = openConnection(iso)) {
                    log.accept("TxB: BEGIN (" + isolationName(iso) + ")");
                    BigDecimal salary = readSalary(conn, 1);
                    log.accept("TxB: salary citit=" + salary);
                    conn.commit();
                    log.accept("TxB: COMMIT");
                    if (!restrictive) {
                        log.accept("OBS: PostgreSQL nu permite dirty read; READ UNCOMMITTED este tratat ca READ COMMITTED.");
                    }
                }
            } catch (Exception e) {
                log.accept("TxB ERROR: " + e.getMessage());
            } finally {
                bRead.countDown();
            }
        }, "dirty-read-B");

        txA.start();
        txB.start();
        txA.join();
        txB.join();
        await(bRead);
        logFinalState(log);
    }

    public void runNonRepeatableReadDemo(boolean restrictive, Consumer<String> log) throws InterruptedException {
        log.accept("[Non-Repeatable Read] Pornire demo in modul: " + modeName(restrictive));
        CountDownLatch firstRead = new CountDownLatch(1);
        CountDownLatch bCommitted = new CountDownLatch(1);

        Thread txA = new Thread(() -> {
            int iso = restrictive ? Connection.TRANSACTION_REPEATABLE_READ : Connection.TRANSACTION_READ_COMMITTED;
            try (Connection conn = openConnection(iso)) {
                log.accept("TxA: BEGIN (" + isolationName(iso) + ")");
                BigDecimal first = readSalary(conn, 1);
                log.accept("TxA: Prima citire=" + first);
                firstRead.countDown();
                await(bCommitted);
                BigDecimal second = readSalary(conn, 1);
                log.accept("TxA: A doua citire=" + second);
                conn.commit();
                log.accept("TxA: COMMIT");
            } catch (Exception e) {
                log.accept("TxA ERROR: " + e.getMessage());
            }
        }, "non-repeatable-A");

        Thread txB = new Thread(() -> {
            try {
                await(firstRead);
                try (Connection conn = openConnection(Connection.TRANSACTION_READ_COMMITTED)) {
                    log.accept("TxB: BEGIN");
                    updateSalary(conn, 1, BigDecimal.valueOf(12000));
                    conn.commit();
                    log.accept("TxB: salary=12000, COMMIT");
                }
            } catch (Exception e) {
                log.accept("TxB ERROR: " + e.getMessage());
            } finally {
                bCommitted.countDown();
            }
        }, "non-repeatable-B");

        txA.start();
        txB.start();
        txA.join();
        txB.join();
        logFinalState(log);
    }

    public void runPhantomReadDemo(boolean restrictive, Consumer<String> log) throws InterruptedException {
        log.accept("[Phantom Read] Pornire demo in modul: " + modeName(restrictive));
        CountDownLatch firstCount = new CountDownLatch(1);
        CountDownLatch bCommitted = new CountDownLatch(1);

        Thread txA = new Thread(() -> {
            int iso = restrictive ? Connection.TRANSACTION_SERIALIZABLE : Connection.TRANSACTION_READ_COMMITTED;
            try (Connection conn = openConnection(iso)) {
                log.accept("TxA: BEGIN (" + isolationName(iso) + ")");
                int c1 = countDepartment(conn, 5);
                log.accept("TxA: Prima numaratoare dep=5 -> " + c1);
                firstCount.countDown();
                await(bCommitted);
                int c2 = countDepartment(conn, 5);
                log.accept("TxA: A doua numaratoare dep=5 -> " + c2);
                conn.commit();
                log.accept("TxA: COMMIT");
            } catch (SQLTransactionRollbackException e) {
                log.accept("TxA SERIALIZATION: " + e.getMessage());
            } catch (Exception e) {
                log.accept("TxA ERROR: " + e.getMessage());
            }
        }, "phantom-A");

        Thread txB = new Thread(() -> {
            try {
                await(firstCount);
                try (Connection conn = openConnection(Connection.TRANSACTION_READ_COMMITTED)) {
                    log.accept("TxB: BEGIN");
                    int nextId = nextEmployeeId(conn);
                    insertEmployee(conn, nextId, "Angajat Nou", 5, BigDecimal.valueOf(4300));
                    conn.commit();
                    log.accept("TxB: Angajat nou inserat, COMMIT");
                }
            } catch (Exception e) {
                log.accept("TxB ERROR: " + e.getMessage());
            } finally {
                bCommitted.countDown();
            }
        }, "phantom-B");

        txA.start();
        txB.start();
        txA.join();
        txB.join();
        logFinalState(log);
    }

    public void runLostUpdateDemo(boolean restrictive, Consumer<String> log) throws InterruptedException {
        log.accept("[Lost Update] Pornire demo in modul: " + modeName(restrictive));
        CountDownLatch aRead = new CountDownLatch(1);
        CountDownLatch bCommitted = new CountDownLatch(1);

        Thread txA = new Thread(() -> {
            int iso = restrictive ? Connection.TRANSACTION_SERIALIZABLE : Connection.TRANSACTION_READ_COMMITTED;
            try (Connection conn = openConnection(iso)) {
                log.accept("TxA: BEGIN (" + isolationName(iso) + ")");
                BigDecimal salary = readSalary(conn, 1);
                BigDecimal newSalary = salary.add(BigDecimal.valueOf(1000));
                log.accept("TxA: citit=" + salary + ", calculez=" + newSalary);
                aRead.countDown();
                await(bCommitted);
                updateSalary(conn, 1, newSalary);
                conn.commit();
                log.accept("TxA: UPDATE la " + newSalary + ", COMMIT");
            } catch (SQLTransactionRollbackException e) {
                log.accept("TxA SERIALIZATION: " + e.getMessage());
            } catch (Exception e) {
                log.accept("TxA ERROR: " + e.getMessage());
            }
        }, "lost-update-A");

        Thread txB = new Thread(() -> {
            int iso = restrictive ? Connection.TRANSACTION_SERIALIZABLE : Connection.TRANSACTION_READ_COMMITTED;
            try {
                await(aRead);
                try (Connection conn = openConnection(iso)) {
                    log.accept("TxB: BEGIN (" + isolationName(iso) + ")");
                    BigDecimal salary = readSalary(conn, 1);
                    BigDecimal newSalary = salary.add(BigDecimal.valueOf(500));
                    log.accept("TxB: citit=" + salary + ", calculez=" + newSalary);
                    updateSalary(conn, 1, newSalary);
                    conn.commit();
                    log.accept("TxB: UPDATE la " + newSalary + ", COMMIT");
                }
            } catch (SQLTransactionRollbackException e) {
                log.accept("TxB SERIALIZATION: " + e.getMessage());
            } catch (Exception e) {
                log.accept("TxB ERROR: " + e.getMessage());
            } finally {
                bCommitted.countDown();
            }
        }, "lost-update-B");

        txA.start();
        txB.start();
        txA.join();
        txB.join();
        logFinalState(log);
    }

    public void runDeadlockDemo(Consumer<String> log) throws InterruptedException {
        log.accept("[Deadlock] Pornire demo");
        CountDownLatch aLocked = new CountDownLatch(1);
        CountDownLatch bLocked = new CountDownLatch(1);

        Thread txA = new Thread(() -> {
            try (Connection conn = openConnection(Connection.TRANSACTION_READ_COMMITTED)) {
                log.accept("TxA: BEGIN");
                updateSalary(conn, 1, BigDecimal.valueOf(6000));
                log.accept("TxA: row id=1 blocat");
                aLocked.countDown();
                await(bLocked);
                sleepMillis(2000);
                updateSalary(conn, 2, BigDecimal.valueOf(7000));
                conn.commit();
                log.accept("TxA: COMMIT");
            } catch (SQLException e) {
                rollbackSilently(e);
                log.accept("TxA SQLSTATE=" + e.getSQLState() + " MSG=" + e.getMessage());
            } catch (Exception e) {
                log.accept("TxA ERROR: " + e.getMessage());
            }
        }, "deadlock-A");

        Thread txB = new Thread(() -> {
            try (Connection conn = openConnection(Connection.TRANSACTION_READ_COMMITTED)) {
                log.accept("TxB: BEGIN");
                updateSalary(conn, 2, BigDecimal.valueOf(6000));
                log.accept("TxB: row id=2 blocat");
                bLocked.countDown();
                await(aLocked);
                sleepMillis(2000);
                updateSalary(conn, 1, BigDecimal.valueOf(7000));
                conn.commit();
                log.accept("TxB: COMMIT");
            } catch (SQLException e) {
                rollbackSilently(e);
                log.accept("TxB SQLSTATE=" + e.getSQLState() + " MSG=" + e.getMessage());
            } catch (Exception e) {
                log.accept("TxB ERROR: " + e.getMessage());
            }
        }, "deadlock-B");

        txA.start();
        txB.start();
        txA.join(TimeUnit.SECONDS.toMillis(20));
        txB.join(TimeUnit.SECONDS.toMillis(20));

        log.accept("OBS: Deadlock in PostgreSQL apare de obicei cu SQLSTATE 40P01 pentru una dintre tranzactii.");
        logFinalState(log);
    }

    private long benchmarkAutoCommit() throws SQLException {
        long start = System.nanoTime();
        String sql = "INSERT INTO employees_benchmark (name, department_id, salary) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = conn.prepareStatement(sql)) {
            for (int i = 0; i < BENCHMARK_TOTAL_ROWS; i++) {
                bindBenchmarkEmployee(statement, i);
                statement.executeUpdate();
            }
        }
        return System.nanoTime() - start;
    }

    private long benchmarkCommitPerHundred() throws SQLException {
        long start = System.nanoTime();
        String sql = "INSERT INTO employees_benchmark (name, department_id, salary) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            try {
                for (int i = 0; i < BENCHMARK_TOTAL_ROWS; i++) {
                    bindBenchmarkEmployee(statement, i);
                    statement.executeUpdate();
                    if ((i + 1) % BENCHMARK_COMMIT_BATCH_SIZE == 0) {
                        conn.commit();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
        return System.nanoTime() - start;
    }

    private long benchmarkSingleTransactionBatch() throws SQLException {
        long start = System.nanoTime();
        String sql = "INSERT INTO employees_benchmark (name, department_id, salary) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            try {
                for (int i = 0; i < BENCHMARK_TOTAL_ROWS; i++) {
                    bindBenchmarkEmployee(statement, i);
                    statement.addBatch();
                    if ((i + 1) % BENCHMARK_EXECUTE_BATCH_SIZE == 0) {
                        statement.executeBatch();
                    }
                }
                statement.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
        return System.nanoTime() - start;
    }

    private void bindBenchmarkEmployee(PreparedStatement statement, int index) throws SQLException {
        statement.setString(1, "BenchmarkEmployee" + index);
        statement.setInt(2, 10 + (index % 5));
        statement.setBigDecimal(3, BigDecimal.valueOf(3000 + (index % 1200)));
    }

    private void resetBenchmarkTable() throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement statement = conn.createStatement()) {
            statement.execute("TRUNCATE TABLE employees_benchmark RESTART IDENTITY");
        }
    }

    private int countBenchmarkRows() throws SQLException {
        String sql = "SELECT COUNT(*) FROM employees_benchmark";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private String formatBenchmarkLine(String label, long elapsedNanos, int rowsInserted) {
        double elapsedMs = elapsedNanos / 1_000_000.0;
        double rowsPerSecond = elapsedNanos == 0 ? 0 : (rowsInserted * 1_000_000_000.0) / elapsedNanos;
        return String.format("%s -> rows=%d, timp=%.2f ms, throughput=%.2f rows/s", label, rowsInserted, elapsedMs, rowsPerSecond);
    }

    private String benchmarkWinner(long autoCommitNanos, long commitPerHundredNanos, long oneTransactionBatchNanos, long fastest) {
        if (fastest == autoCommitNanos) {
            return "Abordarea 1 (auto-commit)";
        }
        if (fastest == commitPerHundredNanos) {
            return "Abordarea 2 (commit per 100)";
        }
        if (fastest == oneTransactionBatchNanos) {
            return "Abordarea 3 (tranzactie unica + batch)";
        }
        return "egalitate";
    }

    private void executeScript(String resourcePath) throws SQLException {
        String script = readResource(resourcePath);
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement statement = conn.createStatement()) {
            for (String chunk : script.split(";")) {
                String sql = chunk.trim();
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
        }
    }

    private String readResource(String resourcePath) {
        InputStream input = TransactionLabService.class.getClassLoader().getResourceAsStream(resourcePath);
        if (input == null && resourcePath.startsWith("/")) {
            input = TransactionLabService.class.getClassLoader().getResourceAsStream(resourcePath.substring(1));
        }
        if (input == null && !resourcePath.startsWith("/")) {
            input = TransactionLabService.class.getClassLoader().getResourceAsStream("/" + resourcePath);
        }
        if (input == null) {
            return fallbackSql(resourcePath);
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            throw new IllegalStateException("Nu pot citi resource: " + resourcePath, e);
        }
        return sb.toString();
    }

    private String fallbackSql(String resourcePath) {
        if (resourcePath.endsWith("01_create_transaction_lab.sql")) {
            return CREATE_LAB_SQL;
        }
        if (resourcePath.endsWith("02_reset_transaction_lab.sql")) {
            return RESET_LAB_SQL;
        }
        throw new IllegalStateException("Nu gasesc resource: " + resourcePath);
    }

    private Connection openConnection(int isolationLevel) throws SQLException {
        Connection conn = DriverManager.getConnection(url, username, password);
        conn.setAutoCommit(false);
        conn.setTransactionIsolation(isolationLevel);
        return conn;
    }

    private void updateSalary(Connection connection, int employeeId, BigDecimal salary) throws SQLException {
        String sql = "UPDATE employees SET salary = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBigDecimal(1, salary);
            ps.setInt(2, employeeId);
            ps.executeUpdate();
        }
    }

    private BigDecimal readSalary(Connection connection, int employeeId) throws SQLException {
        String sql = "SELECT salary FROM employees WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Employee id=" + employeeId + " inexistent");
                }
                return rs.getBigDecimal(1);
            }
        }
    }

    private int countDepartment(Connection connection, int departmentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM employees WHERE department_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int nextEmployeeId(Connection connection) throws SQLException {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 FROM employees";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private void insertEmployee(Connection connection, int id, String name, int departmentId, BigDecimal salary) throws SQLException {
        String sql = "INSERT INTO employees (id, name, department_id, salary) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setInt(3, departmentId);
            ps.setBigDecimal(4, salary);
            ps.executeUpdate();
        }
    }

    public void logFinalState(Consumer<String> log) {
        String sql = "SELECT id, name, department_id, salary FROM employees ORDER BY id";
        log.accept("[FINAL] Starea tabelei employees:");
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                log.accept("  id=" + rs.getInt("id") +
                        ", name=" + rs.getString("name") +
                        ", dep=" + rs.getInt("department_id") +
                        ", salary=" + rs.getBigDecimal("salary"));
            }
        } catch (SQLException e) {
            log.accept("[FINAL] ERROR: " + e.getMessage());
        }
    }

    private void rollbackSilently(SQLException e) {
        SQLException current = e;
        while (current != null) {
            if ("25P02".equals(current.getSQLState())) {
                break;
            }
            current = current.getNextException();
        }
    }

    private String modeName(boolean restrictive) {
        return restrictive ? "RESTRICTIVE" : "PERMISSIVE";
    }

    private String isolationName(int isolation) {
        return switch (isolation) {
            case Connection.TRANSACTION_READ_UNCOMMITTED -> "READ UNCOMMITTED";
            case Connection.TRANSACTION_READ_COMMITTED -> "READ COMMITTED";
            case Connection.TRANSACTION_REPEATABLE_READ -> "REPEATABLE READ";
            case Connection.TRANSACTION_SERIALIZABLE -> "SERIALIZABLE";
            default -> "UNKNOWN";
        };
    }

    private static void sleepMillis(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void await(CountDownLatch latch) throws InterruptedException {
        latch.await();
    }

    private String extractDatabaseName(String jdbcUrl) {
        int slash = jdbcUrl.lastIndexOf('/');
        if (slash < 0 || slash == jdbcUrl.length() - 1) {
            throw new IllegalStateException("URL JDBC PostgreSQL invalid: " + jdbcUrl);
        }

        String tail = jdbcUrl.substring(slash + 1);
        int queryPos = tail.indexOf('?');
        String dbName = queryPos >= 0 ? tail.substring(0, queryPos) : tail;

        if (!dbName.matches("[A-Za-z0-9_]+")) {
            throw new IllegalStateException("Numele bazei nu poate fi folosit pentru auto-create: " + dbName);
        }
        return dbName;
    }

    private String replaceDatabaseName(String jdbcUrl, String newDatabaseName) {
        int slash = jdbcUrl.lastIndexOf('/');
        if (slash < 0) {
            throw new IllegalStateException("URL JDBC PostgreSQL invalid: " + jdbcUrl);
        }

        String prefix = jdbcUrl.substring(0, slash + 1);
        String tail = jdbcUrl.substring(slash + 1);
        int queryPos = tail.indexOf('?');
        if (queryPos >= 0) {
            return prefix + newDatabaseName + tail.substring(queryPos);
        }
        return prefix + newDatabaseName;
    }
}

