--liquibase formatted sql
--changeset mihai:001-initial-schema

-- Create actors table if not exists
CREATE TABLE IF NOT EXISTS actors (
    ida INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL,
    birthday TEXT NOT NULL,
    idm INTEGER
);

-- Create movies table if not exists
CREATE TABLE IF NOT EXISTS movies (
    idm INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    title VARCHAR(255) NOT NULL,
    release_year INTEGER
);

-- Create employees table for transaction lab
CREATE TABLE IF NOT EXISTS employees (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department_id INTEGER NOT NULL,
    salary NUMERIC(12,2) NOT NULL CHECK (salary >= 0),
    email VARCHAR(100),
    UNIQUE(email)
);

-- Create departments table
CREATE TABLE IF NOT EXISTS departments (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    budget NUMERIC(15,2)
);

-- Initial data
INSERT INTO departments (id, name, budget)
VALUES
    (1, 'Sales', 100000),
    (2, 'IT', 150000),
    (3, 'HR', 80000),
    (4, 'Finance', 120000),
    (5, 'Operations', 90000)
ON CONFLICT DO NOTHING;

INSERT INTO employees (id, name, department_id, salary, email)
VALUES
    (1, 'Angajat 1', 5, 5000.00, 'angajat1@company.com'),
    (2, 'Angajat 2', 5, 5200.00, 'angajat2@company.com'),
    (3, 'Angajat 3', 3, 4600.00, 'angajat3@company.com')
ON CONFLICT DO NOTHING;

--rollback DROP TABLE IF EXISTS actors CASCADE;
--rollback DROP TABLE IF EXISTS movies CASCADE;
--rollback DROP TABLE IF EXISTS employees CASCADE;
--rollback DROP TABLE IF EXISTS departments CASCADE;

