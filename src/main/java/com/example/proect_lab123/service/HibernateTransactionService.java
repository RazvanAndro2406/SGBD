package com.example.proect_lab123.service;

import com.example.proect_lab123.domain.Employee2;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class HibernateTransactionService {

    private final SessionFactory sessionFactory;

    public HibernateTransactionService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void resetData(Consumer<String> log) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            // Ștergem datele vechi folosind un query HQL (pe entitate, nu pe tabela SQL!)
            session.createMutationQuery("DELETE FROM Employee2").executeUpdate();

            // Inserăm prin obiecte ORM
            session.persist(new Employee2(1L, "Angajat 1", 5, new BigDecimal("5000")));
            session.persist(new Employee2(2L, "Angajat 2", 5, new BigDecimal("5200")));
            session.persist(new Employee2(3L, "Angajat 3", 3, new BigDecimal("4600")));

            session.getTransaction().commit();
            log.accept("[SETUP] Baza de date a fost resetata cu succes prin Hibernate.");
        } catch (Exception e) {
            log.accept("[SETUP ERROR] " + e.getMessage());
        }
    }

    public void runPhantomReadDemo(boolean restrictive, Consumer<String> log) throws InterruptedException {
        log.accept("\n=== PHANTOM READ DEMO (" + (restrictive ? "SERIALIZABLE" : "READ COMMITTED") + ") ===");
        CountDownLatch firstCountDone = new CountDownLatch(1);
        CountDownLatch txBCommitted = new CountDownLatch(1);

        Thread txA = new Thread(() -> {
            try (Session session = sessionFactory.openSession()) {
                session.beginTransaction();
                session.doWork(conn -> {
                    conn.setTransactionIsolation(restrictive ? Connection.TRANSACTION_SERIALIZABLE : Connection.TRANSACTION_READ_COMMITTED);

                    int c1 = countDep(conn, 5);
                    log.accept("TxA (Citirea 1): Numar angajati dep 5 -> " + c1);
                    firstCountDone.countDown();

                    try { txBCommitted.await(); } catch (InterruptedException ignored) {}

                    int c2 = countDep(conn, 5);
                    log.accept("TxA (Citirea 2): Numar angajati dep 5 -> " + c2);
                    if (!restrictive && c2 > c1) log.accept("-> S-a produs Phantom Read (A aparut o fantoma!)");
                    if (restrictive && c2 == c1) log.accept("-> Phantom Read prevenit de modul restrictiv!");
                });
                session.getTransaction().commit();
            } catch (Exception e) { log.accept("TxA Error: " + e.getMessage()); }
        });

        Thread txB = new Thread(() -> {
            try {
                firstCountDone.await();
                try (Session session = sessionFactory.openSession()) {
                    session.beginTransaction();
                    // Inseram fantoma folosind Hibernate persist
                    Employee2 fantoma = new Employee2(99L, "Angajat Fantoma", 5, new BigDecimal("4000"));
                    session.persist(fantoma);
                    session.getTransaction().commit();
                    log.accept("TxB: A inserat o inregistrare noua si a dat COMMIT.");
                }
            } catch (Exception e) { log.accept("TxB Error: " + e.getMessage()); }
            finally { txBCommitted.countDown(); }
        });

        txA.start(); txB.start();
        txA.join(); txB.join();
    }

    public void runDirtyReadDemo(boolean restrictive, Consumer<String> log) throws InterruptedException {
        log.accept("\n=== DIRTY READ DEMO (" + (restrictive ? "READ COMMITTED" : "READ UNCOMMITTED") + ") ===");
        CountDownLatch txAUpdated = new CountDownLatch(1);
        CountDownLatch txBRead = new CountDownLatch(1);

        Thread txA = new Thread(() -> {
            try (Session session = sessionFactory.openSession()) {
                session.beginTransaction();

                // Modificam entitatea, dar NU dam commit (raman date "murdare")
                Employee2 emp = session.get(Employee2.class, 1L);
                emp.setSalary(new BigDecimal("99999"));
                session.merge(emp);
                session.flush(); // Trimite modificarile la baza de date, dar fara commit

                log.accept("TxA: A setat salariul la 99999 (NECOMIS).");
                txAUpdated.countDown();

                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

                session.getTransaction().rollback();
                log.accept("TxA: A dat ROLLBACK (Modificarea s-a sters).");
            }
        });

        Thread txB = new Thread(() -> {
            try {
                txAUpdated.await();
                try (Session session = sessionFactory.openSession()) {
                    session.beginTransaction();
                    session.doWork(conn -> {
                        int iso = restrictive ? Connection.TRANSACTION_READ_COMMITTED : Connection.TRANSACTION_READ_UNCOMMITTED;
                        conn.setTransactionIsolation(iso);

                        log.accept("TxB: Incearca sa citeasca salariul...");
                        BigDecimal readSalary = null;
                        readSalary = readSalaryDirect(conn, 1L);

                        log.accept("TxB: Salariul citit este -> " + readSalary);

                        if (!restrictive) {
                            log.accept("OBS: Desi suntem pe READ UNCOMMITTED, Postgres nu permite Dirty Read.");
                            log.accept("     Deci TxB fie e blocata pana TxA da rollback, fie citeste valoarea originala (5000).");
                        }
                    });
                    session.getTransaction().commit();
                }
            } catch (Exception e) { log.accept("TxB Error: " + e.getMessage()); }
            finally { txBRead.countDown(); }
        });

        txA.start(); txB.start();
        txA.join(); txB.join();
    }

    // --- Helper methods for direct JDBC reading inside doWork ---

    private int countDep(Connection conn, int depId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM employees2 WHERE department_id = ?")) {
            ps.setInt(1, depId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private BigDecimal readSalaryDirect(Connection conn, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT salary FROM employees2 WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }
}