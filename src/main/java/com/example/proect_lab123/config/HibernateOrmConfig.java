package com.example.proect_lab123.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Properties;

/**
 * Hibernate ORM configuration factory using JdbcUtils-style Properties pattern.
 * Creates SessionFactory instances for ORM operations.
 */
public class HibernateOrmConfig {
    private static final Logger logger = LogManager.getLogger(HibernateOrmConfig.class);

    /**
     * Create SessionFactory from Properties object (similar to JdbcUtils pattern)
     * Properties should contain:
     * - db.url: JDBC connection URL
     * - db.username: Database username
     * - db.password: Database password
     */
    public static SessionFactory createSessionFactory(Properties props) {
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
        hibernateConfig.setProperty("hibernate.show_sql", "false");
        hibernateConfig.setProperty("hibernate.format_sql", "true");
        hibernateConfig.setProperty("hibernate.hbm2ddl.auto", "validate");
        hibernateConfig.setProperty("hibernate.jdbc.batch_size", "20");
        hibernateConfig.setProperty("hibernate.jdbc.fetch_size", "50");
        hibernateConfig.setProperty("hibernate.bytecode.provider", "bytebuddy");
        hibernateConfig.setProperty("hibernate.bytecode.use_reflection_optimizer", "false");
        hibernateConfig.setProperty("hibernate.use_reflection_optimizer", "false");

        // Register entity classes with ORM annotations
        hibernateConfig.addAnnotatedClass(com.example.proect_lab123.domain.Actor.class);
        hibernateConfig.addAnnotatedClass(com.example.proect_lab123.domain.Movie.class);
        //hibernateConfig.addAnnotatedClass(com.example.proect_lab123.domain.Project.class);
        logger.info("Registered entities: Actor, Movie, Project");

        SessionFactory sessionFactory = hibernateConfig.buildSessionFactory();
        logger.info("Hibernate SessionFactory created successfully");
        return sessionFactory;
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

