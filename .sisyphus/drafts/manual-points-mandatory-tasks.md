# Draft: Manual Points Grant + MANDATORY Task Type Features

## User Request Summary

### Feature 1: Manual Points Grant
- Parent can manually add/deduct points to child
- Creates `PointTransaction` with type `MANUAL_GRANT`
- Updates `Child.points`
- Frontend: Add buttons on parent/children.html

### Feature 2: MANDATORY Task Type
- Parent creates task with type `MANDATORY`, sets deadlineType, deadlineValue, penaltyPoints
- **Deadline Logic**:
  - DAILY: deadline = task.createdAt's date at 23:59:59
  - WEEKLY_TIMES: rolling 7-day window, must complete deadlineValue times
- **Approval Logic**:
  - If submission time <= deadline: award full task points (TASK_COMPLETION)
  - If submission time > deadline: award full task points + deduct penalty (MANDATORY_PENALTY)
- **UI**: Show deadline info and "On Time" / "Late" indicator

### Feature 3: Refactor Existing Code
- Modify `TaskServiceImpl.approveCompletion()` to create PointTransaction for TASK_COMPLETION
- Modify `RewardServiceImpl.redeemReward()` to create PointTransaction for REWARD_REDEMPTION

## Known Context

### PointTransaction Entity (EXISTS, UNUSED)
- **Fields**: id, child, points, type, reason, createdBy, createdAt
- **TransactionType enum**: MANUAL_GRANT, TASK_COMPLETION, REWARD_REDEMPTION, MANDATORY_PENALTY
- **Repository exists**: `PointTransactionRepository` with methods:
  - `findByChildIdOrderByCreatedAtDesc(Long childId)`
  - `findByParentId(Long parentId)`
  - `findByChildIdAndTypeOrderByCreatedAtDesc(Long childId, TransactionType type)`

### TaskType Enum (EXISTS)
- Values: ONE_TIME, REPEATABLE, DAILY_ONCE, MANDATORY

### TaskDeadlineType Enum (EXISTS)
- Values: DAILY, WEEKLY_TIMES

### Task Entity (EXISTS)
- Has deadlineType, deadlineValue, penaltyPoints fields
- Currently these fields are NOT being used

### Current Implementation
- `TaskServiceImpl.approveCompletion()` (line 171-191): Directly updates `Child.points` without creating PointTransaction
- `RewardServiceImpl.redeemReward()` (line 84-121): Directly updates `Child.points` without creating PointTransaction
- No PointTransaction creation exists anywhere in codebase

### Frontend Templates
- parent/children.html: Child cards with points display, buttons for details/edit/delete
- parent/tasks.html: Task creation form with type selection (ONE_TIME, REPEATABLE, DAILY_ONLY only)
- parent/approvals.html: Approval list with approve/reject buttons, navigation tabs
- UI uses Bootstrap 5 with Animate.css, custom color theme

## Research Findings (COMPLETED)

### PointTransaction Entity
- **Location**: `src/main/java/com/creditapp/entity/PointTransaction.java`
- **Fields**: id, child, points, type, reason, createdBy, createdAt
- **TransactionType enum**: MANUAL_GRANT, TASK_COMPLETION, REWARD_REDEMPTION, MANDATORY_PENALTY
- **CRITICAL FINDING**: Entity exists but is **NOT USED** in codebase
- **Repository**: `PointTransactionRepository` exists with 3 query methods but never called

### TaskServiceImpl.approveCompletion()
- **Location**: Lines 171-191 in `TaskServiceImpl.java`
- **Current Behavior**: Directly updates `child.setPoints(child.getPoints() + points)`
- **Problem**: No PointTransaction record created
- **Needs**: Refactor to create PointTransaction with type TASK_COMPLETION

### RewardServiceImpl.redeemReward()
- **Location**: Lines 84-121 in `RewardServiceImpl.java`
- **Current Behavior**: Directly updates `child.setPoints(child.getPoints() - reward.getPointsRequired())`
- **Problem**: No PointTransaction record created
- **Needs**: Refactor to create PointTransaction with type REWARD_REDEMPTION (negative points)

