# Parent Marketplace Feature Implementation

## TL;DR

> **Quick Summary**: Add parent marketplace management page with separate sections for available and picked tasks, edit capability, and statistics display
>
> **Deliverables**:
> - New service method `getMarketplaceTasksByParent(Long parentId)` returning parent's marketplace tasks
> - New controller endpoint `GET /parent/marketplace` with statistics data
> - New template `parent/marketplace.html` with two-section layout (available + picked)
> - Navigation link "任务市场" added to 8 parent template files
> - Edit functionality for marketplace tasks
>
> **Estimated Effort**: Medium
> **Parallel Execution**: YES - 2 waves
> **Critical Path**: Service layer → Controller → Template → Navigation updates

---

## Context

### Original Request
Implement parent marketplace functionality so parents can:
1. View the task market and see tasks they've published
2. See which children have picked their tasks
3. Have a dedicated marketplace management page

### Interview Summary
**Key Discussions**:
- View scope: Separate sections - "Available Tasks" and "Picked Tasks" (like child view)
- Parent actions: Edit marketplace task details (in addition to viewing)
- Statistics: Simple counts (Total tasks, Available tasks, Picked tasks)
- Navigation position: Between "任务管理" and "礼物管理"

**Research Findings**:
- TaskRepository.findAvailableMarketplaceTasksByParentId() already exists - can be reused
- Child marketplace.html provides complete UI patterns to adapt (grid layout, cards, Bootstrap 5, Animate.css)
- ViewController patterns: @GetMapping with Model, @AuthenticationPrincipal for auth, returns template name
- Parent navigation is inline in each template (8 files need updates)
- TaskService patterns: @Service, @RequiredArgsConstructor, @Transactional, DTO builders

### User Preferences Confirmed
1. **View Scope**: Option C - Separate sections for "Available Tasks" and "Picked Tasks"
2. **Parent Actions**: Option D - Edit marketplace task details
3. **Statistics**: Option A - Simple counts (Total, Available, Picked)
4. **Navigation Position**: Option A - Between "任务管理" and "礼物管理"

---

## Work Objectives

### Core Objective
Create a parent marketplace management page that allows parents to view, manage, and edit their marketplace tasks with visibility into which children have picked tasks.

### Concrete Deliverables
- `TaskService.getMarketplaceTasksByParent(Long parentId)` - New service method
- `GET /parent/marketplace` - New controller endpoint
- `parent/marketplace.html` - New template file
- "任务市场" navigation link added to 8 parent templates

### Definition of Done
- [ ] Parent marketplace page displays available and picked tasks in separate sections
- [ ] Statistics show total, available, and picked task counts
- [ ] Edit functionality works for marketplace tasks
- [ ] Navigation link appears in all parent pages
- [ ] All tasks compile without errors
- [ ] mvn test passes all existing tests

### Must Have
- Two-section layout: "Available Tasks" (not picked) and "Picked Tasks" (with child info)
- Edit button on each marketplace task linking to existing edit endpoint
- Statistics cards showing task counts
- Parent marketplace link in navigation bar

### Must NOT Have (Guardrails)
- DO NOT create new Task entity or modify existing Task entity
- DO NOT change existing marketplace behavior for children
- DO NOT add delete functionality to parent marketplace (only edit)
- DO NOT modify child marketplace template or controller
- DO NOT create separate navigation layout file (update existing inline navs)
- DO NOT change existing task type or status enums

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: YES (mvn test)
- **User wants tests**: Tests-after (focus on manual verification)
- **Framework**: Existing Spring Boot tests

### Automated Verification (NO User Intervention)

Each TODO includes EXECUTABLE verification procedures:

**For Service Layer Changes**:
```bash
# Agent runs:
mvn test -Dtest=TaskServiceTest
# Assert: All existing tests pass (19 tests)
```

**For Controller Layer Changes**:
```bash
# Agent runs:
mvn test -Dtest=ViewControllerTaskTest
# Assert: All existing tests pass
```

