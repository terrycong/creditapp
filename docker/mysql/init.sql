-- ===================================================================
-- MySQL Database Initialization Script
-- Credit Points Application - Production Environment
-- ===================================================================

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS creditapp 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE creditapp;

-- Create application user
CREATE USER IF NOT EXISTS 'creditapp'@'%' IDENTIFIED BY 'creditapp123';
GRANT ALL PRIVILEGES ON creditapp.* TO 'creditapp'@'%';
FLUSH PRIVILEGES;

-- Note: Tables will be created automatically by JPA (spring.jpa.hibernate.ddl-auto=update)
-- Default data will be initialized by DataInitializer.java
