@echo off
REM MySQL Setup Script for GWS Microservices
REM This script sets up MySQL and initializes databases

echo ========================================
echo GWS MySQL Setup Script
echo ========================================
echo.

REM Check if MySQL is installed
where mysql >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] MySQL is not installed or not in PATH
    echo Please install MySQL 8.0 or higher from https://dev.mysql.com/downloads/mysql/
    echo.
    pause
    exit /b 1
)

echo [OK] MySQL found in PATH
echo.

REM Check if MySQL server is running
echo Checking if MySQL server is running...
mysql -u root -e "SELECT 1" >nul 2>nul
if %errorlevel% neq 0 (
    echo [WARNING] MySQL server might not be running
    echo Please start MySQL service and try again
    echo.
    pause
    exit /b 1
)

echo [OK] MySQL server is running
echo.

REM Create databases and users
echo Creating databases and users...
mysql -u root -e "CREATE DATABASE IF NOT EXISTS user_service;"
mysql -u root -e "CREATE DATABASE IF NOT EXISTS notification_service;"
mysql -u root -e "CREATE USER IF NOT EXISTS 'gwsapp'@'localhost' IDENTIFIED BY 'GWSApp@123';"
mysql -u root -e "GRANT ALL PRIVILEGES ON user_service.* TO 'gwsapp'@'localhost';"
mysql -u root -e "GRANT ALL PRIVILEGES ON notification_service.* TO 'gwsapp'@'localhost';"
mysql -u root -e "FLUSH PRIVILEGES;"

if %errorlevel% neq 0 (
    echo [ERROR] Failed to create databases
    pause
    exit /b 1
)

echo [OK] Databases and users created successfully
echo.

REM Verify setup
echo Verifying setup...
mysql -u gwsapp -pGWSApp@123 -e "SHOW DATABASES;" 2>nul | findstr "user_service notification_service" >nul
if %errorlevel% neq 0 (
    echo [ERROR] Verification failed
    pause
    exit /b 1
)

echo [OK] Setup verification successful
echo.

echo ========================================
echo Setup Complete!
echo ========================================
echo.
echo Database Details:
echo   - User-Service DB: user_service
echo   - Notification-Service DB: notification_service
echo   - Username: gwsapp
echo   - Password: GWSApp@123
echo   - Host: localhost
echo   - Port: 3306
echo.
echo Environment Variables (Optional):
echo   set DB_USERNAME=gwsapp
echo   set DB_PASSWORD=GWSApp@123
echo.
echo Next Steps:
echo   1. Build services: mvn clean install
echo   2. Run User-Service: mvn spring-boot:run (from User-Service directory)
echo   3. Run Notification-Service: mvn spring-boot:run (from Notification-Service directory)
echo.
pause

