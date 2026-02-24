# MySQL Setup Guide for GWS Microservices

## 📋 Overview
This guide provides step-by-step instructions to set up MySQL databases for both User-Service and Notification-Service.

---

## ✅ Prerequisites

- MySQL 8.0 or higher installed
- MySQL Client or MySQL Workbench
- Java 17+
- Maven 3.6+

---

## 🔧 Installation Steps

### Step 1: Install MySQL (if not already installed)

#### Windows:
```bash
# Download from: https://dev.mysql.com/downloads/mysql/
# Or use Chocolatey:
choco install mysql
```

#### Mac:
```bash
brew install mysql
brew services start mysql
```

#### Linux (Ubuntu/Debian):
```bash
sudo apt-get update
sudo apt-get install mysql-server
sudo mysql_secure_installation
```

---

## 📦 Step 2: Create Databases and Users

Connect to MySQL with root user:

```bash
mysql -u root -p
```

Then execute the following SQL commands:

```sql
-- Create User-Service Database
CREATE DATABASE user_service;

-- Create Notification-Service Database
CREATE DATABASE notification_service;

-- Create Application User with necessary permissions
CREATE USER 'gwsapp'@'localhost' IDENTIFIED BY 'GWSApp@123';

-- Grant privileges
GRANT ALL PRIVILEGES ON user_service.* TO 'gwsapp'@'localhost';
GRANT ALL PRIVILEGES ON notification_service.* TO 'gwsapp'@'localhost';

-- Apply changes
FLUSH PRIVILEGES;

-- Verify
SHOW DATABASES;
SHOW GRANTS FOR 'gwsapp'@'localhost';
```

---

## 🔐 Step 3: Configure Environment Variables (Optional)

Instead of hardcoding credentials, use environment variables:

### Windows (PowerShell):
```powershell
$env:DB_USERNAME='gwsapp'
$env:DB_PASSWORD='GWSApp@123'
```

### Windows (Command Prompt):
```cmd
setx DB_USERNAME gwsapp
setx DB_PASSWORD GWSApp@123
```

### Linux/Mac:
```bash
export DB_USERNAME=gwsapp
export DB_PASSWORD=GWSApp@123
```

### Or add to `.env` file in project root:
```env
DB_USERNAME=gwsapp
DB_PASSWORD=GWSApp@123
```

---

## 🚀 Step 4: Build and Run Services

### Build User-Service:
```bash
cd D:\Dev\GWS\User-Service
mvn clean install
mvn spring-boot:run
```

### Build Notification-Service:
```bash
cd D:\Dev\GWS\Notification-Service
mvn clean install
mvn spring-boot:run
```

---

## 🔍 Step 5: Verify Database Creation

The services will automatically create tables on first run due to `spring.jpa.hibernate.ddl-auto=update`.

```bash
mysql -u gwsapp -p notification_service
SHOW TABLES;
```

```bash
mysql -u gwsapp -p user_service
SHOW TABLES;
```

---

## 📊 Database Configuration Details

### User-Service (`application.properties`)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/user_service?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

### Notification-Service (`application.yaml`)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/notification_service?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}
  jpa:
    database-platform: org.hibernate.dialect.MySQL8Dialect
```

---

## 🛠️ Troubleshooting

### Issue 1: "Connection refused"
**Solution:** Ensure MySQL is running
```bash
# Check MySQL status
mysql -u root -p -e "SELECT 1"
```

### Issue 2: "Access denied for user"
**Solution:** Verify credentials in `application.properties` or environment variables
```bash
mysql -u gwsapp -p -e "SELECT 1"
```

### Issue 3: "Unknown database"
**Solution:** Run the SQL setup commands again (Step 2)

### Issue 4: "PublicKeyRetrieval not allowed"
**Solution:** The connection string already includes `allowPublicKeyRetrieval=true`

---

## 🔄 Schema Management

### To Reset Databases (Caution: Deletes all data):
```sql
DROP DATABASE user_service;
DROP DATABASE notification_service;

CREATE DATABASE user_service;
CREATE DATABASE notification_service;

GRANT ALL PRIVILEGES ON user_service.* TO 'gwsapp'@'localhost';
GRANT ALL PRIVILEGES ON notification_service.* TO 'gwsapp'@'localhost';
FLUSH PRIVILEGES;
```

---

## 📝 Default Connection Details

| Property | User-Service | Notification-Service |
|----------|-------------|----------------------|
| **Database** | user_service | notification_service |
| **Host** | localhost | localhost |
| **Port** | 3306 | 3306 |
| **Username** | gwsapp (env: DB_USERNAME) | gwsapp (env: DB_USERNAME) |
| **Password** | GWSApp@123 (env: DB_PASSWORD) | GWSApp@123 (env: DB_PASSWORD) |

---

## ✨ Features Enabled

- ✅ MySQL 8.0 Dialect support
- ✅ Automatic table creation (ddl-auto: update)
- ✅ Batch insert optimization
- ✅ SQL formatting and comments
- ✅ Timezone UTC configuration
- ✅ SSL bypass for local development

---

## 📚 Next Steps

1. Create entity classes with `@Entity` and `@Table` annotations
2. Create repositories extending `JpaRepository`
3. Run the services to auto-create schema
4. Implement business logic with JPA

---

**Last Updated:** 2026-02-24
**Version:** 1.0

