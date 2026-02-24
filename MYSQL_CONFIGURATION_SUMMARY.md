# MySQL Configuration - Implementation Summary

## 📋 Changes Applied

### ✅ 1. Notification-Service Configuration

**File:** `Notification-Service/pom.xml`
- Added Spring Data JPA dependency
- Added MySQL Connector Java 8.0.33

**File:** `Notification-Service/src/main/resources/application.yaml`
- Added MySQL datasource configuration
- Configured connection to `notification_service` database
- Added JPA/Hibernate configuration with MySQL8Dialect
- Environment variable support for `DB_USERNAME` and `DB_PASSWORD`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/notification_service?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

---

### ✅ 2. User-Service Configuration

**File:** `User-Service/pom.xml`
- Replaced H2 database dependency with MySQL Connector Java 8.0.33
- Already had Spring Data JPA dependency

**File:** `User-Service/src/main/resources/application.properties`
- Replaced H2 configuration with MySQL configuration
- Configured connection to `user_service` database
- Added JPA/Hibernate optimization properties

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/user_service?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update
```

---

## 🗄️ Database Setup

Two databases will be automatically created or manually created:

| Database | Service | Description |
|----------|---------|-------------|
| `user_service` | User-Service | Stores user, enterprise, and hierarchy data |
| `notification_service` | Notification-Service | Stores notification logs and templates |

---

## 🔐 Default Credentials

```
Host: localhost
Port: 3306
Username: gwsapp (use environment variable DB_USERNAME)
Password: GWSApp@123 (use environment variable DB_PASSWORD)
```

---

## 🚀 Quick Start Options

### Option 1: Manual MySQL Setup (Windows)
```batch
# Run the provided script
setup-mysql.bat
```

### Option 2: Docker Compose (Recommended)
```bash
# From GWS root directory
docker-compose up -d

# Access phpMyAdmin at http://localhost:8888
# User: gwsapp / Password: GWSApp@123
```

### Option 3: Manual MySQL Commands
```sql
mysql -u root -p

-- Execute commands from mysql-init/01-init.sql
CREATE DATABASE user_service;
CREATE DATABASE notification_service;
CREATE USER 'gwsapp'@'localhost' IDENTIFIED BY 'GWSApp@123';
GRANT ALL PRIVILEGES ON user_service.* TO 'gwsapp'@'localhost';
GRANT ALL PRIVILEGES ON notification_service.* TO 'gwsapp'@'localhost';
FLUSH PRIVILEGES;
```

---

## 📦 Build and Run

### Build both services:
```bash
# User-Service
cd User-Service
mvn clean install

# Notification-Service
cd Notification-Service
mvn clean install
```

### Run services:

**Terminal 1 - User-Service:**
```bash
cd User-Service
mvn spring-boot:run
# Runs on http://localhost:8081
```

**Terminal 2 - Notification-Service:**
```bash
cd Notification-Service
mvn spring-boot:run
# Runs on http://localhost:8084
```

---

## 🔧 Configuration Properties

### Connection String Format
```
jdbc:mysql://localhost:3306/database_name?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

**Parameters:**
- `useSSL=false` - Disable SSL for local development
- `serverTimezone=UTC` - Set timezone to UTC
- `allowPublicKeyRetrieval=true` - Allow public key retrieval (for authentication)

### Hibernate DDL Options
- `ddl-auto: update` - Automatically create/update tables based on entities
- Alternative: `validate` (production), `create` (fresh), `drop` (reset)

### Performance Optimizations
- `jdbc.batch_size: 20` - Batch insert/update operations
- `order_inserts: true` - Order INSERT statements
- `order_updates: true` - Order UPDATE statements

---

## ✨ Features

✅ **MySQL 8.0 Support** - Latest MySQL dialect  
✅ **Automatic Schema Creation** - Tables auto-created from entities  
✅ **Environment Variables** - Secure credential management  
✅ **Batch Operations** - Optimized insert/update performance  
✅ **UTF-8 Support** - Full UTF-8 character support  
✅ **Connection Pooling** - Efficient connection management  

---

## 📊 Entity Classes

Both services will now support JPA entities stored in MySQL:

### User-Service Entity Example:
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    // ... other fields
}
```

### Notification-Service Entity Example:
```java
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String enterpriseId;
    
    @Enumerated(EnumType.STRING)
    private NotificationEvent event;
    
    // ... other fields
}
```

---

## 🔍 Verification

### Check database creation:
```bash
mysql -u gwsapp -pGWSApp@123 -e "SHOW DATABASES;"
```

### Check tables after running services:
```bash
mysql -u gwsapp -pGWSApp@123 user_service -e "SHOW TABLES;"
mysql -u gwsapp -pGWSApp@123 notification_service -e "SHOW TABLES;"
```

---

## 📁 Files Created/Modified

### Created Files:
- ✅ `MYSQL_SETUP_GUIDE.md` - Detailed setup instructions
- ✅ `docker-compose.yml` - Docker Compose configuration
- ✅ `mysql-init/01-init.sql` - Database initialization script
- ✅ `setup-mysql.bat` - Windows setup script
- ✅ `MYSQL_CONFIGURATION_SUMMARY.md` - This file

### Modified Files:
- ✅ `Notification-Service/pom.xml` - Added MySQL dependencies
- ✅ `Notification-Service/src/main/resources/application.yaml` - Added MySQL config
- ✅ `User-Service/pom.xml` - Replaced H2 with MySQL
- ✅ `User-Service/src/main/resources/application.properties` - Updated to MySQL

---

## ⚠️ Troubleshooting

### Issue: Connection refused
**Solution:** Ensure MySQL is running
```bash
# Windows
net start MySQL80
# or via Services app

# Mac
brew services start mysql
```

### Issue: Access denied
**Solution:** Verify credentials and user exists
```bash
mysql -u gwsapp -pGWSApp@123 -e "SELECT 1;"
```

### Issue: Database not found
**Solution:** Run initialization script
```bash
mysql -u root -p < mysql-init/01-init.sql
```

---

## 📞 Support

For issues:
1. Check MySQL is running: `mysql -u root -p -e "SELECT 1;"`
2. Verify credentials in application configuration
3. Check database exists: `mysql -u gwsapp -pGWSApp@123 -e "SHOW DATABASES;"`
4. Review Spring Boot logs for detailed error messages

---

**Configuration Date:** 2026-02-24  
**Version:** 1.0  
**Status:** ✅ Complete

