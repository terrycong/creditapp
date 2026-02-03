# Deployment Guide - Family Credit Points Application

## Overview
This guide provides step-by-step instructions for deploying the Family Credit Points Application with Task Marketplace feature to production.

## Prerequisites

### 1. System Requirements
- **Java**: JDK 21 or higher
- **Database**: MySQL 8.0 or higher (for production)
- **Memory**: Minimum 2GB RAM
- **Storage**: 1GB free disk space
- **Operating System**: Linux, Windows, or macOS

### 2. Software Dependencies
- **MySQL Server**: For production database
- **Java Runtime Environment**: JDK 21+
- **Git**: For version control (optional)
- **Nginx/Apache**: For reverse proxy (optional)

## Deployment Steps

### Step 1: Prepare Production Environment

#### 1.1 Install MySQL Database
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install mysql-server

# CentOS/RHEL
sudo yum install mysql-server

# macOS (Homebrew)
brew install mysql

# Windows
# Download from https://dev.mysql.com/downloads/installer/
```

#### 1.2 Configure MySQL
```sql
-- Create database and user
CREATE DATABASE creditapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'creditapp_user'@'localhost' IDENTIFIED BY 'your_secure_password_here';
GRANT ALL PRIVILEGES ON creditapp.* TO 'creditapp_user'@'localhost';
FLUSH PRIVILEGES;
```

#### 1.3 Install Java
```bash
# Ubuntu/Debian
sudo apt install openjdk-21-jdk

# CentOS/RHEL
sudo yum install java-21-openjdk

# macOS (Homebrew)
brew install openjdk@21

# Windows
# Download from https://adoptium.net/
```

### Step 2: Configure Application

#### 2.1 Update Production Configuration
Edit `src/main/resources/application-prod.properties`:

```properties
# Update database credentials
spring.datasource.username=creditapp_user
spring.datasource.password=your_secure_password_here

# Update database URL if using remote MySQL
spring.datasource.url=jdbc:mysql://your-database-host:3306/creditapp?useSSL=false&serverTimezone=UTC

# Update application URL for CSRF
# spring.web.cors.allowed-origins=https://your-domain.com
```

#### 2.2 Build Production JAR
```bash
# Clean and build with production profile
mvn clean package -DskipTests -Pprod

# Or build with specific profile
mvn clean package -DskipTests -Dspring.profiles.active=prod
```

### Step 3: Deploy Application

#### 3.1 Copy Files to Server
```bash
# Copy JAR file
scp target/credit-app-1.0.0.jar user@your-server:/opt/creditapp/

# Copy configuration (optional)
scp src/main/resources/application-prod.properties user@your-server:/opt/creditapp/config/
```

#### 3.2 Create Systemd Service (Linux)
Create `/etc/systemd/system/creditapp.service`:

```ini
[Unit]
Description=Family Credit Points Application
After=network.target mysql.service

[Service]
User=creditapp
Group=creditapp
WorkingDirectory=/opt/creditapp
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod credit-app-1.0.0.jar
SuccessExitStatus=143
Restart=always
RestartSec=10
Environment="JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"
Environment="SPRING_PROFILES_ACTIVE=prod"

[Install]
WantedBy=multi-user.target
```

#### 3.3 Start Application
```bash
# Linux with systemd
sudo systemctl daemon-reload
sudo systemctl enable creditapp
sudo systemctl start creditapp
sudo systemctl status creditapp

# Manual start
java -jar -Dspring.profiles.active=prod credit-app-1.0.0.jar

# With custom configuration
java -jar -Dspring.config.location=file:/opt/creditapp/config/application-prod.properties credit-app-1.0.0.jar
```

### Step 4: Configure Web Server (Optional)

#### 4.1 Nginx Configuration
Create `/etc/nginx/sites-available/creditapp`:

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # Redirect HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    # SSL certificates
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    
    # Security headers
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    
    # Proxy to Spring Boot
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket support
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
    
    # Static files
    location /static/ {
        alias /opt/creditapp/static/;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

#### 4.2 Enable Site
```bash
sudo ln -s /etc/nginx/sites-available/creditapp /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### Step 5: SSL Certificate (Optional)

#### 5.1 Let's Encrypt with Certbot
```bash
# Install Certbot
sudo apt install certbot python3-certbot-nginx

# Obtain certificate
sudo certbot --nginx -d your-domain.com

# Auto-renewal
sudo certbot renew --dry-run
```

## Post-Deployment Verification

### 1. Health Check
```bash
# Check application status
curl http://localhost:8080/actuator/health

# Check database connection
curl http://localhost:8080/actuator/health/db

# Check application info
curl http://localhost:8080/actuator/info
```

### 2. Functional Testing

#### 2.1 Test User Login
1. Open browser: `https://your-domain.com` or `http://localhost:8080`
2. Login as parent: `parent` / `parent123`
3. Login as child: `child` / `child123`

#### 2.2 Test Marketplace Feature
1. Parent creates task with "放入市场" checked
2. Child logs in and goes to "任务市场"
3. Child picks a task
4. Verify task appears in child's "我的任务"
5. Child completes task
6. Parent approves task
7. Verify child receives points

