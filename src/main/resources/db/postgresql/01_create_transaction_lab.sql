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
);

