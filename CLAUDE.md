# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Family Points Task Management System built with Spring Boot 3.3.0 + Maven + JPA + Thymeleaf + Spring Security. Kids complete tasks to earn points and redeem rewards.

**Tech Stack:**
- Java 21, Spring Boot 3.3.0, Maven
- MySQL 8.0 (production) / H2 (development)
- Liquibase 4.27.0 for database migrations
- Thymeleaf + Bootstrap 5 + Animate.css for frontend
- Cucumber + JUnit 5 + Mockito for testing

## Build & Development Commands

```bash
# Compile
mvn clean compile

# Package (skip tests)
mvn clean package -DskipTests

# Run application
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run all tests
mvn test

# Run single test class
mvn test -Dtest=TaskServiceTest

# Run single test method
mvn test -Dtest=TaskServiceTest#testCreateTask

# Run Selenium UI tests
mvn clean install -Pselenium-tests

# Generate JaCoCo coverage report
mvn clean test

# Database migrations
mvn liquibase:validate   # Validate changelog
mvn liquibase:status     # Check pending changes
mvn liquibase:update     # Apply migrations
mvn liquibase:clearCheckSums  # Clear checksums (dev only)
```

## Architecture

### Layered Architecture
```
Controller (HTTP requests) → Service (business logic) → Repository (data access) → Entity (database)
```

### Package Structure
- `controller` - REST APIs and Thymeleaf page controllers
- `service` / `service.impl` - Business logic
- `repository` - JPA repositories
- `entity` - JPA entities
- `dto` - Data transfer objects
- `config` - Spring configuration
- `security` - Spring Security config, CustomUserDetailsService
- `exception` - Custom exceptions and global handler

### Core Entities

**User System:** `User`, `Child` (extends User with parent relationship)

**Task System:**
- `Task` - Task template (title, points, type)
- `TaskJob` - Task-child association (which child picked/assigned)
- `TaskCompletion` - Completion records with approval status

**Reward System:** `Reward`, `RewardRedemption`

**Lottery System:** `LotteryTheme`, `LotteryPrize`, `LotteryDraw`, `LotteryDrawResult`

**Point System:** `PointHistory`

**Other:** `PenaltyRule`, `PenaltyRecord`, `Coupon`, `Notification`

### Key Design Patterns

**Task-TaskJob Separation:** Task is purely a template; TaskJob manages child associations. This enables the marketplace feature where multiple children can pick the same task template.

**Task Types:**
- `ONE_TIME` - Complete once, not repeatable
- `REPEATABLE` - Can be completed multiple times
- `DAILY_ONCE` - Once per child per day (enforced by existsCompletionToday query)

## Database

### Liquibase Setup
- Master file: `src/main/resources/db/changelog/db.changelog-master.yaml`
- Main schema: `001-schema-ddl.sql` (all DDL)
- Initial data: `002-data-dml.sql` (all DML with INSERT ... SELECT ... WHERE NOT EXISTS)
- Incremental changes: `changes/0XX-*.sql`

### Connection Configuration
- Dev (H2): `application-dev.properties`
- Prod (MySQL): `application-prod.properties` or environment variables
- Manual migrations use `liquibase.properties`

### Production Database
```
Host: 192.168.9.113:3306
Database: creditapp
User: root
```

## Security

**Roles:** `ROLE_PARENT` (full management), `ROLE_CHILD` (limited operations)

**Authentication:** Form-based login with BCrypt password encoding

**Authorization:** Method-level `@PreAuthorize("hasRole('PARENT')")` annotations

**CSRF:** Disabled by default, enabled in production profile

## Frontend

**Template Structure:**
- `parent/` - Parent pages (dashboard, tasks, rewards, penalties, lottery, coupons)
- `child/` - Child pages (tasks, marketplace, lottery, rewards)
- `common/` - Shared fragments
- `layout/` - Main layout template

**UI Libraries:** Bootstrap 5, Bootstrap Icons, Animate.css, Chart.js

**Thymeleaf Patterns:**
```html
<th:each="task : ${tasks}">
<th:if="${task.active}">
<th:href="@{/tasks/{id}(id=${task.id})}">
<th:replace="~{common/header :: header}">
```

## Testing

