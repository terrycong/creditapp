# Task Marketplace Feature Implementation Plan

## TL;DR

> **Quick Summary**: Add marketplace capability where children can browse and pick tasks from their parent, then complete them to earn points. Parents can still manually assign tasks directly.
>
> **Deliverables**:
> - Task entity with `pickedByChild` field
> - Marketplace browsing page for children (`/child/marketplace`)
> - Task pick/unpick functionality
> - Integration with existing task completion flow
> - Parent can create marketplace-available tasks
>
> **Estimated Effort**: Medium
> **Parallel Execution**: YES - 3 waves
> **Critical Path**: Entity change → Repository → Service → Controller → Frontend

---

## Context

### Original Request
Implement a Task Marketplace feature where:
1. Children can browse and CHOOSE tasks from a marketplace
2. Once picked and completed, earn credits (points)
3. Parents can still manually assign tasks to specific children

### Interview Summary
**Key Decisions Made**:
- **Pick Mechanism**: Add `pickedByChild` field to Task entity (separate from `assignedChild`)
- **Marketplace Logic**: Available tasks = `assignedChild = null AND pickedByChild = null AND createdBy.parent.id = child.parent.id`
- **Picked Tasks**: `pickedByChild = currentChild`
- **Parent Assigned**: `assignedChild = specificChild` (bypasses marketplace)
- **Task Uniqueness**: First-come-first-served (once picked, not visible to other children)
- **Approval Flow**: Keep existing (PENDING → Parent approves → Points awarded)
- **Parent Creation**: Make `assignedChild` optional with "Available in Marketplace" checkbox

**Research Findings**:
- Task entity uses manual getters/setters (no Lombok @Data)
- Repository pattern uses @Query with JOIN FETCH for N+1 prevention
- Service layer uses @Transactional and custom exceptions (BusinessException, ResourceNotFoundException)
- DTOs use Builder pattern
- Test infrastructure: JUnit 5 + Mockito with given-when-then pattern
- Frontend uses Bootstrap 5 + Animate.css + AJAX fetch API

### Self-Review (Gap Analysis)

**Gaps Identified and Resolved**:

| Gap | Type | Resolution |
|------|-------|------------|
| **DAILY_ONCE for picked tasks** | Minor | Existing `existsCompletionToday()` check applies to picked tasks (uses childId, not assignedChild) |
| **Unpick functionality** | Minor | Add `unpickTask()` service method and POST endpoint |
| **Child cannot pick from other families** | Critical | Query filters by `createdBy.parent.id == child.parent.id` to isolate by family |
| **Picked task in My Tasks list** | Minor | Modify `findActiveTasksWithChild()` to include `pickedByChild.id = :childId` |
| **Parent sees pickedByChild info** | Minor | Add `pickedByChildId` and `pickedByChildName` to TaskDTO |
| **Task creation with null assignedChild** | Minor | Make assignedChild optional in CreateTaskRequest and parent form |
| **Database column for pickedByChild** | Minor | Hibernate auto-creates column via @ManyToOne annotation |
| **Navigation update** | Minor | Add "任务市场" link to child navbar |

---

## Work Objectives

### Core Objective
Implement a task marketplace where children can browse unassigned tasks from their parent, pick them, and complete them to earn points, while preserving parent's ability to manually assign tasks directly.

### Concrete Deliverables
- Task.java with `pickedByChild` field
- TaskDTO.java with `pickedByChildId` and `pickedByChildName` fields
- TaskRepository with marketplace query methods
- TaskService with pick/unpick methods
- ViewController with marketplace endpoints
- child/marketplace.html template
- Updated child navigation
- Updated parent task creation form

### Definition of Done
- [ ] `mvn clean compile` succeeds
- [ ] All 19 existing tests pass (`mvn test`)
- [ ] Child can browse marketplace at `/child/marketplace`
- [ ] Child can pick task from marketplace
- [ ] Picked task appears in `/child/tasks`
- [ ] Child can complete picked task
- [ ] Parent approves picked task completion
- [ ] Parent can create marketplace-available task
- [ ] DAILY_ONCE limit still works for picked tasks