**For Frontend Template**:
```bash
# Agent runs:
curl -s http://localhost:8080/parent/marketplace -I -u parent:parent123 | head -n 20
# Assert: HTTP status 200, HTML content returned
```

**For Compilation**:
```bash
# Agent runs:
mvn clean compile
# Assert: BUILD SUCCESS, no compilation errors
```

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately - All independent):
├── Task 1: Add getMarketplaceTasksByParent() to TaskService interface
├── Task 2: Add findPickedMarketplaceTasksByParentId() query to TaskRepository
├── Task 3: Implement getMarketplaceTasksByParent() in TaskServiceImpl
├── Task 4: Add GET /parent/marketplace endpoint to ViewController
└── Task 5: Create parent/marketplace.html template

Wave 2 (After Wave 1 - needs template created):
└── Task 6: Add "任务市场" navigation link to parent template files

Critical Path: Task 1 → Task 2 → Task 3 → Task 4 → Task 5 → Task 6
Parallel Speedup: ~40% faster than sequential
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|-------------|---------|---------------------|
| 1 | None | 2 | None |
| 2 | None | 3 | 1 |
| 3 | 1, 2 | 4 | 1, 2 |
| 4 | None | 5 | 1, 2, 3 |
| 5 | None | 6 | 1, 2, 3, 4 |
| 6 | 4, 5 | None | None (final task) |

### Agent Dispatch Summary

| Wave | Tasks | Recommended Agents |
|-------|--------|-------------------|
| 1 | 1, 2, 3, 4, 5 | delegate_task(category="quick", load_skills=[], run_in_background=true) |
| 2 | 6 | delegate_task(category="quick", load_skills=[], run_in_background=true) |

---

## TODOs

> Implementation + Test = ONE Task. Never separate.

- [ ] 1. Add getMarketplaceTasksByParent() to TaskService interface

- [ ] 2. Add findPickedMarketplaceTasksByParentId() query to TaskRepository

  **What to do**:
  - Add method signature to `src/main/java/com/creditapp/service/TaskService.java`
  - Return type: `List<TaskDTO>`
  - Parameter: `Long parentId`
  - Place after existing `getPickedTasks(Long childId)` method

  **Must NOT do**:
  - Do NOT modify existing method signatures
  - Do NOT change return types of existing methods

  **Recommended Agent Profile**:
  > Select category + skills based on task domain. Justify each choice.
  - **Category**: `quick`
    - Reason: Simple interface method addition following existing patterns, no complex logic
  - **Skills**: `[]`
    - None needed - straightforward file addition

   **Parallelization**:
   - **Can Run In Parallel**: YES
   - **Parallel Group**: Wave 1 (with Tasks 1, 2, 3, 4, 5)
   - **Blocks**: None (all tasks independent)
   - **Blocked By**: None (can start immediately)

  **References** (CRITICAL - Be Exhaustive):

  > The executor has NO context from your interview. References are their ONLY guide.
  > Each reference must answer: "What should I look at and WHY?"

  **Pattern References** (existing code to follow):
  - `src/main/java/com/creditapp/service/TaskService.java:10-47` - Interface method patterns (getMarketplaceTasks, getPickedTasks follow naming convention)
  - `src/main/java/com/creditapp/service/TaskService.java:45-46` - Method signature pattern with childId parameter (replicate with parentId)

  **API/Type References** (contracts to implement against):
  - N/A (new method definition)

  **Test References** (testing patterns to follow):
  - `src/test/java/com/creditapp/service/TaskServiceTest.java` - Service test structure (should add tests for new method)

  **Documentation References** (specs and requirements):
  - `AGENTS.md` - Project conventions and code style guide

  **External References** (libraries and frameworks):
  - Spring Service interface patterns (standard Java interface methods)

  **WHY Each Reference Matters** (explain the relevance):
  - TaskService.java:10-47: Shows exact method signature pattern - return type, parameter name, placement in interface
  - TaskService.java:45-46: Demonstrates naming convention for similar methods (getMarketplaceTasks, getPickedTasks)

  **Acceptance Criteria**:

  > **CRITICAL: AGENT-EXECUTABLE VERIFICATION ONLY**

  **If TDD (tests enabled):**
  - [ ] Test file created: src/test/java/com/creditapp/service/TaskServiceMarketplaceTest.java
  - [ ] Test covers: getMarketplaceTasksByParent returns correct list
  - [ ] mvn test src/test/java/com/creditapp/service/TaskServiceMarketplaceTest.java → PASS (1 test, 0 failures)

  **Automated Verification (ALWAYS include, choose by deliverable type):**

  **For Service Interface Changes** (using Bash mvn test):
  ```bash
  # Agent runs:
  mvn clean compile
  # Assert: BUILD SUCCESS
  # Assert: No compilation errors
  ```

  **Evidence to Capture**:
  - [ ] Terminal output from mvn clean compile (should show BUILD SUCCESS)
  - [ ] Any warnings should be informational only

   **Commit**: YES
   - Message: `feat(service): implement getMarketplaceTasksByParent in TaskServiceImpl`
   - Files: `src/main/java/com/creditapp/service/impl/TaskServiceImpl.java`
   - Pre-commit: `mvn test -Dtest=TaskServiceTest`