**Test Frameworks:** JUnit 5, Mockito, Cucumber BDD, Selenium WebDriver

**Test Locations:**
- Unit/Integration: `src/test/java/com/creditapp/...`
- BDD Features: `src/test/resources/features/`
- Selenium: `src/test/java/com/creditapp/selenium/`

**Test Properties:** `src/test/resources/application-test.properties`

## Docker & Deployment

**Docker Compose:** `docker-compose.yml` includes creditapp, mysql, phpmyadmin, redis, nginx

**Environment Variables:**
```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/creditapp
TZ=Asia/Shanghai
```

**CI/CD:**
- GitHub Actions: `.github/workflows/docker-build.yml` (daily sync from Gitee)
- CodeWave Pipeline: `.workflow/master-pipeline.yml` (master branch build)

## API Documentation

OpenAPI/Swagger UI available at `/swagger-ui.html` (springdoc-openapi 2.5.0)

## Common Workflows

### Adding a New Feature
1. Create entity class with JPA annotations
2. Create repository interface extending JpaRepository
3. Create service interface and implementation
4. Create controller (REST API or Thymeleaf view)
5. Add Liquibase changelog for database changes
6. Write tests

### Adding Database Changes
1. Create new SQL file in `db/changelog/changes/0XX-description.sql`
2. Add changeset header: `--changeset author:id`
3. Use `CREATE TABLE IF NOT EXISTS` or `ALTER TABLE ... ADD COLUMN IF NOT EXISTS`
4. Include in master yaml
5. Run `mvn liquibase:validate` then `mvn liquibase:update`

### Task Marketplace Flow
```
Parent creates task → Task saved (no child assignment)
Child picks task → TaskJob created (childId, assignedAt)
Child completes → TaskCompletion created (PENDING)
Parent approves → Points added to child points balance
```

## Configuration Files

- `pom.xml` - Maven dependencies and plugins (JaCoCo, Liquibase, Surefire)
- `application.properties` - Base configuration
- `application-dev.properties` - Development profile (H2)
- `application-prod.properties` - Production profile (MySQL)
- `liquibase.properties` - Manual migration credentials
- `docker-compose.yml` - Local container orchestration

## Code Conventions

**Import Order:**
1. Java standard library (java.*, javax.*)
2. Third-party (org.springframework.*, lombok.*)
3. Project internal (com.creditapp.*)

**Lombok Usage:**
- `@Data` - Simple DTOs only
- `@Builder` - Complex objects with `@AllArgsConstructor` + `@NoArgsConstructor`
- `@RequiredArgsConstructor` - Service classes (constructor injection)
- **Never** use `@Data` on JPA entities (manually implement equals/hashCode)

**Entity Naming:**
- Entities: Singular (Task, User, Reward)
- Tables: Plural (tasks, users, rewards)
- Use `@Column` for explicit column names and constraints

**Test Naming:**
- Test class: `{ClassName}Test`
- Test method: `{methodName}_{Should}_{expectedResult}`

**Logging (SLF4J):**
- `ERROR` - System errors, exception stacks
- `WARN` - Recoverable exceptions, business rule violations
- `INFO` - Important business operations
- `DEBUG` - Method parameters, intermediate state
- Use placeholders: `log.info("User {} logged in", username)`

## Common Pitfalls

**N+1 Query Problem:**
- Use `JOIN FETCH` in queries or `@EntityGraph`
- Example: `SELECT t FROM Task t JOIN FETCH t.createdBy`

**Transaction Management:**
- Service layer must use `@Transactional`
- Read-only queries: `@Transactional(readOnly = true)`
- Multi-table operations require transactions

**Task-TaskJob Relationship:**
- Task has NO assignedChild field
- All child associations go through TaskJob entity
- Query pattern: `JOIN TaskJob tj ON t.id = tj.task.id WHERE tj.child.id = :childId`

## Feature Reference

**Implemented Systems:**
- Task Management (with marketplace, draft approval)
- Reward System (redemption with inventory)
- Lottery (weighted random algorithm, multiple themes)
- Point System (tracked via PointHistory)
- Penalty System (predefined rules, execution records)
- Coupon Management (batch import, individual redemption)
- Notification System (user preferences)