### Must Have
- `pickedByChild` field in Task entity (nullable ManyToOne)
- Marketplace query filters by parent's tasks only (not global)
- Pick operation prevents multiple children from picking same task
- Picked tasks integrate seamlessly with existing completion flow
- All new code follows project conventions (no Lombok on entity, Builder on DTO)

### Must NOT Have (Guardrails)
- **AI Slop**: Do NOT create separate TaskMarketplace entity (reuse Task)
- **AI Slop**: Do NOT add "marketplace" to TaskType enum (use pickedByChild flag)
- **AI Slop**: Do NOT create separate task creation form (extend existing form)
- **Scope Boundary**: Do NOT implement task search/filtering in marketplace (keep simple list)
- **Scope Boundary**: Do NOT modify reward redemption flow (only tasks)

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: YES (JUnit 5 + Mockito)
- **User wants tests**: YES (TDD - Red-Green-Refactor)
- **Framework**: JUnit 5 with Mockito

### If TDD Enabled

Each TODO follows RED-GREEN-REFACTOR:

**Task Structure**:
1. **RED**: Write failing test first
   - Test file: `src/test/java/com/creditapp/service/TaskServiceMarketplaceTest.java`
   - Test command: `mvn test -Dtest=TaskServiceMarketplaceTest#pickTask_ShouldSetPickedByChild`
   - Expected: FAIL (method doesn't exist yet)

2. **GREEN**: Implement minimum code to pass
   - Command: `mvn test -Dtest=TaskServiceMarketplaceTest#pickTask_ShouldSetPickedByChild`
   - Expected: PASS

3. **REFACTOR**: Clean up while keeping green
   - Command: `mvn test -Dtest=TaskServiceMarketplaceTest`
   - Expected: PASS (all tests still green)

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately):
├── Task 1: Add pickedByChild field to Task entity
├── Task 2: Add pickedByChild fields to TaskDTO
├── Task 3: Add repository query methods
└── Task 4: Add pick/unpick service methods

Wave 2 (After Wave 1):
├── Task 5: Add marketplace controller endpoints
├── Task 6: Update TaskServiceImpl to include picked tasks
└── Task 7: Create child/marketplace.html template

Wave 3 (After Wave 2):
├── Task 8: Update parent task creation form
├── Task 9: Update child navigation
├── Task 10: Write all tests
└── Task 11: Run full test suite and build

Critical Path: Task 1 → Task 3 → Task 4 → Task 5 → Task 7 → Task 11
Parallel Speedup: ~35% faster than sequential
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 2, 3 | None (first) |
| 2 | 1 | 4 | 3 |
| 3 | None | 4, 5, 6 | 1, 2 |
| 4 | 2, 3 | 5, 6 | None |
| 5 | 3, 4 | 7 | 6 |
| 6 | 3 | None | 5 |
| 7 | 5 | None | 6, 8, 9 |
| 8 | 4 | None | 9, 10 |
| 9 | None | None | 8, 10 |
| 10 | 4, 5, 6 | None | 8, 9 |
| 11 | 7, 8, 9, 10 | None | None (final) |

### Agent Dispatch Summary

| Wave | Tasks | Recommended Agents |
|------|-------|-------------------|
| 1 | 1, 2, 3, 4 | delegate_task(category="quick", load_skills=["git-master"], run_in_background=true) |
| 2 | 5, 6, 7 | delegate_task(category="unspecified-high", load_skills=["frontend-ui-ux"], run_in_background=true) |
| 3 | 8, 9, 10, 11 | delegate_task(category="quick", load_skills=[], run_in_background=true) |

---

## TODOs