---

- [ ] 4. Add GET /parent/marketplace endpoint to ViewController

  **What to do**:
  - Create file `src/main/resources/templates/parent/marketplace.html`
  - Copy structure from `child/marketplace.html` but adapt for parents
  - Add parent navigation bar with all parent links (include "任务市场" as active)
  - **Section 1: Statistics Cards** - Show 3 cards: Total Tasks, Available Tasks, Picked Tasks
  - **Section 2: Available Tasks** - Grid of cards for tasks where `task.pickedByChildId IS NULL`
  - **Section 3: Picked Tasks** - List showing tasks where `task.pickedByChildId NOT NULL` with child name
  - Add edit button linking to `/parent/tasks/{id}/edit` for each task
  - Use Bootstrap 5 classes, Bootstrap Icons, Animate.css animations
  - Add empty states with helpful messages for each section
  - Include footer with copyright

  **Must NOT do**:
  - Do NOT add pick/unpick functionality (parent only views and edits)
  - Do NOT modify child marketplace template
  - Do NOT create separate navigation fragment (use inline nav)

  **Recommended Agent Profile**:
  > Select category + skills based on task domain. Justify each choice.
  - **Category**: `visual-engineering`
    - Reason: Requires creating a visually appealing parent marketplace page with two sections, grid layouts, cards, and following existing UI/UX patterns
  - **Skills**: [`frontend-ui-ux`]
    - `frontend-ui-ux`: Designer-turned-developer who crafts stunning UI/UX even without design mockups - needed to adapt child marketplace template for parent view while maintaining design consistency

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 3)
  - **Blocks**: Task 5 (navigation updates need template to exist)
  - **Blocked By**: None (can start immediately)

  **References** (CRITICAL - Be Exhaustive):

  > The executor has NO context from your interview. References are their ONLY guide.
  > Each reference must answer: "What should I look at and WHY?"

  **Pattern References** (existing code to follow):
  - `src/main/resources/templates/child/marketplace.html:1-356` - Complete template structure (nav, stats, grid layout, cards, empty states, footer)
  - `src/main/resources/templates/child/marketplace.html:134-154` - Stats cards pattern (2-column layout, icons, animated cards)
  - `src/main/resources/templates/child/marketplace.html:156-208` - Available tasks grid pattern (card layout, type badges, pick buttons to edit)
  - `src/main/resources/templates/child/marketplace.html:210-257` - Picked tasks list pattern (row layout with child info)
  - `src/main/resources/templates/parent/tasks.html:66-110` - Parent navigation bar structure (all parent links)

  **API/Type References** (contracts to implement against):
  - `src/main/java/com/creditapp/controller/ViewController.java` - Controller passes model attributes (marketplaceTasks, availableCount, pickedCount, totalTasks, username)

  **Test References** (testing patterns to follow):
  - Manual browser verification for frontend templates

  **Documentation References** (specs and requirements):
  - `AGENTS.md` - Thymeleaf template syntax and Bootstrap 5 patterns
  - `AGENTS.md` - Animate.css usage guidelines

  **External References** (libraries and frameworks):
  - Bootstrap 5 documentation for grid system and card components
  - Thymeleaf documentation for th:if, th:each, th:text, th:href syntax

  **WHY Each Reference Matters** (explain the relevance):
  - child/marketplace.html:1-356: Complete template to adapt - shows nav structure, stats layout, grid pattern, card styling, empty states, JavaScript patterns
  - child/marketplace.html:134-154: Stats cards show how to display 2 or 3-column statistics with icons and counts
  - child/marketplace.html:156-208: Available tasks grid shows card layout with title, description, points badge, type badge, and action button (edit for parent)
  - child/marketplace.html:210-257: Picked tasks list shows row-based layout with child name, task info, and action
  - parent/tasks.html:66-110: Parent navigation shows exact link structure with icons to replicate

  **Acceptance Criteria**:

  > **CRITICAL: AGENT-EXECUTABLE VERIFICATION ONLY**

  **If TDD (tests enabled):**
  - N/A (frontend templates manually verified)

  **Automated Verification (ALWAYS include, choose by deliverable type):**

  **For Frontend Template Changes** (using Bash curl):
  ```bash
  # Agent runs:
  mvn spring-boot:run
  # Wait 10s for startup
  curl -s -I http://localhost:8080/parent/marketplace | head -n 5
  # Assert: HTTP status 200
  ```

  **Evidence to Capture**:
  - [ ] Terminal output from curl (should show HTTP/1.1 200)
  - [ ] Application startup logs (should show "Started Application in X.XXX seconds")

  **Commit**: YES
  - Message: `feat(frontend): add parent/marketplace.html template with two-section layout`
  - Files: `src/main/resources/templates/parent/marketplace.html`
  - Pre-commit: `mvn clean compile`

