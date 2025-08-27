-- SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
-- SPDX-License-Identifier: EUPL-1.2

-- Create database user if not exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = 'wallet_gateway') THEN
        CREATE USER wallet_gateway WITH ENCRYPTED PASSWORD 'postgres';
    END IF;
END
$$;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE wallet_gateway TO wallet_gateway;
GRANT ALL PRIVILEGES ON SCHEMA public TO wallet_gateway;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Set timezone
SET timezone = 'UTC';

-- Initial schema will be managed by Flyway/Liquibase migrations
-- This file only sets up the database user and basic configuration