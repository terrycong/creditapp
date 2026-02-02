# Draft: Parent Manual Points Adjustment + MANDATORY Task Enforcement

## Original Request
Implement two new features for the family credit task management system:

### Feature 1: Parent Manual Points Adjustment
- Parents can manually add/subtract points from children
- Modal popup on each child card with "Add Points"/"Subtract Points" buttons
- Allow negative points, optional reason field
- Create PointTransaction records (type: MANUAL_GRANT)

### Feature 2: MANDATORY Task Type Enforcement
- TaskType.MANDATORY already exists in enum
- Deadline: daily 23:59:59
- Period: rolling 7-day window
- Penalty trigger: when parent views task
- Late submission: get points + penalty deduction
- Task entity already has deadlineType, deadlineValue, penaltyPoints fields

### Code Refactoring Required
- TaskServiceImpl.approveCompletion() needs to create PointTransaction
- RewardServiceImpl.redeemReward() needs to create PointTransaction
- Currently code directly updates Child.points without transaction records

## Research Findings

### Existing Infrastructure
1. **PointTransaction entity exists** at `PointTransaction.java` with:
   - TransactionType enum: MANUAL_GRANT, TASK_COMPLETION, REWARD_REDEMPTION, MANDATORY_PENALTY
   - All necessary fields: child, points, type, reason, createdBy, createdAt
   - PointTransactionRepository with query methods

2. **Task entity has MANDATORY fields**:
   - deadlineType (TaskDeadlineType: DAILY, WEEKLY_TIMES)
   - deadlineValue (Integer)
   - penaltyPoints (Integer)
   - TaskType.MANDATORY exists but unused in business logic

3. **Current Issues**:
   - TaskServiceImpl.approveCompletion() modifies child.points directly WITHOUT creating PointTransaction
   - RewardServiceImpl.redeemReward() modifies child.points directly WITHOUT creating PointTransaction
   - No PointTransaction records are created anywhere in the codebase
   - No scheduler component exists for MANDATORY task penalty enforcement

4. **UI Patterns**:
   - Child cards in `parent/children.html` use Bootstrap 5
   - NO modals exist in codebase - only alert() and confirm()
   - JavaScript uses fetch() API for form submissions
   - Bootstrap 5.3 is loaded and available

5. **Child Management**:
   - All child endpoints in ViewController.java (lines 286-587)
   - Child detail view at `/parent/children/{childId}`
   - Child list with cards at `/parent/children`

6. **Existing Patterns**:
   - DAILY_ONCE task limit checking pattern in TaskServiceImpl.completeTask() (lines 144-156)
   - Points deduction pattern in RewardServiceImpl.redeemReward() (line 104)
   - Repository query pattern with date ranges for daily checks

## Key Decisions

### Feature 1: Manual Points Adjustment
- UI: Bootstrap modal on child card (no existing modal pattern to follow)
- Service: Create new PointService for all point operations
- API: POST /api/v1/children/{childId}/points
- Transaction type: MANUAL_GRANT
- Allow negative point values (for deductions)
- Reason field required for audit trail

### Feature 2: MANDATORY Task Enforcement
- Trigger: Scheduled job runs at 23:59 daily
- Penalty logic: Deduct penaltyPoints from child's balance
- Rolling window: Check last 7 days of completions
- Notification: Display penalty on parent view
- Transaction type: MANDATORY_PENALTY
- Deadline: 23:59:59 daily for DAILY type

### Refactoring Priorities
1. Create PointService as single source of truth for point modifications
2. Refactor TaskServiceImpl.approveCompletion() to use PointService
3. Refactor RewardServiceImpl.redeemReward() to use PointService
4. Ensure all point changes create PointTransaction records

## Open Questions
None - requirements are clear based on research

## Test Strategy
- Infrastructure exists: YES (mvn test passes, 19 tests)
- User wants tests: TBD (will ask user)
- Framework: JUnit 5 + Mockito (existing)

## Scope Boundaries
### INCLUDE:
- Parent manual points adjustment feature (full implementation)
- MANDATORY task type enforcement (full implementation)
- Refactoring of existing point modification code
- PointTransaction creation for all point changes
- UI for manual points adjustment (modal + form)
- Scheduler for MANDATORY task penalties

### EXCLUDE:
- Notification system for penalties (mentioned as optional)
- Email alerts
- SMS alerts
- Push notifications
- Advanced penalty configuration (ratio, limits)
- Penalty waiver/approval workflow
- Historical data migration (no existing PointTransaction records to migrate)
