-- MySQL Initialization Script for GWS Microservices
-- This script is automatically executed when the MySQL container starts

-- Create User-Service Database
CREATE DATABASE IF NOT EXISTS user_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create Notification-Service Database
CREATE DATABASE IF NOT EXISTS notification_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create application user
CREATE USER IF NOT EXISTS 'gwsapp'@'%' IDENTIFIED BY 'GWSApp@123';

-- Grant privileges to user_service database
GRANT ALL PRIVILEGES ON user_service.* TO 'gwsapp'@'%';

-- Grant privileges to notification_service database
GRANT ALL PRIVILEGES ON notification_service.* TO 'gwsapp'@'%';

-- Grant privileges to localhost as well
GRANT ALL PRIVILEGES ON user_service.* TO 'gwsapp'@'localhost';
GRANT ALL PRIVILEGES ON notification_service.* TO 'gwsapp'@'localhost';

-- Apply privileges
FLUSH PRIVILEGES;

-- Use UTF8MB4 for all connections
ALTER DATABASE user_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER DATABASE notification_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

