# Draft: Parent Marketplace Feature Implementation

## User Request

Implement parent marketplace functionality so parents can:
1. View the task market and see tasks they've published
2. See which children have picked their tasks
3. Have a dedicated marketplace management page

## Current State Analysis

**Already Implemented (Child Side):**
- Task entity has `pickedByChild` field
- TaskRepository has `findAvailableMarketplaceTasksByParentId()` query
- TaskService has `getMarketplaceTasks(Long childId)` method
- ViewController has `GET /child/marketplace` endpoint
- child/marketplace.html frontend template exists

**Missing for Parents:**
1. Service: No `getMarketplaceTasksByParent(Long parentId)` method
2. Controller: No `GET /parent/marketplace` endpoint
3. Frontend: No `parent/marketplace.html` template
4. Navigation: No "任务市场" link in parent's nav

## Key Questions to Resolve

### Business Logic
- Should parents be able to edit tasks that are currently in the marketplace?
- Should parents be able to delete tasks from the marketplace?
- Should parents be able to "unassign" picked tasks (force return to market)?

### UI Features
- What statistics should be shown on the parent marketplace page?
- Should parents see tasks that are available vs. tasks already picked?
- What actions should parents have on marketplace tasks?

### Implementation Approach
- Should parent view show all marketplace tasks (available + picked) or separate sections?
- Should there be filtering options (by status, by child, by date)?

## Complete Research Findings

### Service Layer Patterns (from bg_9738b36e)
- TaskService interface: Simple method signatures, naming conventions like `getMarketplaceTasks`, `getPickedTasks`
- TaskServiceImpl: Uses `@Service`, `@RequiredArgsConstructor`, `@Transactional`
- Read-only methods use `@Transactional(readOnly = true)`
- Pattern: Fetch parent/child from user, then query repository
- DTOs: Use Builder pattern `TaskDTO.builder().id().title()...build()`
- Error handling: ResourceNotFoundException for missing resources, BusinessException for business logic

### Controller Layer Patterns (from bg_b4b36fde)
- ViewController: Uses `@Controller`, `@RequiredArgsConstructor`
- Pattern: `@GetMapping("/path")` with Model parameter
- Use `@AuthenticationPrincipal UserDetails` to get current user
- Security: URL-based in SecurityConfig (no @PreAuthorize annotations)
- Return template name as String ("child/marketplace")
- Model attributes: `username`, `tasks`, `children`, `pendingCompletions`, etc.
- RedirectAttributes for flash messages after POST

### Frontend Patterns (child/marketplace.html) (from bg_5ff9ef13)
- Layout: Stats cards + Marketplace tasks grid + Picked tasks list
- Styling: Bootstrap 5, Bootstrap Icons, Animate.css
- Color variables defined in `:root` CSS
- Card layout: Grid with `col-md-6`, animation delays, hover effects
- AJAX pattern: `fetch()` with JSON response, button state management
- Empty state: Large icon + helpful message
- **IMPORTANT NOTE**: Template calls `/api/v1/marketplace/` endpoints that don't exist - child uses redirect-based endpoints instead

### Parent Navigation (from bg_77aca292)
- Location: Each template has its own navbar (NO shared layout)
- Current items: 首页, 任务管理, 礼物管理, 任务审批, 小孩管理, 草稿审批, 通知中心
- Styling: Bootstrap navbar classes, icon + text pattern
- Icon for marketplace: `bi-shop` or `bi-cart4`
- Need to update 8 template files to add new navigation link

### Repository Queries Available
- `findAvailableMarketplaceTasksByParentId(parentId)` - Already exists!
  - Filters: assignedChild IS NULL, pickedByChild IS NULL, active=true, status='APPROVED'
  - Perfect for parent to see their marketplace tasks

## Open Questions (User Input Needed)

### Business Logic
1. **Parent Marketplace View**: Should the parent view show:
   - Only marketplace tasks that are still available (not picked)?
   - Or include tasks that have been picked (showing which child picked them)?
   - Or both in separate sections?

2. **Parent Actions**: What actions should parents have on marketplace tasks?
   - Remove from marketplace (delete)?
   - Force return to market (if picked)?
   - Edit marketplace tasks?
   - No actions (view-only)?

3. **Statistics**: What stats should the parent marketplace page show?
   - Total marketplace tasks?
   - Tasks picked by children?
   - Most popular tasks?
   - Simple counts only?
