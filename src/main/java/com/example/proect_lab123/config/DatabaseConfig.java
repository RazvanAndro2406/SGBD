package com.example.proect_lab123.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    private static final Logger logger = LogManager.getLogger(JdbcUtils.class);
    private static  Properties props=null;
    private static HikariDataSource dataSource = null;

    public DatabaseConfig(Properties props) {
        this.props = props;
    }

    /**
     * Inițializează și returnează sursa de date (Pool-ul).
     * Dacă pool-ul nu există, îl creează folosind proprietățile primite.
     */
    public static HikariDataSource getDataSource(Properties props) {
        if (dataSource == null) {
            try {
                HikariConfig config = new HikariConfig();

                // Acum folosim props primit prin parametru, deci nu mai poate fi null
                config.setJdbcUrl(props.getProperty("db.url"));
                config.setUsername(props.getProperty("db.username"));
                config.setPassword(props.getProperty("db.password"));

                config.setMaximumPoolSize(10);
                config.setMinimumIdle(5);
                config.setConnectionTimeout(5000);
                config.setConnectionInitSql("PRAGMA foreign_keys = ON;");

                dataSource = new HikariDataSource(config);
                logger.info("HikariCP Data Source initialized successfully");
            } catch (Exception e) {
                logger.error("Error creating HikariCP Data Source", e);
                throw new RuntimeException("Could not initialize database pool", e);
            }
        }
        return dataSource;
    }

    /**
     * Returnează o conexiune din pool.
     * Nu mai păstrăm o instanță unică (Singleton) de conexiune,
     * deoarece pooling-ul se ocupă de managementul lor.
     */
    public Connection getConnection() {
        try {
            return this.dataSource.getConnection();
        } catch (SQLException e) {
            logger.error("Error getting connection from pool", e);
            return null;
        }
    }

    /**
     * Închide întregul Pool de conexiuni la oprirea aplicației.
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("HikariCP Data Source closed");
        }
    }
}