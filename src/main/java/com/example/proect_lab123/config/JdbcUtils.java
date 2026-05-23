package com.example.proect_lab123.config;

import org.hibernate.SessionFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class JdbcUtils {
    private Properties props;
    private Connection instance = null;
    private SessionFactory sessionFactory;

    public JdbcUtils(Properties props) {
        this.props = props;
        this.sessionFactory = HibernateOrmConfig.createSessionFactory(props);
    }

    public Connection getConnection() {
        try {
            if (instance == null || instance.isClosed()) {
                instance = getNewConnection();
            }
        } catch (SQLException e) {
            System.out.println("Error DB " + e);
        }
        return instance;
    }

    private Connection getNewConnection() {

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.username");
        String pass = props.getProperty("db.password");

        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            // Activez foreign keys pentru SQLite
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            return conn;
        } catch (SQLException e) {
            System.out.println("Error getting connection " + e);
            return null;
        }
    }

    public void closeConnection() {
        if (instance != null) {
            try {
                if (!instance.isClosed()) {
                    instance.close();
                    System.out.println("Database connection closed successfully");
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e);
            } finally {
                instance = null;
            }
        }
        
        // Close SessionFactory
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }

    public SessionFactory getEntityManagerFactory() {
        return sessionFactory;
    }

    public Properties getProperties() {
        return props;
    }
}