- [ ] 1. Add pickedByChild field to Task entity

  **What to do**:
  - Add `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "picked_by_child_id") private Child pickedByChild;`
  - Add getters and setters for pickedByChild
  - No constructor changes needed (null by default)

  **Must NOT do**:
  - Do NOT use Lombok @Data on entity (follow existing pattern)
  - Do NOT create separate TaskMarketplace entity

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple entity field addition, follows existing patterns
  - **Skills**: None needed
  - **Skills Evaluated but Omitted**:
    - `git-master`: Not needed for single file edit

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3, 4)
  - **Blocks**: Task 2 (DTO field depends on entity field existence for consistency)
  - **Blocked By**: None (can start immediately)

  **References**:
  - **Pattern References**:
    - `src/main/java/com/creditapp/entity/Task.java:32-34` - assignedChild field pattern for pickedByChild
  - **Entity Design**:
    - `src/main/java/com/creditapp/entity/Task.java:28-34` - Existing relationship mapping patterns

  **Acceptance Criteria**:
  - [ ] Test: Verify Task.java compiles: `mvn clean compile`
  - [ ] Verify: pickedByChild field exists with correct annotations (ManyToOne, JoinColumn)
  - [ ] Verify: Getters and setters follow existing pattern (camelCase, manual implementation)

  **Commit**: YES
  - Message: `feat(entity): add pickedByChild field to Task for marketplace feature`
  - Files: `src/main/java/com/creditapp/entity/Task.java`
  - Pre-commit: `mvn clean compile`

- [ ] 2. Add pickedByChild fields to TaskDTO

  **What to do**:
  - Add `private Long pickedByChildId;`
  - Add `private String pickedByChildName;`
  - Add getters and setters for both fields
  - Update Builder class with pickedByChildId and pickedByChildName
  - Update build() method to set DTO fields from builder

  **Must NOT do**:
  - Do NOT remove existing fields
  - Do NOT change existing Builder methods

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: DTO field addition follows existing Builder pattern
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3, 4)
  - **Blocks**: Task 4 (service uses DTO fields)
  - **Blocked By**: Task 1 (entity field should match DTO pattern)

  **References**:
  - **Pattern References**:
    - `src/main/java/com/creditapp/dto/TaskDTO.java:16-17` - assignedChildId/Name pattern for pickedByChild
    - `src/main/java/com/creditapp/dto/TaskDTO.java:138-143` - Builder methods pattern
    - `src/main/java/com/creditapp/dto/TaskDTO.java:229-246` - build() method pattern

  **Acceptance Criteria**:
  - [ ] Test: Verify TaskDTO compiles: `mvn clean compile`
  - [ ] Verify: pickedByChildId and pickedByChildName fields exist
  - [ ] Verify: Builder has pickedByChildId() and pickedByChildName() methods
  - [ ] Verify: build() method sets DTO fields from builder

  **Commit**: YES (groups with Task 1)
  - Message: `feat(dto): add pickedByChild fields to TaskDTO`
  - Files: `src/main/java/com/creditapp/dto/TaskDTO.java`
  - Pre-commit: `mvn clean compile`

- [ ] 3. Add marketplace query methods to TaskRepository

  **What to do**:
  - Add `findAvailableMarketplaceTasksByParentId(@Param("parentId") Long parentId)`
  - Add `findPickedTasksByChildId(@Param("childId") Long childId)`
  - Modify existing `findActiveTasksWithChild` query to include picked tasks
  - Use JOIN FETCH for child to avoid N+1

  **Exact Repository Method Signatures**:
  ```java
  // Find unassigned, unpicked tasks from parent
  @Query("SELECT t FROM Task t " +
         "JOIN t.createdBy cb " +
         "WHERE t.assignedChild IS NULL " +
         "AND t.pickedByChild IS NULL " +
         "AND cb.parent.id = :parentId " +
         "AND t.active = true " +
         "AND t.status = 'APPROVED'")
  List<Task> findAvailableMarketplaceTasksByParentId(@Param("parentId") Long parentId);

  // Find tasks picked by specific child
  @Query("SELECT t FROM Task t JOIN FETCH t.pickedByChild " +
         "WHERE t.pickedByChild.id = :childId AND t.active = true")
  List<Task> findPickedTasksByChildId(@Param("childId") Long childId);
  ```

  **Must NOT do**:
  - Do NOT query tasks from other families (filter by parent.id)
  - Do NOT include DRAFT tasks in marketplace
  - Do NOT forget JOIN FETCH (avoid N+1)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Repository queries follow existing JPQL patterns
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 4)
  - **Blocks**: Task 4 (service uses repository methods)
  - **Blocked By**: None (can start immediately)

  **References**:
  - **Pattern References**:
    - `src/main/java/com/creditapp/repository/TaskRepository.java:19-20` - findActiveTasksWithChild JOIN FETCH pattern
    - `src/main/java/com/creditapp/repository/TaskRepository.java:22-28` - findDraftTasksByParentId complex query pattern

  **Acceptance Criteria**:
  - [ ] Test: Verify TaskRepository compiles: `mvn clean compile`
  - [ ] Verify: findAvailableMarketplaceTasksByParentId method exists with correct query
  - [ ] Verify: findPickedTasksByChildId method exists with JOIN FETCH
  - [ ] Verify: Queries filter by parent.id (not global)

  **Commit**: YES
  - Message: `feat(repository): add marketplace query methods`
  - Files: `src/main/java/com/creditapp/repository/TaskRepository.java`
  - Pre-commit: `mvn clean compile`

