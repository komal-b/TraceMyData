
-- schema.sql: Create users table for TraceMyData

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID ,
    first_name VARCHAR(500),
    last_name VARCHAR(500),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT,
    auth_provider VARCHAR(50) DEFAULT 'google' CHECK (auth_provider IN ('local', 'google', 'outlook')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);