---

- [ ] 5. Add "任务市场" navigation link to parent templates

  **What to do**:
  - Add navigation link to 8 parent template files:
    1. `src/main/resources/templates/parent/tasks.html` (after line 86)
    2. `src/main/resources/templates/parent/rewards.html` (after task management link)
    3. `src/main/resources/templates/parent/approvals.html` (after line 96)
    4. `src/main/resources/templates/parent/children.html` (after line 101)
    5. `src/main/resources/templates/parent/notifications.html` (after line 101)
    6. `src/main/resources/templates/parent/drafts.html` (after line 77)
    7. `src/main/resources/templates/parent/edit-task.html` (after line 70)
    8. `src/main/resources/templates/parent/child-details.html` (after existing nav items)
  - Add `<li class="nav-item"><a class="nav-link" href="/parent/marketplace"><i class="bi bi-shop"></i> 任务市场</a></li>`
  - Position: After "任务管理" link, before "礼物管理" link
  - Follow existing Bootstrap classes and icon patterns

  **Must NOT do**:
  - Do NOT create shared navigation layout file
  - Do NOT modify child navigation
  - Do NOT change link order of existing navigation items

  **Recommended Agent Profile**:
  > Select category + skills based on task domain. Justify each choice.
  - **Category**: `quick`
    - Reason: Simple navigation link addition to multiple template files, no complex logic
  - **Skills**: `[]`
    - None needed - straightforward HTML additions following existing patterns

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (after Task 4)
  - **Blocks**: None
  - **Blocked By**: Task 4 (parent/marketplace.html needs to exist first)

  **References** (CRITICAL - Be Exhaustive):

  > The executor has NO context from your interview. References are their ONLY guide.
  > Each reference must answer: "What should I look at and WHY?"

  **Pattern References** (existing code to follow):
  - `src/main/resources/templates/parent/tasks.html:76-107` - Parent navigation bar structure (navbar structure, link classes, icon patterns)
  - `src/main/resources/templates/parent/approvals.html:76-110` - Parent navigation in approvals page
  - `src/main/resources/templates/child/marketplace.html:100-102` - Marketplace link pattern (uses bi-cart4 icon for reference, parent should use bi-shop)

  **API/Type References** (contracts to implement against):
  - N/A (navigation link only)

  **Test References** (testing patterns to follow):
  - Manual browser verification for navigation links

  **Documentation References** (specs and requirements):
  - `AGENTS.md` - Template modification guidelines

  **External References** (libraries and frameworks):
  - Bootstrap 5 navbar documentation

  **WHY Each Reference Matters** (explain the relevance):
  - parent/tasks.html:76-107: Shows exact navigation HTML structure with navbar classes, nav-link classes, and icon patterns to replicate
  - parent/approvals.html:76-110: Confirms navigation structure is consistent across parent templates
  - child/marketplace.html:100-102: Shows marketplace icon pattern (bi-cart4) - parent should use bi-shop for consistency with parent tasks page

  **Acceptance Criteria**:

  > **CRITICAL: AGENT-EXECUTABLE VERIFICATION ONLY**

  **If TDD (tests enabled):**
  - N/A (frontend templates manually verified)

  **Automated Verification (ALWAYS include, choose by deliverable type):**

  **For Frontend Template Changes** (using Bash grep):
  ```bash
  # Agent runs:
  grep -n '任务市场' src/main/resources/templates/parent/*.html
  # Assert: Returns 8 matches (all parent templates updated)
  ```

  **Evidence to Capture**:
  - [ ] Terminal output from grep (should show 8 lines with "任务市场")

  **Commit**: YES (groups all 8 files)
  - Message: `feat(frontend): add 任务市场 navigation link to parent templates`
  - Files: `src/main/resources/templates/parent/tasks.html`, `src/main/resources/templates/parent/rewards.html`, `src/main/resources/templates/parent/approvals.html`, `src/main/resources/templates/parent/children.html`, `src/main/resources/templates/parent/notifications.html`, `src/main/resources/templates/parent/drafts.html`, `src/main/resources/templates/parent/edit-task.html`, `src/main/resources/templates/parent/child-details.html`
  - Pre-commit: `mvn clean compile`

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|----------|--------------|
| 1 | `feat(service): add getMarketplaceTasksByParent method to TaskService interface` | `src/main/java/com/creditapp/service/TaskService.java` | `mvn clean compile` |
| 2 | `feat(service): implement getMarketplaceTasksByParent in TaskServiceImpl` | `src/main/java/com/creditapp/service/impl/TaskServiceImpl.java` | `mvn test -Dtest=TaskServiceTest` |
| 3 | `feat(controller): add parentMarketplace endpoint to ViewController` | `src/main/java/com/creditapp/controller/ViewController.java` | `mvn test -Dtest=ViewControllerTaskTest` |
| 4 | `feat(frontend): add parent/marketplace.html template with two-section layout` | `src/main/resources/templates/parent/marketplace.html` | `mvn clean compile` |
| 5 | `feat(frontend): add 任务市场 navigation link to parent templates` | 8 parent template files | `grep -n '任务市场' src/main/resources/templates/parent/*.html` |

---

## Success Criteria

### Verification Commands
```bash
# Compile all code
mvn clean compile
# Expected: BUILD SUCCESS

# Run all service tests
mvn test -Dtest=TaskServiceTest
# Expected: 19 tests passed

# Run all controller tests
mvn test -Dtest=ViewControllerTaskTest
# Expected: All existing tests passed

# Verify navigation link added
grep -n '任务市场' src/main/resources/templates/parent/*.html
# Expected: 8 matches

# Build package
mvn clean package -DskipTests
# Expected: BUILD SUCCESS, jar created
```

### Final Checklist
- [ ] All "Must Have" present
- [ ] All "Must NOT Have" absent
- [ ] Parent marketplace page displays two sections
- [ ] Statistics show correct counts (total, available, picked)
- [ ] Edit buttons link to existing edit endpoint
- [ ] Navigation link appears in all 8 parent templates
- [ ] All tests pass (19 service + existing controller tests)
- [ ] Code compiles without errors