- [ ] 4. Add pick/unpick service methods to TaskService and TaskServiceImpl

  **What to do**:
  - Add interface methods to TaskService:
    - `TaskDTO pickTask(Long taskId, Long childId)`
    - `void unpickTask(Long taskId, Long childId)`
    - `List<TaskDTO> getMarketplaceTasks(Long childId)`
    - `List<TaskDTO> getPickedTasks(Long childId)`
  - Implement in TaskServiceImpl:
    - pickTask: Validate task exists, not already picked, belongs to child's parent, set pickedByChild
    - unpickTask: Validate task picked by this child, clear pickedByChild
    - getMarketplaceTasks: Query by child.parent.id
    - getPickedTasks: Query by childId
  - Update TaskServiceImpl.toDTO() to include pickedByChildId and pickedByChildName

  **Exact Service Method Signatures**:
  ```java
  // In TaskService.java (add to interface):
  TaskDTO pickTask(Long taskId, Long childId);
  void unpickTask(Long taskId, Long childId);
  List<TaskDTO> getMarketplaceTasks(Long childId);
  List<TaskDTO> getPickedTasks(Long childId);
  ```

  **Must NOT do**:
  - Do NOT allow child to pick tasks from other families (validate parent.id)
  - Do NOT allow picking already picked tasks (validate pickedByChild == null)
  - Do NOT allow unpicking other children's tasks (validate ownership)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Service methods follow existing business logic patterns
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 3)
  - **Blocks**: Task 5 (controller uses service methods)
  - **Blocked By**: Tasks 1, 2, 3 (entity, DTO, repository dependencies)

  **References**:
  - **Pattern References**:
    - `src/main/java/com/creditapp/service/impl/TaskServiceImpl.java:36-67` - createTask pattern for pickTask validation
    - `src/main/java/com/creditapp/service/impl/TaskServiceImpl.java:434-466` - toDTO() builder pattern
    - `src/main/java/com/creditapp/service/impl/TaskServiceImpl.java:128-138` - getTasksByChild pattern

  **Acceptance Criteria**:
  - [ ] Test: Verify TaskService and TaskServiceImpl compile: `mvn clean compile`
  - [ ] Verify: pickTask sets pickedByChild and validates parent ownership
  - [ ] Verify: unpickTask validates ownership and clears pickedByChild
  - [ ] Verify: toDTO() includes pickedByChildId and pickedByChildName in Builder

  **Commit**: YES
  - Message: `feat(service): add marketplace pick/unpick methods`
  - Files: `src/main/java/com/creditapp/service/TaskService.java`, `src/main/java/com/creditapp/service/impl/TaskServiceImpl.java`
  - Pre-commit: `mvn clean compile`

