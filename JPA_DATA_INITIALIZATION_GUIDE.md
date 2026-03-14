# JPA Data Initialization Guide

## Overview

The application now uses **JPA-based data initialization** instead of SQL scripts for better type safety, maintainability, and IDE support.

## ✅ What Changed

### Before (SQL Scripts)
```yaml
# application-dev.properties
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:data-seed.sql
spring.sql.init.data-locations=classpath:import.sql
```

**Problems**:
- ❌ SQL syntax errors not caught at compile time
- ❌ No IDE autocomplete or refactoring support
- ❌ Schema changes require manual SQL updates
- ❌ Easy to have column name mismatches
- ❌ Reserved keywords cause errors (e.g., `value`)

### After (JPA Initialization)
```java
@Configuration
@Profile("dev")
public class JpaDataInitializer {
    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            // Type-safe Java code
            createUsers();
            createTasks();
            createRewards();
        };
    }
}
```

**Benefits**:
- ✅ Compile-time type checking
- ✅ Full IDE support (autocomplete, refactoring)
- ✅ Automatic schema synchronization
- ✅ No SQL syntax errors
- ✅ Easy to maintain and extend

## 📁 File Structure

```
src/main/resources/
├── application.properties          # Base config
├── application-dev.properties      # Dev profile (SQL init disabled)
├── application-prod.properties     # Prod profile
└── sql-backup/                     # Old SQL files (for reference)
    ├── import.sql
    └── data-seed.sql

src/main/java/.../config/
└── JpaDataInitializer.java        # NEW: JPA data initialization
```

## 🚀 How It Works

### 1. Application Startup (Dev Profile)

```
1. Hibernate creates schema from entities
   ↓
2. JpaDataInitializer.run() executes
   ↓
3. Check if data exists (userRepository.count() > 0)
   ↓
4. If no data → Create default users, tasks, rewards
   ↓
5. Log summary of created data
```

### 2. Idempotent Initialization

The initializer is **idempotent** - it only runs if no data exists:

```java
if (userRepository.count() > 0) {
    log.info("Data already exists, skipping initialization");
    return;
}
```

This means:
- ✅ First startup: Creates all data
- ✅ Subsequent startups: Skips initialization
- ✅ No duplicate data creation

## 📊 Data Created

### Users (2 accounts)
```
parent / parent123 (ROLE_PARENT)
child  / child123   (ROLE_CHILD)
```

### Tasks (7 total)

**Direct Assigned (4)**:
- 完成作业 (10 points, DAILY_ONCE)
- 打扫房间 (5 points, REPEATABLE)
- 阅读书籍 (8 points, DAILY_ONCE)
- 帮助做家务 (7 points, REPEATABLE)

**Marketplace (3)**:
- 洗碗一次 (15 points, REPEATABLE)
- 倒垃圾 (5 points, DAILY_ONCE)
- 整理客厅 (10 points, REPEATABLE)

### Rewards (11 total)

**Entertainment**:
- 游戏时间 (20 points)
- 看电影 (40 points)
- 去游乐场 (150 points)

**Food**:
- 冰淇淋 (15 points)
- 零花钱 (100 points)
- 披萨大餐 (100 points)

**Items**:
- 新玩具 (500 points)
- 图书 (40 points)
- 文具套装 (50 points)

**Privileges**:
- 选择周末活动 (80 points)
- 晚睡 1 小时特权 (40 points)
- 免做家务一次 (25 points)

## 🔧 Configuration

### Development (application-dev.properties)
```properties
# SQL initialization DISABLED
spring.sql.init.mode=never
spring.sql.init.enabled=false

# JPA auto-creates schema
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

### Production (application-prod.properties)
```properties
# Use MySQL with Flyway migrations
spring.datasource.url=jdbc:mysql://localhost:3306/creditapp
spring.jpa.hibernate.ddl-auto=validate

# DataInitializer only runs in 'dev' profile
```

## 🧪 Testing

### Run Tests
```bash
mvn clean test
```

Tests use in-memory H2 database with fresh initialization each run.

### Test JPA Initialization
```bash
# Run with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Check logs for:
# "=== JPA Data Initialization Started ==="
# "Created 2 users"
# "Created 7 tasks"
# "Created 11 rewards"
```

### Verify Data
```bash
# Login with default credentials
curl -u parent:parent123 http://localhost:8080/dashboard

