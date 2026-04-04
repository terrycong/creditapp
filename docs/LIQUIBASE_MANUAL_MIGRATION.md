# Liquibase Manual Migration Guide

> **Purpose**: This guide explains how to manually run database migrations using Liquibase instead of auto-migration on application startup.

---

## 📋 Overview

Liquibase auto-migration has been **disabled** in the production profile. Database changes must now be applied manually using Maven commands before or after deploying new code.

### Why Manual Migration?

- ✅ **Control**: You decide exactly when migrations run
- ✅ **Safety**: Review changes before applying to production
- ✅ **Debugging**: Easier to troubleshoot migration issues
- ✅ **Deployment Flexibility**: Separate code deployment from database changes

---

## 🔧 Prerequisites

- Maven installed (`mvn --version`)
- Database access (MySQL)
- Application configuration files (`application-prod.properties`)

---

## 📖 Available Commands

### 1. Check Pending Changes (Dry Run)

See what SQL will be executed **without** applying changes:

```bash
mvn liquibase:updateSQL -Dspring.profiles.active=prod
```

**Output**: SQL script that would be executed

---

### 2. Check Database Status

View which changesets have been applied and which are pending:

```bash
mvn liquibase:status -Dspring.profiles.active=prod
```

**Output**: List of applied and pending changesets

---

### 3. Apply Pending Migrations

Run all pending changesets:

```bash
mvn liquibase:update -Dspring.profiles.active=prod
```

**Output**: Confirmation of applied changesets

---

### 4. Tag Current Database State

Create a restore point for rollback:

```bash
mvn liquibase:tag -Dliquibase.tag=v1.0.0 -Dspring.profiles.active=prod
```

**Replace** `v1.0.0` with your version tag

---

### 5. Rollback to a Tag

Revert database to a previous state:

```bash
# Rollback to a specific tag
mvn liquibase:rollback -Dliquibase.tag=v1.0.0 -Dspring.profiles.active=prod

# Rollback by count (number of changesets)
mvn liquibase:rollback -Dliquibase.count=3 -Dspring.profiles.active=prod

# Rollback to a specific date
mvn liquibase:rollback -Dliquibase.date=2026-04-04 -Dspring.profiles.active=prod
```

---

### 6. Generate Changelog from Existing Database

Create a changeset from current database state:

```bash
mvn liquibase:generateChangeLog -Dspring.profiles.active=prod
```

**Output**: `src/main/resources/db/changelog/generated-changelog.yaml`

---

### 7. View Database Documentation

Generate documentation about current database state:

```bash
mvn liquibase:dbDoc -Dliquibase.dbDoc=docs/db-docs -Dspring.profiles.active=prod
```

**Output**: HTML documentation in `docs/db-docs/`

---

## 🚀 Standard Deployment Workflow

### Step 1: Deploy New Code

```bash
git pull origin recover-branch
mvn clean package -DskipTests
```

### Step 2: Review Pending Migrations

```bash
# Check what will be applied
mvn liquibase:updateSQL -Dspring.profiles.active=prod
```

### Step 3: Apply Migrations

```bash
# Run pending changesets
mvn liquibase:update -Dspring.profiles.active=prod
```

### Step 4: Restart Application

```bash
# Stop existing app
# (depends on your deployment method)

# Start application
mvn spring-boot:run -Dspring.profiles.active=prod

# Or with JAR
java -jar target/credit-app-1.0.0.jar --spring.profiles.active=prod
```

### Step 5: Verify

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Or test the new feature manually
```

---

## 📁 Migration File Location

All Liquibase changesets are located in:

```
src/main/resources/db/changelog/changes/
```

Files are named with sequential numbers:
- `001-initial-schema.yaml`
- `002-initial-data.yaml`
- `018-add-spring-autumn-trip-reward.yaml`
- etc.

The master changelog file includes all changesets:
```
src/main/resources/db/changelog/db.changelog-master.yaml
```

---

## ⚠️ Important Notes

### Before Running Migrations

1. **Backup database** (especially in production):
   ```bash
   mysqldump -u root -p creditapp > backup_$(date +%Y%m%d_%H%M%S).sql
   ```

2. **Review changes** with `updateSQL` command

3. **Test in development** environment first

### After Running Migrations

1. **Verify** the application works correctly
2. **Test** new features added by the migration
3. **Monitor** application logs for errors

### Common Issues

| Issue | Solution |
|-------|----------|
| `LockException` | Run `mvn liquibase:releaseLocks -Dspring.profiles.active=prod` |
| `ValidationFailedException` | Check changeset syntax and checksums |
| `MigrationFailedException` | Review error message, rollback if needed |
| Connection timeout | Check database credentials in `application-prod.properties` |

---

## 🔍 Configuration

Liquibase settings in `application-prod.properties`:

```properties
# Liquibase Configuration
# Disabled by default - run migrations manually with: mvn liquibase:update -Dspring.profiles.active=prod
spring.liquibase.enabled=false
spring.liquibase.change-log=classpath:db/changelog/db.changelog-mysql.yaml
```

To **re-enable auto-migration** (not recommended for production):

```properties
spring.liquibase.enabled=true
```

---

## 📚 Additional Resources

- [Liquibase Maven Plugin Documentation](https://docs.liquibase.com/tools-integrations/maven/home.html)
- [Liquibase Commands Reference](https://docs.liquibase.com/commands/home.html)
- [Liquibase Changeset Formats](https://docs.liquibase.com/concepts/changelists/changelog-formats.html)

---

## 🆘 Quick Reference

```bash
# Most common commands:
mvn liquibase:status                    # Check pending changes
mvn liquibase:updateSQL                 # Preview SQL (dry run)
mvn liquibase:update                    # Apply migrations
mvn liquibase:releaseLocks              # Clear stuck locks
mvn liquibase:rollback -Dliquibase.tag=v1.0.0  # Rollback to tag
```

---

*Last updated: 2026-04-04*