- [ ] 5. Add marketplace controller endpoints to ViewController

  **What to do**:
  - Add GET endpoint `/child/marketplace`:
    - Get current user (child)
    - Get child's parent ID
    - Query marketplace tasks via taskService.getMarketplaceTasks(childId)
    - Add tasks, username, childPoints to model
    - Return "child/marketplace"
  - Add POST endpoint `/child/marketplace/{taskId}/pick`:
    - Get child ID from UserDetails
    - Call taskService.pickTask(taskId, childId)
    - Redirect to /child/marketplace with success message
  - Add POST endpoint `/child/marketplace/{taskId}/unpick`:
    - Get child ID from UserDetails
    - Call taskService.unpickTask(taskId, childId)
    - Redirect to /child/marketplace with success message

  **Exact Controller Endpoints**:
  ```java
  @GetMapping("/child/marketplace")
  public String childMarketplace(@AuthenticationPrincipal UserDetails userDetails, Model model);

  @PostMapping("/child/marketplace/{taskId}/pick")
  public String pickMarketplaceTask(@PathVariable Long taskId,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes redirectAttrs);

  @PostMapping("/child/marketplace/{taskId}/unpick")
  public String unpickMarketplaceTask(@PathVariable Long taskId,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     RedirectAttributes redirectAttrs);
  ```

  **Must NOT do**:
  - Do NOT allow children to access marketplace of other families (already filtered by service)
  - Do NOT forget flash messages (success/error feedback)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Controller endpoints follow existing MVC pattern
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 6, 7)
  - **Blocks**: Task 7 (template depends on controller endpoint)
  - **Blocked By**: Tasks 3, 4 (repository and service dependencies)

  **References**:
  - **Pattern References**:
    - `src/main/java/com/creditapp/controller/ViewController.java:320-374` - childTasks endpoint pattern
    - `src/main/java/com/creditapp/controller/ViewController.java:400-431` - createDraftTask POST pattern
    - `src/main/java/com/creditapp/controller/ViewController.java:377-397` - withdrawCompletion POST pattern

  **Acceptance Criteria**:
  - [ ] Test: Verify ViewController compiles: `mvn clean compile`
  - [ ] Verify: /child/marketplace endpoint exists and returns marketplace tasks
  - [ ] Verify: pick endpoint calls pickTask and redirects correctly
  - [ ] Verify: unpick endpoint calls unpickTask and redirects correctly

  **Commit**: YES
  - Message: `feat(controller): add marketplace browsing and pick/unpick endpoints`
  - Files: `src/main/java/com/creditapp/controller/ViewController.java`
  - Pre-commit: `mvn clean compile`

- [ ] 6. Modify TaskServiceImpl.getTasksByChild to include picked tasks

  **What to do**:
  - Modify repository query `findActiveTasksWithChild`:
    - Change WHERE clause from `AND t.assignedChild.id = :childId`
    - To `AND (t.assignedChild.id = :childId OR t.pickedByChild.id = :childId)`
  - Ensure JOIN FETCH handles both assignedChild and pickedByChild

  **Must NOT do**:
  - Do NOT return duplicate tasks (same task in both assigned and picked)
  - Do NOT break existing functionality for assigned tasks

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Repository query modification follows existing JPQL patterns
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 7)
  - **Blocks**: None (independent from 5 and 7)
  - **Blocked By**: Task 3 (repository method must exist)

  **References**:
  - **Pattern References**:
    - `src/main/java/com/creditapp/repository/TaskRepository.java:19-20` - findActiveTasksWithChild query

  **Acceptance Criteria**:
  - [ ] Test: Verify repository compiles: `mvn clean compile`
  - [ ] Verify: Query returns both assigned AND picked tasks
  - [ ] Test: Verify childTasks page shows both task types
  - [ ] Test: Run existing tests: `mvn test -Dtest=TaskServiceTest#getTasksByChild_ShouldReturnTaskDTOList`

  **Commit**: YES
  - Message: `fix(repository): include picked tasks in child's task list`
  - Files: `src/main/java/com/creditapp/repository/TaskRepository.java`
  - Pre-commit: `mvn clean compile`

