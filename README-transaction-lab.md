# PostgreSQL Transaction Lab

Acest modul adauga in GUI un laborator pentru:
- Dirty Read
- Non-Repeatable Read
- Phantom Read
- Lost Update
- Deadlock
- Batch Insert Performance (5000 randuri, 3 abordari)

## Configurare

Editeaza `src/main/resources/config.properties`:

- `lab.db.url=jdbc:postgresql://localhost:5432/sgbd_lab`
- `lab.db.username=postgres`
- `lab.db.password=postgres`

`lab.db.*` este folosit doar de fereastra **Transaction Lab (PostgreSQL)**.

## Rulare aplicatie

```powershell
.\gradlew.bat run
```

Din fereastra principala deschide butonul `Transaction Lab (PostgreSQL)`.

## Workflow recomandat

1. Apasa `Reset Data`.
2. Ruleaza fiecare demo cu `Restrictive mode` debifat (PERMISSIVE).
3. Ruleaza acelasi demo cu `Restrictive mode` bifat (RESTRICTIVE).
4. Compara log-urile si starea finala din `employees`.
5. Ruleaza `Batch Insert (5000)` pentru comparatia de performanta:
   - Abordarea 1: auto-commit (1 tranzactie/inserare)
   - Abordarea 2: commit la fiecare 100 inserari
   - Abordarea 3: tranzactie unica + `executeBatch` la 50 inserari

## Observatie PostgreSQL

In PostgreSQL, `READ UNCOMMITTED` este tratat ca `READ COMMITTED`, deci dirty read clasic nu apare nici in modul permisiv.

## Smoke test optional (integrare)

Seteaza variabile de mediu pentru un Postgres de test:

```powershell
$env:LAB_PG_URL="jdbc:postgresql://localhost:5432/sgbd_lab"
$env:LAB_PG_USER="postgres"
$env:LAB_PG_PASSWORD="postgres"
.\gradlew.bat test --tests "*TransactionLabSmokeTest"
```

