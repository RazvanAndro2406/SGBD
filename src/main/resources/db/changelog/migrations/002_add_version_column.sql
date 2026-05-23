--liquibase formatted sql
--changeset mihai:002-add-version-column

-- Add version column for optimistic locking to employees table
ALTER TABLE IF EXISTS employees ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;

-- Add version column to actors table
ALTER TABLE IF EXISTS actors ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;

-- Add version column to movies table
ALTER TABLE IF EXISTS movies ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;

--rollback ALTER TABLE employees DROP COLUMN IF EXISTS version;
--rollback ALTER TABLE actors DROP COLUMN IF EXISTS version;
--rollback ALTER TABLE movies DROP COLUMN IF EXISTS version;