- [ ] 7. Create child/marketplace.html template

  **What to do**:
  - Create new Thymeleaf template at `src/main/resources/templates/child/marketplace.html`
  - Include: Bootstrap 5, Animate.css, Bootstrap Icons
  - Sections:
    1. Navigation bar (same as child/tasks.html)
    2. Stats cards (current points, available tasks count)
    3. Marketplace tasks grid (card layout with "Pick Task" button)
    4. My Picked Tasks section (list of picked tasks with "Unpick" button)
    5. Empty state if no tasks available
  - JavaScript: AJAX for pick/unpick operations with success messages

  **Frontend Reference Pattern** (from child/tasks.html):
  ```html
  <!-- Navigation -->
  <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
      <a class="nav-link active" href="/child/marketplace">
          <i class="bi bi-shop"></i> 任务市场
      </a>
  </nav>

  <!-- Task Cards -->
  <div th:each="task, iterStat : ${marketplaceTasks}"
       class="col-md-6 mb-4 animate__animated animate__fadeInUp">
      <div class="card task-card h-100">
          <span class="badge points-badge" th:text="${task.points + ' 积分'}">10 积分</span>
          <button class="btn btn-primary" th:attr="data-task-id=${task.id}">
              <i class="bi bi-plus-circle"></i> 挑选任务
          </button>
      </div>
  </div>
  ```

  **Must NOT do**:
  - Do NOT create complex filtering/search UI (keep simple list)
  - Do NOT change overall design language (use Chinese, existing colors)

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: Frontend template creation requires UI/UX skills
  - **Skills**: `frontend-ui-ux`
    - `frontend-ui-ux`: Designer-turned-developer who crafts stunning UI/UX even without design mockups
  - **Skills Evaluated but Omitted**:
    - `dev-browser`: Not needed, creating template file (not browser testing)

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 6)
  - **Blocks**: None (independent from 5 and 6)
  - **Blocked By**: Task 5 (controller endpoint must exist)

  **References**:
  - **Template References**:
    - `src/main/resources/templates/child/tasks.html:1-449` - Complete template structure
    - `src/main/resources/templates/child/tasks.html:74-111` - Navigation pattern
    - `src/main/resources/templates/child/tasks.html:232-287` - Task card pattern
    - `src/main/resources/templates/child/tasks.html:400-447` - JavaScript AJAX pattern

  **Acceptance Criteria**:
  - [ ] Test: Template renders correctly at /child/marketplace
  - [ ] Verify: Marketplace tasks displayed in card grid
  - [ ] Verify: "Pick Task" buttons have data-task-id attribute
  - [ ] Verify: "Unpick" buttons show for picked tasks
  - [ ] Verify: JavaScript makes fetch() calls to endpoints
  - [ ] Verify: Success/error alerts display after pick/unpick

  **Commit**: YES
  - Message: `feat(frontend): add child marketplace browsing page`
  - Files: `src/main/resources/templates/child/marketplace.html`
  - Pre-commit: `mvn clean compile`

- [ ] 8. Update parent task creation form to support marketplace

  **What to do**:
  - Modify `src/main/resources/templates/parent/tasks.html`:
    - Add checkbox: "Available in Marketplace" (optional)
    - Make assignedChild dropdown optional (add "No assignment" option with value="")
    - Update form submission to include marketplace flag
  - Modify ViewController.createTask() POST handler:
    - Check if "marketplace" checkbox is checked
    - If checked and no child selected → Create task with assignedChild = null (marketplace task)
    - If child selected → Create task with assignedChild (existing behavior)

  **Must NOT do**:
  - Do NOT make assignedChild required (must allow null for marketplace)
  - Do NOT break existing manual assignment flow

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Form modification follows existing Thymeleaf patterns
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 9, 10, 11)
  - **Blocks**: None (independent from other tasks)
  - **Blocked By**: None (can start after Waves 1-2)

  **References**:
  - **Template References**:
    - `src/main/resources/templates/parent/tasks.html:156-165` - Child assignment dropdown
    - `src/main/java/com/creditapp/controller/ViewController.java:107-160` - createTask POST handler

  **Acceptance Criteria**:
  - [ ] Test: Parent can create task without selecting child
  - [ ] Test: Parent can create task with "Available in Marketplace" checked
  - [ ] Verify: Task appears in child's marketplace when created
  - [ ] Test: Existing manual assignment still works

  **Commit**: YES
  - Message: `feat(frontend): add marketplace option to parent task creation`
  - Files: `src/main/resources/templates/parent/tasks.html`, `src/main/java/com/creditapp/controller/ViewController.java`
  - Pre-commit: `mvn clean compile`

