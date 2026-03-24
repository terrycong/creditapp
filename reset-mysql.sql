-- Reset creditapp database for fresh Liquibase migration
DROP DATABASE IF EXISTS creditapp;
CREATE DATABASE creditapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON creditapp.* TO 'root'@'%';
FLUSH PRIVILEGES;