# Or access via browser
http://localhost:8080/login
Username: parent
Password: parent123
```

## 📝 Extending Data Initialization

### Add New Sample Data

```java
@Configuration
@Profile("dev")
public class JpaDataInitializer {
    
    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            if (userRepository.count() > 0) return;
            
            initializeUsers();
            initializeTasks();
            initializeRewards();
            initializeLotteryThemes();  // ← Add new method
        };
    }
    
    private void initializeLotteryThemes() {
        User parent = userRepository.findByUsername("parent").orElseThrow();
        
        LotteryTheme theme = new LotteryTheme();
        theme.setName("Daily Lottery");
        theme.setPointsPerDraw(50);
        theme.setType(LotteryTypeEnum.WEIGHTED_RANDOM);
        theme.setCreatedBy(parent);
        lotteryThemeRepository.save(theme);
        
        log.info("Created lottery theme: {}", theme.getName());
    }
}
```

### Add More Test Data

```java
// Create child-specific tasks
createDirectTask(parent, child1, "数学练习", "完成 10 道数学题", 12, TaskType.DAILY_ONCE);

// Create marketplace tasks
createMarketplaceTask(parent, "额外家务", "帮忙整理车库", 20, TaskType.ONE_TIME);

// Create bonus points
pointWalletService.addPoints(child1, 500, "BONUS", null, 180);
```

## 🎯 Best Practices

### 1. Always Check for Existing Data
```java
if (userRepository.count() > 0) {
    return;  // Skip if data exists
}
```

### 2. Use Repositories (Not EntityManager)
```java
// ✅ Good
User user = userRepository.save(user);

// ❌ Avoid
entityManager.persist(user);
```

### 3. Log Initialization Progress
```java
log.info("Creating {}...", entityType);
log.info("Created {}: {}", entityType, name);
```

### 4. Use Profile Annotation
```java
@Profile("dev")  // Only runs in dev profile
@Configuration
public class JpaDataInitializer { }
```

### 5. Handle Dependencies
```java
// Create parent first
User parent = userRepository.save(parent);

// Then create child (depends on parent)
Child child = new Child();
child.setParent(parent);
childRepository.save(child);
```

## 🔍 Troubleshooting

### Issue: Data Not Created
**Symptom**: Application starts but no default data

**Solution**:
```bash
# Check profile is 'dev'
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Check logs for initialization messages
grep "JPA Data Initialization" logs/app.log
```

### Issue: Duplicate Data
**Symptom**: Multiple copies of same data

**Solution**:
```java
// Ensure count check is in place
if (userRepository.count() > 0) {
    return;  // Prevents duplicates
}
```

### Issue: Foreign Key Errors
**Symptom**: Cannot insert child records

**Solution**:
```java
// Create parent FIRST
User parent = userRepository.save(parent);

// Then create child
Child child = new Child();
child.setParent(parent);  // Parent must exist
childRepository.save(child);
```

## 📈 Migration from SQL

### Step 1: Backup SQL Files
```bash
mkdir sql-backup
mv import.sql data-seed.sql sql-backup/
```

### Step 2: Disable SQL Init
```properties
spring.sql.init.mode=never
spring.sql.init.enabled=false
```

### Step 3: Create JpaDataInitializer
```java
@Configuration
@Profile("dev")
public class JpaDataInitializer {
    @Bean
    public CommandLineRunner initializeData() {
        return args -> { /* ... */ };
    }
}
```

### Step 4: Test
```bash
mvn clean test
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## ✅ Summary

| Aspect | SQL Init | JPA Init |
|--------|----------|----------|
| **Type Safety** | ❌ Runtime errors | ✅ Compile-time checks |
| **IDE Support** | ❌ Limited | ✅ Full support |
| **Refactoring** | ❌ Manual updates | ✅ Automatic |
| **Syntax Errors** | ❌ Common | ✅ Impossible |
| **Maintainability** | ❌ Hard | ✅ Easy |
| **Testing** | ❌ Separate | ✅ Integrated |

**Status**: JPA initialization is the **recommended approach** for all environments! 🚀

---

**Last Updated**: 2026-03-15  
**Implementation**: `JpaDataInitializer.java`  
**Profile**: `dev` only  
**Status**: ✅ Active and tested