- [ ] 9. Update child navigation to include marketplace link

  **What to do**:
  - Modify all child templates (child/tasks.html, child/rewards.html, child/marketplace.html):
    - Add nav item: "任务市场" with icon bi-shop
    - Link to `/child/marketplace`
  - Ensure active state highlights correctly on each page

  **Must NOT do**:
  - Do NOT remove existing navigation items (My Tasks, My Drafts, Gift Store)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Navigation update is simple HTML change
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 8, 10, 11)
  - **Blocks**: None (independent)
  - **Blocked By**: Task 7 (marketplace.html must exist)

  **References**:
  - **Navigation References**:
    - `src/main/resources/templates/child/tasks.html:83-103` - Current navigation structure

  **Acceptance Criteria**:
  - [ ] Verify: "任务市场" link appears in child navbar
  - [ ] Verify: Link navigates to /child/marketplace
  - [ ] Verify: Active state highlights correctly on each page

  **Commit**: YES
  - Message: `feat(frontend): add marketplace link to child navigation`
  - Files: `src/main/resources/templates/child/tasks.html`, `src/main/resources/templates/child/rewards.html`, `src/main/resources/templates/child/marketplace.html`
  - Pre-commit: `mvn clean compile`

- [ ] 10. Write comprehensive tests for marketplace functionality

  **What to do**:
  - Create new test file: `src/test/java/com/creditapp/service/TaskServiceMarketplaceTest.java`
  - Test cases to implement:
    1. `pickTask_ShouldSetPickedByChild` - Verify pick sets pickedByChild
    2. `pickTask_ShouldValidateParentOwnership` - Verify can't pick from other families
    3. `pickTask_ShouldNotPickAlreadyPickedTask` - Verify first-come-first-served
    4. `unpickTask_ShouldClearPickedByChild` - Verify unpick clears field
    5. `unpickTask_ShouldValidateOwnership` - Verify can only unpick own tasks
    6. `getMarketplaceTasks_ShouldReturnParentTasksOnly` - Verify family isolation
    7. `getPickedTasks_ShouldReturnChildPickedTasks` - Verify picked task retrieval
    8. `getTasksByChild_ShouldIncludePickedTasks` - Verify both assigned + picked in list
    9. `DAILY_ONCE_PickedTask_ShouldEnforceLimit` - Verify DAILY_ONCE works for picked tasks

  **Test Pattern** (from TaskServiceTest.java):
  ```java
  @Test
  void pickTask_ShouldSetPickedByChild() {
      // Given
      Task task = new Task();
      task.setId(1L);
      task.setStatus(TaskStatus.APPROVED);
      task.setCreatedBy(parentUser);
      task.setAssignedChild(null); // Unassigned task
      task.setPickedByChild(null);

      when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
      when(childRepository.findById(2L)).thenReturn(Optional.of(childUser));
      when(childRepository.findByParentId(1L)).thenReturn(Arrays.asList(childUser));
      when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

      // When
      TaskDTO result = taskService.pickTask(1L, 2L);

      // Then
      assertThat(result.getPickedByChildId()).isEqualTo(2L);
      verify(taskRepository).save(any(Task.class));
  }
  ```

  **Must NOT do**:
  - Do NOT use @ExtendWith(MockitoExtension.class) - already in existing tests, follow pattern
  - Do NOT skip edge cases (DAILY_ONCE limit, family isolation)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Test writing follows existing given-when-then pattern
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 8, 9, 11)
  - **Blocks**: None (independent)
  - **Blocked By**: Tasks 4, 5, 6 (service implementation must exist)

  **References**:
  - **Test References**:
    - `src/test/java/com/creditapp/service/TaskServiceTest.java:33-142` - Test structure and setup patterns
    - `src/test/java/com/creditapp/service/TaskServiceTest.java:415-485` - DAILY_ONCE test patterns

  **Acceptance Criteria**:
  - [ ] Test: All 9 new tests pass: `mvn test -Dtest=TaskServiceMarketplaceTest`
  - [ ] Test: All 19 existing tests still pass: `mvn test`
  - [ ] Verify: DAILY_ONCE limit test covers picked tasks scenario

  **Commit**: YES
  - Message: `test(marketplace): add comprehensive marketplace tests`
  - Files: `src/test/java/com/creditapp/service/TaskServiceMarketplaceTest.java`
  - Pre-commit: `mvn clean test`