### TaskType Enum
- **Location**: `src/main/java/com/creditapp/entity/TaskType.java`
- **Values**: ONE_TIME, REPEATABLE, DAILY_ONCE, MANDATORY

### TaskDeadlineType Enum
- **Location**: `src/main/java/com/creditapp/entity/TaskDeadlineType.java`
- **Values**: DAILY, WEEKLY_TIMES

### Task Entity (MANDATORY fields)
- **Location**: `src/main/java/com/creditapp/entity/Task.java`
- **Fields**: deadlineType (TaskDeadlineType), deadlineValue (Integer), penaltyPoints (Integer)
- **Status**: Fields exist but not currently used in task creation/update logic

### Frontend Templates
- **parent/children.html**: Card-based child list with points display, action buttons
- **parent/approvals.html**: Table-based approval list with approve/reject buttons
- **parent/tasks.html**: Task creation form (doesn't include MANDATORY fields in UI)
- **Pattern**: Bootstrap 5 + Animate.css, no existing modal pattern found

### Repository Layer
- **PointTransactionRepository**: Exists but unused
- **ChildRepository**: Has parent and username queries
- **TaskRepository**: Has draft task queries and JOIN FETCH optimizations
- **No PointTransactionService**: No dedicated service for point transaction management

### Service Layer Patterns
- **@Transactional**: Used on all write operations
- **Repository injection**: Via Lombok @RequiredArgsConstructor
- **DTO pattern**: Builder pattern with @Builder annotation
- **No point transaction service**: All point updates done directly on Child entity

## User Decisions (FINALIZED)

### 1. Manual Points Grant UI
**Decision**: Modal popup on child cards in `parent/children.html`
- Add "Grant Points" and "Deduct Points" buttons
- Click opens Bootstrap modal with points amount and optional reason field
- Default placeholders: "手动发放积分" / "手动扣除积分"

### 2. Negative Point Balances
**Decision**: Allow negative points
- MANDATORY task penalties may cause negative balances
- No blocking when child goes below zero
- More flexible system

### 3. WEEKLY_TIMES Deadline Calculation
**Decision**: Rolling 7-day window
- Any 7 consecutive days from task creation date
- Window slides each day
- Child must complete task `deadlineValue` times within window

### 4. Reason Field for Manual Points
**Decision**: Optional but shown
- Reason field visible in modal
- Not required but encouraged for tracking
- Default placeholder provided

### 5. Testing Approach
**Decision**: TDD (Test-Driven Development)
- Write tests before implementation
- Test infrastructure exists (spring-boot-starter-test)
- All features must have test coverage

### 6. PointTransaction Creation Pattern
**Decision**: Transaction pattern for all point changes
1. Create PointTransaction record first
2. Update Child.points
3. Save both in same @Transactional method

### 7. MANDATORY Task Creation UI
**Decision**: Dynamic form fields
- Show deadlineType, deadlineValue, penaltyPoints fields only when TaskType=MANDATORY
- Use JavaScript to show/hide based on task type selection
- Conditional validation

## Scope Boundaries

### INCLUDE (IN SCOPE):
- PointTransactionService creation with CRUD operations
- Refactor TaskServiceImpl.approveCompletion() to use PointTransaction
- Refactor RewardServiceImpl.redeemReward() to use PointTransaction
- Manual points grant feature (modal UI + controller + service)
- MANDATORY task creation UI enhancement
- MANDATORY task deadline logic (DAILY, WEEKLY_TIMES)
- MANDATORY task penalty logic (late submission handling)
- Approval page enhancements (deadline display, on-time/late indicator)
- TDD test suite for all new features

### EXCLUDE (OUT OF SCOPE):
- Point transaction history page for viewing all transactions
- Scheduled job for automatic penalty application (manual approval only)
- Child view of point transaction history
- Point transaction editing or deletion
- Recalculating points from transactions (current Child.points field remains source of truth)
