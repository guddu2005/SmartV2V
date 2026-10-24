-- SmartV2V PostgreSQL Database Schema
-- Database Name: smartv2v_db

CREATE DATABASE smartv2v_db;
\c smartv2v_db;

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index on email for fast authentication lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- 2. VEHICLES TABLE
CREATE TABLE IF NOT EXISTS vehicles (
    id SERIAL PRIMARY KEY,
    vehicle_number VARCHAR(50) NOT NULL,
    device_id VARCHAR(100) NOT NULL,
    active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

-- Index on user_id for fetching user vehicles efficiently
CREATE INDEX IF NOT EXISTS idx_vehicles_user_id ON vehicles(user_id);

-- 3. VEHICLE TELEMETRY TABLE
CREATE TABLE IF NOT EXISTS vehicle_telemetry (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id INTEGER NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    speed DOUBLE PRECISION NOT NULL,
    heading DOUBLE PRECISION NOT NULL,
    acceleration_x DOUBLE PRECISION NOT NULL,
    acceleration_y DOUBLE PRECISION NOT NULL,
    acceleration_z DOUBLE PRECISION NOT NULL,
    gyroscope_x DOUBLE PRECISION NOT NULL,
    gyroscope_y DOUBLE PRECISION NOT NULL,
    gyroscope_z DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index on vehicle_id and timestamp for rapid telemetry querying
CREATE INDEX IF NOT EXISTS idx_telemetry_vehicle_timestamp ON vehicle_telemetry(vehicle_id, timestamp DESC);

-- 4. COLLISION RISK RESULTS TABLE
CREATE TABLE IF NOT EXISTS collision_risk_results (
    id BIGSERIAL PRIMARY KEY,
    vehicle_a_id INTEGER NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    vehicle_b_id INTEGER NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    distance DOUBLE PRECISION NOT NULL,
    relative_speed DOUBLE PRECISION NOT NULL,
    relative_acceleration DOUBLE PRECISION NOT NULL,
    heading_difference DOUBLE PRECISION NOT NULL,
    acceleration_magnitude DOUBLE PRECISION NOT NULL,
    gyroscope_magnitude DOUBLE PRECISION NOT NULL,
    ttc DOUBLE PRECISION NOT NULL,
    risk_probability DOUBLE PRECISION NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index on vehicle pair and timestamp
CREATE INDEX IF NOT EXISTS idx_risk_vehicles_timestamp ON collision_risk_results(vehicle_a_id, vehicle_b_id, timestamp DESC);
