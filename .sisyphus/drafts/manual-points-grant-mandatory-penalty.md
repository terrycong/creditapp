# Draft: Parent Manual Points Grant + Mandatory Task Type with Penalties

## User Request Summary

Two new features:
1. **Parent Manual Points Grant**: Parent can manually add credits to child's account with optional reason
2. **Mandatory Task Type**: New `MANDATORY` task type with penalties for not completing on time

## Existing Codebase Understanding

### Data Model (Already in Place)

**PointTransaction Entity** (`src/main/java/com/creditapp/entity/PointTransaction.java`):
- ✅ Already exists with all needed fields:
  - `TransactionType` enum: `MANUAL_GRANT`, `TASK_COMPLETION`, `REWARD_REDEMPTION`, `MANDATORY_PENALTY`
  - `points` (Integer) - can be positive or negative
  - `reason` (String, optional)
  - `child` (ManyToOne)
  - `createdBy` (ManyToOne, User)
  - `createdAt` (LocalDateTime)

**Task Entity** (`src/main/java/com/creditapp/entity/Task.java`):
- ✅ Already has MANDATORY fields:
  - `deadlineType` (TaskDeadlineType): `DAILY`, `WEEKLY_TIMES`
  - `deadlineValue` (Integer): e.g., 3 (for weekly times)
  - `penaltyPoints` (Integer): points to deduct if not completed
- ✅ `TaskType.MANDATORY` enum already exists

**CreateTaskRequest DTO** (`src/main/java/com/creditapp/dto/CreateTaskRequest.java`):
- ✅ Already includes mandatory task fields: `deadlineType`, `deadlineValue`, `penaltyPoints`

### Current Implementation Gaps

**PointTransaction Not Being Used**:
- `PointTransaction` entity and repository exist but are NOT used anywhere
- Current `TaskServiceImpl.approveCompletion()` directly modifies child points WITHOUT creating transaction record:
  ```java
  child.setPoints(child.getPoints() + points);
  childRepository.save(child);
  // ❌ No PointTransaction created
  ```

**No PointService Exists**:
- No service layer to manage point transactions
- Need to create `PointService` to:
  - Grant points manually (MANUAL_GRANT)
  - Award points for task completion (TASK_COMPLETION)
  - Deduct points for penalties (MANDATORY_PENALTY)
  - Deduct points for rewards (REWARD_REDEMPTION)

## Critical Questions About Mandatory Task Penalties

### 1. How do we determine "on time" vs "late"?

**DAILY deadline type** (deadlineType = DAILY):
- Question: What's the "end of day" time? 23:59:59? 00:00:00 next day?
- Consideration: Family timezone (not hardcoded to UTC)

**WEEKLY_TIMES deadline type** (deadlineType = WEEKLY_TIMES):
- Question: What's considered a "week"? Monday-Sunday? Rolling 7-day window?
- Consideration: Need to count completions within the period and check against `deadlineValue`

### 2. When should the penalty be triggered?

