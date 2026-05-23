--liquibase formatted sql
--changeset mihai:003-add-soft-delete-columns

-- Add soft delete columns to employees table
ALTER TABLE IF EXISTS employees
    ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(100);

-- Add soft delete columns to actors table
ALTER TABLE IF EXISTS actors
    ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(100);

-- Add soft delete columns to movies table
ALTER TABLE IF EXISTS movies
    ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(100);

--rollback ALTER TABLE employees DROP COLUMN IF EXISTS is_deleted, DROP COLUMN IF EXISTS deleted_at, DROP COLUMN IF EXISTS deleted_by;
--rollback ALTER TABLE actors DROP COLUMN IF EXISTS is_deleted, DROP COLUMN IF EXISTS deleted_at, DROP COLUMN IF EXISTS deleted_by;
--rollback ALTER TABLE movies DROP COLUMN IF EXISTS is_deleted, DROP COLUMN IF EXISTS deleted_at, DROP COLUMN IF EXISTS deleted_by;

