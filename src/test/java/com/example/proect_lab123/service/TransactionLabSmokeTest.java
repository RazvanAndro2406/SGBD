package com.example.proect_lab123.service;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class TransactionLabSmokeTest {

    @Test
    void runsNonRepeatableReadDemoWhenPostgresIsConfigured() throws Exception {
        String url = System.getenv("LAB_PG_URL");
        String user = System.getenv("LAB_PG_USER");
        String pass = System.getenv("LAB_PG_PASSWORD");

        Assumptions.assumeTrue(url != null && !url.isBlank(), "Set LAB_PG_URL ca sa rulezi testul de integrare");

        TransactionLabService service = new TransactionLabService(url, user == null ? "" : user, pass == null ? "" : pass);
        List<String> logs = new ArrayList<>();

        service.setupLab(logs::add);
        service.runNonRepeatableReadDemo(false, logs::add);
        service.runInsertPerformanceDemo(logs::add);

        assertFalse(logs.isEmpty());
    }
}

