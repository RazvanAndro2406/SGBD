--liquibase formatted sql
--changeset mihai:006-add-indexes

-- Performance indexes on employees table
CREATE INDEX IF NOT EXISTS idx_employees_department ON employees(department_id);
CREATE INDEX IF NOT EXISTS idx_employees_email ON employees(email);
CREATE INDEX IF NOT EXISTS idx_employees_deleted ON employees(is_deleted);

-- Performance indexes on actors table
CREATE INDEX IF NOT EXISTS idx_actors_deleted ON actors(is_deleted);

-- Performance indexes on movies table
CREATE INDEX IF NOT EXISTS idx_movies_deleted ON movies(is_deleted);

-- Performance indexes on projects table
CREATE INDEX IF NOT EXISTS idx_projects_department ON projects(department_id);
CREATE INDEX IF NOT EXISTS idx_projects_deleted ON projects(is_deleted);
CREATE INDEX IF NOT EXISTS idx_projects_active ON projects(is_active);

--rollback DROP INDEX IF EXISTS idx_employees_department;
--rollback DROP INDEX IF EXISTS idx_employees_email;
--rollback DROP INDEX IF EXISTS idx_employees_deleted;
--rollback DROP INDEX IF EXISTS idx_actors_deleted;
--rollback DROP INDEX IF EXISTS idx_movies_deleted;
--rollback DROP INDEX IF EXISTS idx_projects_department;
--rollback DROP INDEX IF EXISTS idx_projects_deleted;
--rollback DROP INDEX IF EXISTS idx_projects_active;

