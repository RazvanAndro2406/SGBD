package com.example.proect_lab123.config;

import com.example.proect_lab123.domain.Employee2;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.util.Properties;

public class HibernatePostgresConfig {
    private static SessionFactory sessionFactory;

    public static SessionFactory createSessionFactory(Properties props) {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            return sessionFactory;
        }

        Configuration config = new Configuration();

        // Citim URL-ul specific pentru lab-ul de tranzactii
        config.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        config.setProperty("hibernate.connection.url", props.getProperty("lab.db.url"));
        config.setProperty("hibernate.connection.username", props.getProperty("lab.db.username"));
        config.setProperty("hibernate.connection.password", props.getProperty("lab.db.password"));
        config.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        config.setProperty("hibernate.current_session_context_class", "thread");
        config.setProperty("hibernate.show_sql", "true");
        config.setProperty("hibernate.format_sql", "true");

        // Aici punem mereu "update" ca să fim siguri că ne generează tabela Employee2
        config.setProperty("hibernate.hbm2ddl.auto", "update");

        // INREGISTRAM DOAR ENTITATEA PENTRU TRANZACTII
        config.addAnnotatedClass(Employee2.class);

        sessionFactory = config.buildSessionFactory();
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}