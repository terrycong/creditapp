# Draft: Parent Dashboard & Child Gift History Features

## User Request Summary
The user wants to implement four features in a Spring Boot family credit task management system:

1. **Child gift history UI** - Child should see all gifts they've received
2. **Task editing by parent** - Tasks should be editable after creation by parent (UI)
3. **Parent task approval history** - Parent should see what tasks have been approved
4. **Parent gift redemption history** - Parent should see what gifts have been redeemed and credit consumption

## Current Context (from AGENTS.md)
- **Tech Stack**: Spring Boot 3, Maven, JPA, Thymeleaf, Spring Security, BCrypt, H2(dev)/MySQL(prod)
- **Architecture**: Controller-Service-Repository三层架构
- **Existing Features**:
  - Task creation, completion, approval/rejection
  - Gift redemption system
  - Parent approval page exists
  - Child can see available gifts but NOT redemption history
  - Daily task limit (DAILY_ONCE) already implemented
- **Data Structure**:
  - `task_completions` - immutable historical records with timestamps
  - `reward_redemptions` - immutable historical records with timestamps
  - No new tables needed - historical data already exists

## Research Findings

### ✅ Task Editing (Already Implemented in Backend)
**Location**: `TaskServiceImpl.updateTask()` (lines 60-78)
- Updates: title, description, points, assigned child
- **GAP**: No authorization check - any user can edit any task
- **GAP**: Cannot update task type or status through this method
- REST endpoint exists: `TaskController.updateTask()` (lines 50-56)

### ✅ History Data Infrastructure (Already Exists)
**Task Completion Repository** (`TaskCompletionRepository.java`):
- `findRecentCompletionsByParentId(parentId)` - Approved completions, ordered DESC
- `findRecentCompletionsByParentIdWithLimit(parentId, pageable)` - Paginated recent completions
- `findByChildId(Long childId)` - All completions for a child
- `countCompletionsByChildForParent(parentId)` - Aggregation with COUNT and SUM(points)

**Reward Redemption Repository** (`RewardRedemptionRepository.java`):
- `findRecentRedemptionsByParentId(parentId)` - All redemptions, ordered DESC
- `findRecentRedemptionsByParentIdWithLimit(parentId, pageable)` - Paginated
- `findByChildId(Long childId)` - All redemptions for a child
- `countRedemptionsByChildForParent(parentId)` - Aggregation with COUNT and SUM(pointsRequired)

### ✅ Dashboard Service (Already Implemented)
**Location**: `DashboardServiceImpl.java` (lines 34-481)
- Comprehensive stats with history tracking
- Recent activity (limited to 10 items - hardcoded on line 49)
- Child activity statistics table
- Today's and weekly statistics

### ✅ Existing Views
**Parent**:
- `/parent/approvals` - Shows PENDING completions only (templates/parent/approvals.html)
- `/dashboard` - Shows recent activity (10 items), stats, charts (templates/dashboard.html)

**Child**:
- `/child/rewards` - Shows `redeemedRewards` list for that child

### ❌ Gaps Identified
1. **No dedicated history pages** - Only dashboard has snippets (10 items each)
2. **No pagination** - History lists hardcoded to 10 items
3. **No date filtering** - Cannot filter by date range
4. **No task edit UI** - Backend exists but no Thymeleaf view
5. **No authorization on task edits** - Any user can edit any task
6. **Child has no dedicated history view** - Redeemed rewards shown inline but no dedicated page

## Uncertainties & Clarifying Questions

### 1. Child Gift History UI
- **Question**: Where should the child see their gift history? Should it be:
  - A new dedicated page (e.g., `/child/rewards-history`)
  - Integrated into existing child dashboard
  - A tab or section on the existing `/child/rewards` page
- **Question**: What information should be displayed? Just gift names, or also dates, points consumed, maybe a timeline view?

### 2. Task Editing by Parent (UI)
- **Question**: The backend `updateTask` method already exists. Is this request asking for:
  - A new UI page for editing tasks
  - Edit buttons on existing task list
  - Modal/popup editing from task detail view
- **Question**: Should parents be able to edit ALL task fields, or restricted fields only? (e.g., points, title, but not type to avoid breaking daily limits)
- **Question**: Should editing tasks affect already completed/approved task completions, or only future completions?

### 3. Parent Task Approval History
- **Question**: What time range should this history cover? All time, last 7 days, last 30 days, or configurable?
- **Question**: Should this be a separate page, part of parent dashboard, or an extension of the existing approval page?
- **Question**: Should it show all approved tasks, or only approved tasks with a filter by child?