- [ ] 11. Run full test suite and verify build

  **What to do**:
  - Run clean compile: `mvn clean compile`
  - Run all tests: `mvn test`
  - Verify all 28 tests pass (19 existing + 9 new)
  - Check for any warnings or errors
  - Package application: `mvn clean package`

  **Must NOT do**:
  - Do NOT proceed if any tests fail
  - Do NOT skip tests: use -DskipTests only after confirming all pass

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Running Maven commands is straightforward
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: NO (sequential - final verification)
  - **Parallel Group**: Sequential (final task)
  - **Blocks**: None (final task)
  - **Blocked By**: Tasks 1-10 (all implementation must complete)

  **References**:
  - **Build Commands**:
    - `mvn clean compile` - Verify compilation
    - `mvn test` - Run all tests
    - `mvn clean package` - Create JAR file

  **Acceptance Criteria**:
  - [ ] `mvn clean compile` → BUILD SUCCESS
  - [ ] `mvn test` → 28 tests run, 0 failures
  - [ ] `mvn clean package` → BUILD SUCCESS

  **Commit**: NO (final task, no code changes)

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 1 | `feat(entity): add pickedByChild field to Task` | Task.java | `mvn clean compile` |
| 2 | `feat(dto): add pickedByChild fields to TaskDTO` | TaskDTO.java | `mvn clean compile` |
| 3 | `feat(repository): add marketplace query methods` | TaskRepository.java | `mvn clean compile` |
| 4 | `feat(service): add marketplace pick/unpick methods` | TaskService.java, TaskServiceImpl.java | `mvn clean compile` |
| 5 | `feat(controller): add marketplace endpoints` | ViewController.java | `mvn clean compile` |
| 6 | `fix(repository): include picked tasks in child list` | TaskRepository.java | `mvn clean compile` |
| 7 | `feat(frontend): add marketplace page` | child/marketplace.html | `mvn clean compile` |
| 8 | `feat(frontend): add marketplace option to parent form` | parent/tasks.html, ViewController.java | `mvn clean compile` |
| 9 | `feat(frontend): add marketplace link to nav` | child/*.html | `mvn clean compile` |
| 10 | `test(marketplace): add comprehensive tests` | TaskServiceMarketplaceTest.java | `mvn test` |
| 11 | - (verification only) | - | `mvn test` |

---

## Success Criteria

### Verification Commands
```bash
# Compile check
mvn clean compile
# Expected: BUILD SUCCESS

# Run all tests
mvn test
# Expected: 28 tests run, 0 failures, 0 errors

# Package check
mvn clean package
# Expected: BUILD SUCCESS, JAR file created in target/
```

### Final Checklist
- [ ] All "Must Have" features implemented
- [ ] All "Must NOT Have" guardrails respected
- [ ] All 19 existing tests pass
- [ ] All 9 new marketplace tests pass
- [ ] Build succeeds (clean compile + package)
- [ ] Child can browse marketplace at `/child/marketplace`
- [ ] Child can pick task from marketplace
- [ ] Picked task appears in `/child/tasks`
- [ ] Child can complete picked task
- [ ] Parent approves picked task completion
- [ ] DAILY_ONCE limit works for picked tasks
- [ ] Parent can create marketplace-available task
- [ ] Tasks from other families not visible