### 3. Performance Testing
```bash
# Load test with Apache Bench
ab -n 1000 -c 10 http://localhost:8080/

# Monitor application logs
tail -f /opt/creditapp/logs/creditapp.log

# Monitor database connections
mysql -u creditapp_user -p -e "SHOW PROCESSLIST;"
```

## Monitoring and Maintenance

### 1. Log Management
```bash
# View application logs
tail -f /opt/creditapp/logs/creditapp.log

# Rotate logs (logrotate configuration)
sudo nano /etc/logrotate.d/creditapp
```

### 2. Database Backup
```bash
# Create backup script
#!/bin/bash
BACKUP_DIR="/backup/creditapp"
DATE=$(date +%Y%m%d_%H%M%S)
mysqldump -u creditapp_user -p creditapp > $BACKUP_DIR/creditapp_$DATE.sql
gzip $BACKUP_DIR/creditapp_$DATE.sql

# Schedule with cron
0 2 * * * /opt/creditapp/scripts/backup.sh
```

### 3. Application Updates
```bash
# Stop application
sudo systemctl stop creditapp

# Backup current version
cp /opt/creditapp/credit-app-1.0.0.jar /opt/creditapp/backup/credit-app-1.0.0.jar.$(date +%Y%m%d)

# Deploy new version
cp new-credit-app-1.0.0.jar /opt/creditapp/

# Start application
sudo systemctl start creditapp

# Verify deployment
sudo systemctl status creditapp
tail -f /opt/creditapp/logs/creditapp.log
```

## Troubleshooting

### Common Issues

#### 1. Database Connection Issues
```bash
# Check MySQL service
sudo systemctl status mysql

# Test database connection
mysql -u creditapp_user -p -e "SELECT 1;"

# Check application logs for errors
grep -i "database" /opt/creditapp/logs/creditapp.log
```

#### 2. Application Won't Start
```bash
# Check Java version
java -version

# Check port availability
netstat -tlnp | grep :8080

# Check application logs
journalctl -u creditapp -f
```

#### 3. CSRF Issues
```properties
# Disable CSRF for testing (not recommended for production)
spring.security.csrf.enabled=false

# Check CSRF token in forms
# Ensure forms include: <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
```

#### 4. Memory Issues
```bash
# Monitor memory usage
top -p $(pgrep -f credit-app)

# Adjust JVM memory settings
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
```

## Security Considerations

### 1. Production Security Checklist
- [ ] Change default passwords (`parent123`, `child123`)
- [ ] Enable HTTPS with valid SSL certificate
- [ ] Enable CSRF protection (`spring.security.csrf.enabled=true`)
- [ ] Set secure cookie flags (`http-only`, `secure`)
- [ ] Configure proper CORS headers
- [ ] Implement rate limiting
- [ ] Regular security updates
- [ ] Database backup schedule

### 2. Password Management
```java
// Update default passwords in import.sql
INSERT INTO users (username, password, role) VALUES 
('parent', '$2a$10$YourBcryptHashHere', 'PARENT'),
('child', '$2a$10$YourBcryptHashHere', 'CHILD');
```

### 3. Network Security
- Configure firewall to allow only necessary ports (80, 443, 22)
- Use VPN for database access
- Implement IP whitelisting for admin access
- Regular security scans

## Support and Maintenance

### 1. Monitoring Tools
- **Application**: Spring Boot Actuator (`/actuator`)
- **Database**: MySQL Workbench, phpMyAdmin
- **Logs**: ELK Stack, Splunk, Graylog
- **Performance**: New Relic, Datadog, Prometheus

### 2. Regular Maintenance Tasks
- Weekly: Check application logs for errors
- Monthly: Update dependencies and security patches
- Quarterly: Database optimization and cleanup
- Annually: Security audit and penetration testing

### 3. Contact Information
- **Developer**: Sisyphus AI Agent
- **Documentation**: `AGENTS.md`, `DEPLOYMENT_CHECKLIST.md`
- **Support**: GitHub Issues, Email Support

## Appendix

### A. Default Accounts
- **Parent**: `parent` / `parent123`
- **Child**: `child` / `child123`

### B. API Endpoints
- **Login**: `POST /login`
- **Dashboard**: `GET /dashboard`
- **Marketplace**: `GET /child/marketplace`
- **Task Creation**: `POST /parent/tasks`
- **Task Approval**: `POST /parent/approvals/{id}/approve`

### C. Database Schema
Key tables:
- `users` - User accounts (parents and children)
- `tasks` - Tasks with marketplace support (`picked_by_child_id`)
- `task_completions` - Task completion records
- `rewards` - Available rewards
- `reward_redemptions` - Reward redemption history

### D. Environment Variables
```bash
# Application configuration
export SPRING_PROFILES_ACTIVE=prod
export JAVA_OPTS="-Xms512m -Xmx1024m"
export DATABASE_URL="jdbc:mysql://localhost:3306/creditapp"
export DATABASE_USERNAME="creditapp_user"
export DATABASE_PASSWORD="your_password"

# Security
export CSRF_ENABLED=true
export SECURE_COOKIES=true
```

---

**Last Updated**: 2026-02-03  
**Version**: 1.0.0  
**Application Version**: credit-app-1.0.0  
**Deployment Status**: ✅ Ready for Production