### 4. Parent Gift Redemption History
- **Question**: Similar to task history - what time range?
- **Question**: Should it include:
  - Which child redeemed what gift
  - Total credits consumed per child
  - Most popular gifts (statistics)
- **Question**: Where should this live? New page, parent dashboard, or alongside task approval history?

### 5. Navigation & Integration
- **Question**: Should all these history views be accessible from a single "History" menu item, or separate navigation items?
- **Question**: Should there be any parent-level aggregated statistics (e.g., "Total credits issued this week", "Most popular gifts overall")?

### 6. Error Handling & Edge Cases
- **Question**: What should happen if a parent tries to edit a task that has pending completions?
- **Question**: What should happen if task editing would violate business rules (e.g., changing from DAILY_ONCE to REPEATABLE)?

## Dependencies & Relationships (Preliminary)
Based on AGENTS.md:
- Task editing depends on existing `TaskServiceImpl.updateTask()` method
- History views depend on repository queries for `task_completions` and `reward_redemptions`
- All features need proper Spring Security role checks (`ROLE_PARENT` vs `ROLE_CHILD`)
- Frontend templates need to follow existing Bootstrap 5 + Animate.css patterns

## Complete Research Findings (All Explore Agents Complete)

### Codebase Architecture
**Controller Layer**:
- `ViewController` - Thymeleaf view controller with role-based endpoints
- `TaskController` - REST API with create, update, delete, complete, approve, reject
- `RewardController` - REST API with CRUD and redeem operations
- `AuthController` - Login, logout, current user

**Service Layer**:
- `TaskServiceImpl` - Daily limit validation, approval workflow, completion tracking
- `RewardServiceImpl` - Points validation, inventory management, redemption
- `DashboardServiceImpl` - Comprehensive statistics (daily/weekly, child activity, popularity metrics)

**Repository Layer**:
- All repositories use `JOIN FETCH` to avoid N+1 queries
- Date-based queries: `existsCompletionToday()`, `getDailyTaskCompletionStats()`, `getDailyRewardRedemptionStats()`
- Aggregation queries: `countCompletionsByChildForParent()`, `countRedemptionsByChildForParent()`

**Security Configuration**:
- Role-based access: `/parent/**` for PARENT, `/child/**` for CHILD
- `SecurityUtils.getCurrentUserId()` - Get current user from authentication context
- `@PreAuthorize` for method-level security (exists in codebase)

### Patterns to Follow

**DTOs**:
- Pattern A: Manual Builder (`TaskDTO`, `RewardDTO`) - for complex DTOs
- Pattern B: Lombok `@Data @Builder` (`DashboardStatsDTO`, `ChildActivityDTO`) - for simple DTOs
- Validation: `@NotBlank`, `@NotNull`, `@Size` on request DTOs

**Error Handling**:
- `BusinessException` - Business rule violations with error codes
- `ResourceNotFoundException` - 404 errors
- `GlobalExceptionHandler` - `@RestControllerAdvice` handling all exceptions
- Returns `ApiResponse<T>` with code/message/data fields

**Tests**:
- JUnit 5 + Mockito + AssertJ
- `@BeforeEach` for setup
- Given-When-Then structure
- `verify()` for mock interaction verification

**Thymeleaf**:
- Bootstrap 5 + Bootstrap Icons + Animate.css + Chart.js
- `th:each`, `th:text`, `th:action`, `th:if` for dynamic rendering
- `model.addAttribute()` for passing data
- `RedirectAttributes` for flash messages
- Role-based navigation: `th:if="${role == 'PARENT'}"`

### Key Implementation Details

**Task Editing (Backend Exists)**:
- `TaskServiceImpl.updateTask(Long taskId, CreateTaskRequest request)` - lines 60-78
- Updates: title, description, points, assignedChildId
- **CRITICAL GAP**: No authorization check - ANY user can edit ANY task
- **GAP**: Cannot update task type or status

**Child Gift History (Partial)**:
- Child `/child/rewards` page already shows `redeemedRewards` list
- No dedicated history page, just inline display
- No pagination or date filtering

**Parent History Views (None)**:
- Only dashboard shows last 10 items (hardcoded limit)
- `/parent/approvals` shows PENDING only, not approved history
- No pagination, no date filtering, no detailed history pages

## Status
- ✅ All explore agents completed
- ✅ Complete codebase understanding achieved
- ❓ Need answers to clarifying questions before plan generation