**Options**:
A. **Scheduled Job** (cron job runs daily/hourly):
   - Pro: Automated, no manual intervention
   - Con: Need to ensure idempotency (don't penalize twice)

B. **On Approval** (when parent approves the completion):
   - Pro: Simpler logic, no scheduled jobs
   - Con: Child could submit late, parent approves late, no penalty

C. **On Deadline Check** (when parent views tasks/daily):
   - Pro: User-initiated, easier to test
   - Con: Depends on user action

**Recommended**: Scheduled job + idempotency check (e.g., track penaltyApplied flag in TaskCompletion)

### 3. What happens if deadline passes but task was already submitted for approval?

**Scenario**: Task deadline = 2026-02-02 23:59:59, child submits at 23:50:00, parent approves at 2026-02-03 10:00:00

**Question**: Should this be considered "on time" or "late"?

**Options**:
- **On-time by submission**: If child submitted before deadline, consider it on-time
- **On-time by approval**: Only consider approved completions
- **Hybrid**: If submitted before deadline, give grace period (e.g., 24h) for approval

### 4. How do we track "weekly times" completions?

**Scenario**: `deadlineType = WEEKLY_TIMES`, `deadlineValue = 3`, child needs to complete 3 times per week

**Questions**:
- How do we track which completions count toward the 3?
- What happens if child completes 2 times - partial penalty? Or no penalty?
- What's the weekly period (e.g., Monday-Sunday)?

**Approach**:
- Need a new query to count MANDATORY task completions in the current week
- If count < deadlineValue at week end, apply penalty

### 5. Should we create a separate PenaltyTransaction entity?

**Question**: Should penalties be a separate entity or just use `PointTransaction` with type `MANDATORY_PENALTY`?

**Recommendation**: Use existing `PointTransaction.MANDATORY_PENALTY` (already defined)

## Proposed Architecture

### New Service Layer: PointService

```java
public interface PointService {
    // Manual grant (Feature 1)
    void grantPoints(Long childId, Integer points, String reason, Long createdBy);

    // Award points for task completion
    void awardPoints(Long childId, Long taskId, Integer points);

    // Deduct points for penalty (Feature 2)
    void deductPenalty(Long childId, Long taskId, Integer penaltyPoints, String reason);

    // Deduct points for reward redemption
    void deductForReward(Long childId, Long rewardId, Integer points);
}
```

### Scheduled Job for Penalties (Feature 2)

```java
@Component
public class MandatoryTaskPenaltyScheduler {
    @Scheduled(cron = "0 0 1 * * ?") // 1:00 AM daily
    public void checkAndApplyPenalties() {
        // Find all MANDATORY tasks that missed deadlines
        // Apply penalties via PointService
    }
}
```

### New Repository Queries Needed

**TaskRepository**:
- `findMandatoryTasksNeedingPenalty()` - Find MANDATORY tasks with incomplete deadlines
- `findWeeklyMandatoryTaskCompletions()` - Count completions in current week

**TaskCompletionRepository**:
- `findPendingOrApprovedCompletionsForTaskSince()` - For counting weekly completions

## User Decisions (Final)

1. **Daily Deadline**: 23:59:59 (end of current day)
2. **Weekly Period**: Rolling 7-day window from task creation
3. **Penalty Trigger**: When parent views/approvals (user-triggered)
4. **Late Submission判定**: Submission time matters (if child submitted before deadline = on-time)
5. **Partial Weekly Completion**: Configurable by parent (need UI field to set penalty behavior)
6. **Refactoring**: YES - refactor existing code to use PointTransaction for all point changes

## Feature 1: Manual Points Grant - Requirements

**Backend**:
- Create `PointsService` (not PointTransactionService) with `grantPoints(childId, points, reason)` method
- Creates `PointTransaction` with type `MANUAL_GRANT`
- Updates `Child.points`
- Controller: `POST /parent/children/{childId}/grant-points`

**Frontend**:
- Add "Grant Points" button on child management page (`/parent/children`)
- Modal/form to enter points and reason
- Show success/error messages

## Feature 2: Mandatory Task Implementation - Requirements

**Task Creation**:
- When parent creates MANDATORY task, show deadlineType, deadlineValue, penaltyPoints fields
- Add penaltyBehavior field: FULL_PENALTY, PROPORTIONAL, ALL_OR_NOTHING

**Task Completion**:
- When child completes MANDATORY task: record as normal (PENDING)
- Store submission timestamp in TaskCompletion.completedAt

**Task Approval**:
- When parent approves:
  - Check if submission time is before deadline
  - If on-time: award full points
  - If late: deduct penaltyPoints (create PointTransaction with MANDATORY_PENALTY)
  - Still award points even if late? Or no points if late? **DECISION NEEDED**

**Pending Completions View**:
- When parent views pending completions (`/parent/approvals`):
  - Highlight MANDATORY tasks with deadline info
  - Show "On Time" / "Late" indicator based on completedAt vs deadline

**Penalty Calculation Logic**:
- **DAILY**: deadline = task creation date + 1 day at 23:59:59
  - If completedAt < deadline: on-time
  - Else: late, apply penalty
- **WEEKLY_TIMES**: Count completions in 7-day window from task creation
  - If count >= deadlineValue: no penalty
  - Else: apply penalty based on penaltyBehavior

## Refactoring Existing Code

**TaskServiceImpl.approveCompletion()**:
1. Create PointTransaction for task completion points (TASK_COMPLETION)
2. For MANDATORY tasks: Check deadline and create MANDATORY_PENALTY transaction if late
3. Update Child.points (sum all transactions)

**RewardServiceImpl.redeemReward()**:
1. Create PointTransaction for point deduction (REWARD_REDEMPTION)
2. Update Child.points

## Scope Boundaries (As Currently Understood)

### Feature 1: Parent Manual Points Grant
- **INCLUDE**: New page/form for parent to grant points
- **INCLUDE**: PointService to manage transactions
- **INCLUDE**: UI to show transaction history (reuse existing Dashboard stats)
- **EXCLUDE**: Bulk point grants, scheduled recurring grants

### Feature 2: Mandatory Task Type
- **INCLUDE**: Scheduled job to check deadlines and apply penalties
- **INCLUDE**: New repository queries for deadline checking
- **INCLUDE**: Penalty display on dashboard (e.g., "You lost 10 points for missing deadline")
- **EXCLUDE**: Customizable grace periods, penalty appeals, partial penalties

## Research Results (From Explore Agent)

### Key Findings

**1. PointTransaction Entity is COMPLETE but UNUSED**
- Entity exists with all needed fields and enum values
- `TransactionType` enum includes: `MANUAL_GRANT`, `TASK_COMPLETION`, `REWARD_REDEMPTION`, `MANDATORY_PENALTY`
- PointTransactionRepository has 3 query methods ready
- **CRITICAL**: No service layer uses PointTransaction - all point changes are direct Child.setPoints()

**2. Mandatory Task Data Model is COMPLETE but ENFORCED NOWHERE**
- Task entity already has: `deadlineType`, `deadlineValue`, `penaltyPoints`
- TaskType.MANDATORY exists
- TaskDeadlineType enum: DAILY, WEEKLY_TIMES
- **CRITICAL**: No business logic checks deadlines or applies penalties
- **CRITICAL**: No scheduled jobs exist in codebase

**3. Current Point Management Pattern (Needs Refactoring)**
- TaskServiceImpl.approveCompletion(): Direct point update, no transaction logging
- RewardServiceImpl.redeemReward(): Direct point deduction, no transaction logging
- No PointTransactionService exists
- **IMPLICATION**: If we want point history, we MUST refactor existing code

**4. Scheduled Jobs: ZERO INFRASTRUCTURE**
- No @EnableScheduling found
- No @Scheduled methods
- No scheduler classes
- **IMPLICATION**: Must create scheduling infrastructure from scratch

### Architecture Gap Summary

| Layer | Manual Points Grant | Mandatory Penalties |
|--------|-------------------|-------------------|
| Entity | ✅ Complete | ✅ Complete |
| Repository | ✅ Ready | ❌ Need new queries |
| Service | ❌ Missing | ❌ Missing |
| Controller | ❌ Missing | N/A |
| Scheduled Job | N/A | ❌ Missing |
| Transaction Logic | ❌ Direct updates bypass PointTransaction | ❌ Direct updates bypass PointTransaction |

---

## Research Results (From Explore Agent - Gap Analysis)

### 16 Critical Gaps and Edge Cases Identified

**1. PointTransaction Not Used Anywhere**
- Entity exists but zero usage in current codebase
- Direct point modifications bypass transaction logging
- Refactoring will break existing behavior

**2. Timezone Issues**
- All date operations use system default timezone
- DST transitions can cause deadline miscalculations
- WEEKLY_TIMES across DST boundary = 167 or 169 hours, not 168
- **Recommendation**: Store UTC, convert for display

**3. WEEKLY_TIMES Tracking Complexity**
- No mechanism to track which rolling 7-day window completions belong to
- When does requirement reset? Day 7? Day 8?
- Missing repository query for window-based counting

**4. DAILY Deadline Logic Ambiguity**
- What if parent changes task to MANDATORY after creation?
- Deadline calculation timing unclear (creation time vs when MANDATORY status set)

**5. Penalty Deduction Idempotency (CRITICAL)**
- No protection against double penalty deduction
- Parent refreshes page → double penalty!
- Missing reference fields to link penalties to completions
- **Solution needed**: Add penaltyApplied flag to TaskCompletion

**6. Child Points Can Go Negative**
- No validation preventing negative balances after penalty
- Reward redemption has insufficient points check, penalties don't
- Should negative be allowed? Minimum threshold?

**7. Transaction Rollback Scenarios**
- Inconsistent transaction boundaries
- PointTransaction saved, Child.save() fails → orphaned record
- Need atomic PointService operations

**8. Backward Compatibility**
- Child.points currently denormalized, may become calculated
- Existing completions/redemptions have no PointTransaction records
- Need migration strategy for existing data

**9. Missing DTOs and Validation**
- No PointTransactionDTO for displaying history
- CreateTaskRequest missing mandatory task fields (deadlineType, penaltyPoints, etc.)
- Missing validation: penaltyPoints can be negative, deadlineValue can be 0

**10. UI Considerations**
- No views for point transaction history
- No manual points grant/deduct page
- No deadline display for MANDATORY tasks
- No penalty notification system

**11. Penalty Trigger Timing**
- "User-triggered" is ambiguous
- Parent approves batch → how to calculate penalties before approving?
- Recommend: Calculate isOverdue at submission time, not approval time

**12. Database Performance**
- Dashboard stats currently use single field Child.points
- After refactoring, would need SUM all transactions
- Missing aggregate queries

**13. Concurrent Modification Issues**
- No locking for point updates
- Race conditions: parent grants + child redeems simultaneously
- Need @Version with optimistic locking

**14. Audit Trail Missing**
- PointTransaction missing reference fields (taskCompletionId, rewardRedemptionId)
- Missing previousBalance, newBalance for audit
- Missing referenceType + referenceId for idempotency

**15. Testing Gaps**
- Missing test cases for:
  - Negative balance thresholds
  - WEEKLY_TIMES across DST boundary
  - Idempotent penalty deduction
  - Transaction rollback
  - Concurrent modifications

**16. Error Handling Inconsistencies**
- Different exception types used inconsistently
- Need custom exceptions: InsufficientPointsException, DuplicatePenaltyException

---

## Resolutions Applied to Plan

Based on gap analysis, I will:

1. **Add penaltyApplied flag to TaskCompletion entity** for idempotency
2. **Add audit fields to PointTransaction** (previousBalance, newBalance, referenceType, referenceId)
3. **Create PointTransactionDTO** for UI display
4. **Update CreateTaskRequest** to include mandatory task fields with validation
5. **Design PointService** with atomic @Transactional methods
6. **Add repository queries** for date-range filtering and aggregation
7. **Add validation** for negative point balances (allow with minimum threshold)
8. **Add @Version** to Child entity for optimistic locking
9. **Create data migration** script for existing completions/redemptions
10. **Design WEEKLY_TIMES logic** with clear window tracking (simplified to count within 7-day window, reset daily)
11. **Define timezone strategy**: Use system timezone with documented assumption (simpler than UTC migration)
12. **Define negative balance policy**: Allow negative to -500, display as "欠分: 50"
13. **Add custom exceptions** for point operations

---

## Final Scope Decisions

### Feature 1: Manual Points Grant
**INCLUDE**:
- Create PointService with grantPoints() and deductPoints() methods
- Create PointTransactionController with manual grant/deduct endpoints
- Create PointTransactionDTO for display
- Update parent/children.html with grant/deduct modal
- Add point transaction history view

**EXCLUDE**:
- Undo functionality for manual adjustments (future enhancement)
- Bulk point operations (keep to single child)

### Feature 2: MANDATORY Task Type
**INCLUDE**:
- Update CreateTaskRequest to include mandatory task fields with validation
- Update TaskServiceImpl.createTask() to set mandatory fields
- Add deadline checking logic to TaskServiceImpl.approveCompletion()
- Create penalty deduction with idempotency check
- Update parent/approvals.html to show deadline and on-time/late status
- Add penaltyApplied flag to TaskCompletion entity
- Add audit fields to PointTransaction entity

**EXCLUDE**:
- Scheduled job for automatic penalties (user-triggered only)
- Grace period configuration (fixed to 0)
- WEEKLY_TIMES complex window tracking (simplified to count within rolling 7 days)

### Feature 3: Refactoring
**INCLUDE**:
- Refactor TaskServiceImpl.approveCompletion() to use PointService
- Refactor RewardServiceImpl.redeemReward() to use PointService
- Add @Version to Child entity
- Create data migration script in import.sql
- Add repository queries for point aggregation

**EXCLUDE**:
- Migrate Child.points to calculated field (keep denormalized for performance)
- Change timezone handling to UTC (keep system timezone)
