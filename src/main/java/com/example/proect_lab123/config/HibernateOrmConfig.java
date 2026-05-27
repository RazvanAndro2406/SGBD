package com.example.proect_lab123.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Properties;

/**
 * Hibernate ORM configuration factory using JdbcUtils-style Properties pattern.
 * Creates and manages SessionFactory instances for ORM operations.
 */
public class HibernateOrmConfig {
    private static final Logger logger = LogManager.getLogger(HibernateOrmConfig.class);

    // Păstrăm o referință statică globală pentru a o putea închide corect la final
    private static SessionFactory sessionFactory;

    /**
     * Create or retrieve SessionFactory from Properties object
     */
    public static SessionFactory createSessionFactory(Properties props) {
        // Dacă a fost deja creată, o returnăm pe cea existentă (Singleton pattern)
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            return sessionFactory;
        }

        String databaseUrl = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");

        logger.info("Creating Hibernate SessionFactory from properties");
        logger.debug("Database URL: {}", databaseUrl);

        Configuration hibernateConfig = new Configuration();
        hibernateConfig.setProperty("hibernate.connection.driver_class", getDriver(databaseUrl));
        hibernateConfig.setProperty("hibernate.connection.url", databaseUrl);
        hibernateConfig.setProperty("hibernate.connection.username", username);
        hibernateConfig.setProperty("hibernate.connection.password", password);
        hibernateConfig.setProperty("hibernate.dialect", getDialect(databaseUrl));
        hibernateConfig.setProperty("hibernate.current_session_context_class", "thread");
        hibernateConfig.setProperty("hibernate.show_sql", "true");
        hibernateConfig.setProperty("hibernate.format_sql", "true");
        //hibernateConfig.setProperty("hibernate.hbm2ddl.auto", "validate");
        hibernateConfig.setProperty("hibernate.hbm2ddl.auto", props.getProperty("hibernate.hbm2ddl.auto", "update"));

        //pt lab4 MainBulkUpdate
        hibernateConfig.setProperty("hibernate.jdbc.batch_size", "20");
        hibernateConfig.setProperty("hibernate.jdbc.fetch_size", "50");

        hibernateConfig.setProperty("hibernate.bytecode.provider", "bytebuddy");
        hibernateConfig.setProperty("hibernate.bytecode.use_reflection_optimizer", "false");
        hibernateConfig.setProperty("hibernate.use_reflection_optimizer", "false");

        // Register entity classes with ORM annotations
//        hibernateConfig.addAnnotatedClass(com.example.proect_lab123.domain.Actor.class);
//        hibernateConfig.addAnnotatedClass(com.example.proect_lab123.domain.Movie.class);
//        hibernateConfig.addAnnotatedClass(com.example.proect_lab123.domain.Employee.class);
//        hibernateConfig.addAnnotatedClass(com.example.proect_lab123.domain.Project.class);
//        hibernateConfig.addAnnotatedClass(com.example.proect_lab123.domain.Department.class);
//        hibernateConfig.addAnnotatedClass(com.example.proect_lab123.domain.Employee2.class);

        boolean isPostgres = databaseUrl != null && databaseUrl.contains("postgresql");

        if (isPostgres) {
            // Dacă suntem pe PostgreSQL (Laboratorul de tranzacții), încărcăm DOAR Employee2
            hibernateConfig.addAnnotatedClass(com.example.proect_lab123.domain.Employee2.class);
            logger.info("Mod Tranzactii (Postgres) - Am inregistrat doar entitatea Employee2");
        } else {
            // Dacă suntem pe SQLite (Laboratoarele normale), încărcăm restul claselor
            hibernateConfig.addAnnotatedClass(com.example.proect_lab123.domain.Actor.class);
            hibernateConfig.addAnnotatedClass(com.example.proect_lab123.domain.Movie.class);
            hibernateConfig.addAnnotatedClass(com.example.proect_lab123.domain.Employee.class);
            hibernateConfig.addAnnotatedClass(com.example.proect_lab123.domain.Project.class);
            hibernateConfig.addAnnotatedClass(com.example.proect_lab123.domain.Department.class);
            logger.info("Mod Normal (SQLite) - Am inregistrat entitatile standard");
        }

        logger.info("Registered entities: Actor, Movie, Employee, Project,Department");

        sessionFactory = hibernateConfig.buildSessionFactory();
        logger.info("Hibernate SessionFactory created successfully");
        return sessionFactory;
    }

    public static SessionFactory getSessionFactory(Properties props) {
        return createSessionFactory(props);
    }

    /**
     * Metodă adăugată pentru a elibera conexiunile și resursele în siguranță la finalul rulării programului
     */
    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            logger.info("Închidem Hibernate SessionFactory și eliberăm conexiunile bazei de date...");
            try {
                sessionFactory.close();
                logger.info("Hibernate SessionFactory a fost închis cu succes.");
            } catch (Exception e) {
                logger.error("Eroare la închiderea SessionFactory-ului Hibernate", e);
            }
        }
    }

    /**
     * Determine JDBC driver class based on URL
     */
    private static String getDriver(String url) {
        if (url.contains("postgresql")) {
            return "org.postgresql.Driver";
        } else if (url.contains("sqlite")) {
            return "org.sqlite.JDBC";
        }
        throw new IllegalArgumentException("Unsupported database URL: " + url);
    }

    /**
     * Determine Hibernate dialect based on URL
     */
    private static String getDialect(String url) {
        if (url.contains("postgresql")) {
            return "org.hibernate.dialect.PostgreSQLDialect";
        } else if (url.contains("sqlite")) {
            return "org.hibernate.community.dialect.SQLiteDialect";
        }
        throw new IllegalArgumentException("Unsupported database URL: " + url);
    }
}