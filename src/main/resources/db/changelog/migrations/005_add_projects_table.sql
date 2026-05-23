--liquibase formatted sql
--changeset mihai:005-add-projects-table

-- Create projects table
CREATE TABLE IF NOT EXISTS projects (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    department_id INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    version INTEGER DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- Insert sample projects
INSERT INTO projects (name, description, start_date, department_id, created_by, updated_by)
VALUES
    ('Project Alpha', 'Database migration infrastructure', '2024-01-15', 5, 'system', 'system'),
    ('Project Beta', 'Performance optimization', '2024-02-01', 2, 'system', 'system'),
    ('Project Gamma', 'Security audit', '2024-03-01', 3, 'system', 'system')
ON CONFLICT DO NOTHING;

--rollback DROP TABLE IF EXISTS projects CASCADE;

