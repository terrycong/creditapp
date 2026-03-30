# AGENTS.md - 家庭积分任务管理系统

## 项目概述
家庭积分任务管理系统，支持家长创建任务和礼物，小孩完成任务赚取积分并兑换礼物。
技术栈：Spring Boot 3 + Maven + JPA + Thymeleaf + Spring Security + BCrypt + H2(开发)/MySQL(生产)
架构：单体应用，Controller-Service-Repository三层架构

---

## 新增功能需求 (2026-01-24)

### 家长仪表板增强功能

**需求描述**：
家长需要一个增强的仪表板，可以查看：
1. **最近完成的任务** - 每个小孩最近完成了哪些任务
2. **奖励发放情况** - 每个小孩获得了多少奖励（积分发放）
3. **礼物兑换记录** - 每个小孩兑换了什么礼物

### 每日任务限制功能 (已实现)

**需求描述**：
`DAILY_ONCE` 类型的任务每个孩子每天只能完成一次，防止系统滥用。

**具体功能要求**：
1. **每日限制检查**：孩子尝试完成 `DAILY_ONCE` 任务时，系统检查当天是否已有完成记录
2. **状态考虑**：同时检查 `PENDING` 和 `APPROVED` 状态（待审批的任务也计入限制）
3. **错误处理**：如果当天已存在完成记录，抛出 `BusinessException` 并显示友好错误信息
4. **前端反馈**：前端 JavaScript 显示清晰的中文错误提示

**技术实现**：
- **Repository层**：`TaskCompletionRepository.existsCompletionToday()` 方法
- **Service层**：`TaskServiceImpl.completeTask()` 中的每日限制检查逻辑
- **错误处理**：`BusinessException` 错误码 `DAILY_LIMIT_EXCEEDED`
- **前端显示**：`child/tasks.html` 中的 JavaScript 错误处理

**测试覆盖**：
- 首次完成 `DAILY_ONCE` 任务应成功
- 同一天第二次完成 `DAILY_ONCE` 任务应失败并显示正确错误信息
- 非 `DAILY_ONCE` 任务不应检查每日限制

**具体功能要求**：

#### 1. 任务完成追踪
- 显示每个小孩最近完成的任务（按时间倒序）
- 显示任务完成时间、任务名称、获得积分
- 支持查看所有已完成任务或最近7天的任务
- 统计每个小孩的任务完成数量

#### 2. 奖励发放统计
- 显示每个小孩获得的积分奖励总数
- 按时间范围统计积分发放情况（今日、本周、本月）
- 显示积分发放最多的任务类型
- 统计平均每个任务获得的积分

#### 3. 礼物兑换监控
- 显示每个小孩兑换的礼物记录
- 显示兑换时间、礼物名称、消耗积分
- 统计最受欢迎的礼物（兑换次数最多）
- 显示每个小孩的积分消费总额
- 监控礼物库存变化

#### 4. 数据可视化
- 使用图表展示任务完成趋势
- 使用饼图展示积分分配情况
- 使用柱状图展示礼物兑换排行
- 实时数据更新（可选）

**技术实现要求**：
1. 新增Repository查询方法：
   - 按家长ID查询小孩的任务完成记录
   - 按家长ID查询小孩的礼物兑换记录
   - 统计每个小孩的任务完成数量
   - 统计每个小孩的积分消费总额

2. 新增DTO类：
   - `TaskCompletionDTO` - 任务完成记录DTO
   - `RewardRedemptionDTO` - 礼物兑换记录DTO
   - `ChildActivityDTO` - 小孩活动统计DTO
   - `DashboardStatsDTO` - 仪表板统计数据DTO

3. 控制器增强：
   - 在`ViewController.dashboard()`方法中添加数据获取逻辑
   - 新增API端点获取统计数据（可选）

4. 前端模板增强：
   - 在`dashboard.html`中添加新的统计展示区域
   - 使用Thymeleaf模板引擎渲染统计数据
   - 添加响应式设计，确保在移动设备上良好显示

**数据库查询优化**：
- 使用JOIN FETCH避免N+1查询问题
- 添加适当的数据库索引
- 考虑分页查询大量数据
- 使用缓存提高性能（可选）

**权限控制**：
- 只有家长角色可以查看详细统计数据
- 小孩只能看到自己的简单统计
- 数据访问需要经过身份验证

---

## 构建和测试命令

```bash
# 编译项目
mvn clean compile

# 打包项目
mvn clean package

# 运行应用（开发模式）
mvn spring-boot:run

# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=TaskServiceTest

# 运行单个测试方法
mvn test -Dtest=TaskServiceTest#testCreateTask

# 清理并重新编译测试
mvn clean test

# 跳过测试打包
mvn clean package -DskipTests
```

---

## 代码风格指南

### 包结构和导入规范

**包命名约定：**
```
com.creditapp
├── controller      # 控制器层
├── service         # 业务逻辑层
├── repository      # 数据访问层
├── entity          # JPA实体类
├── dto             # 数据传输对象
├── config          # 配置类
├── security        # 安全配置
├── exception       # 自定义异常
└── util            # 工具类
```

**导入顺序：**
1. Java标准库（java.*, javax.*）
2. 第三方库（org.springframework.*, lombok.*）
3. 项目内部包（com.creditapp.*）

### 实体设计规范

**JPA实体标准：**
```java
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private Integer points;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type; // ONE_TIME, REPEATABLE, DAILY_ONCE
    
**任务类型说明**：
- `ONE_TIME`：一次性任务，完成后不可重复完成
- `REPEATABLE`：可重复任务，可以多次完成
- `DAILY_ONCE`：每日一次任务，每个孩子每天只能完成一次（有每日限制检查）
}
```

**关系映射规范：**
- 使用`mappedBy`在双向关联中指定拥有方
- 避免在`toString()`和`equals()/hashCode()`中包含关联字段
- 使用`@JsonIgnore`或`@JsonManagedReference/@JsonBackReference`防止JSON循环引用
- 使用`JOIN FETCH`或`@EntityGraph`解决N+1查询问题

**Lombok使用：**
- `@Data`：简单的DTO类
- `@Builder`：复杂对象构建（配合`@AllArgsConstructor`和`@NoArgsConstructor`）
- `@RequiredArgsConstructor`：Service类（用于依赖注入）
- **禁止**：`@Data`用于JPA实体（手动实现`equals()`/`hashCode()`）

### Controller层规范

**RESTful API控制器：**
```java
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "任务管理API")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ApiResponse<List<TaskDTO>> getAllTasks() {
        return ApiResponse.success(taskService.getAllTasks());
    }

    @PostMapping
    public ApiResponse<TaskDTO> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return ApiResponse.success(taskService.createTask(request));
    }
}
```

**Thymeleaf页面控制器：**
```java
@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskViewController {

    private final TaskService taskService;

    @GetMapping
    public String listTasks(Model model) {
        model.addAttribute("tasks", taskService.getAllTasks());
        return "tasks/list";
    }
}
```

**统一响应格式：**
```java
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "操作成功", data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message, null, LocalDateTime.now());
    }
}
```

### Service层规范

**Service接口和实现：**
```java
public interface TaskService {
    TaskDTO createTask(CreateTaskRequest request);
    List<TaskDTO> getAllTasks();
    TaskDTO getTaskById(Long id);
}

@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Override
    public TaskDTO createTask(CreateTaskRequest request) {
        Task task = taskMapper.toEntity(request);
        return taskMapper.toDTO(taskRepository.save(task));
    }
}
```

**事务管理：**
- Service类级别使用`@Transactional`
- 只读查询方法使用`@Transactional(readOnly = true)`
- 复杂业务逻辑确保在事务中执行

### Repository层规范

**Repository接口：**
```java
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCreatedBy(User user);
    List<Task> findByTypeAndActive(TaskType type, boolean active);

    @Query("SELECT t FROM Task t JOIN FETCH t.child WHERE t.child.id = :childId")
    List<Task> findByChildIdWithChild(@Param("childId") Long childId);
}
```

- 继承`JpaRepository`获得基础CRUD
- 复杂查询使用`@Query`自定义JPQL
- 使用`@EntityGraph`优化关联查询

### 命名约定

**类名：** PascalCase
- 实体：`Task`, `Reward`, `User`
- DTO：`TaskDTO`, `CreateTaskRequest`, `TaskResponse`
- Service：`TaskService`, `TaskServiceImpl`

**方法名：** camelCase，动词开头
- 查询：`getTaskById`, `findTasksByChild`, `listActiveTasks`
- 创建：`createTask`, `addReward`
- 更新：`updateTask`, `modifyTaskStatus`
- 删除：`deleteTask`, `removeReward`
- 业务逻辑：`approveTask`, `completeTask`, `redeemReward`

**常量：** UPPER_SNAKE_CASE
```java
public static final int DEFAULT_REWARD_QUANTITY = 999;
public static final String ROLE_PARENT = "ROLE_PARENT";
public static final String ROLE_CHILD = "ROLE_CHILD";
```

**私有变量：** camelCase，无前缀
```java
private Long id;
private String taskTitle;  // 避免驼峰命名冲突
```

### 错误处理规范

**自定义异常：**
```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s not found with id: %d", resource, id));
    }
}

public class BusinessException extends RuntimeException {
    private final String errorCode;
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
```

**全局异常处理器：**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", message));
    }
}
```

**请求验证：**
```java
public class CreateTaskRequest {
    @NotBlank(message = "任务标题不能为空")
    @Size(max = 100, message = "任务标题不能超过100字符")
    private String title;

    @NotNull(message = "任务积分不能为空")
    @Min(value = 1, message = "任务积分至少为1")
    private Integer points;

    @NotNull(message = "任务类型不能为空")
    private TaskType type;
}
```

### Spring Security规范

**密码加密：**
```java
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**角色定义：**
- `ROLE_PARENT`：家长角色，完整管理权限
- `ROLE_CHILD`：小孩角色，有限操作权限

**方法级安全：**
```java
@PreAuthorize("hasRole('PARENT')")
public TaskDTO createTask(CreateTaskRequest request) { }

@PreAuthorize("hasRole('PARENT') or @taskService.isTaskOwner(#taskId, authentication.name)")
public void approveTask(Long taskId) { }

@PreAuthorize("hasRole('CHILD')")
public void completeTask(Long taskId) { }
```

### Thymeleaf模板规范

**模板结构：**
```
src/main/resources/
├── templates/
│   ├── layout/          # 布局模板
│   │   └── main.html
│   ├── parent/          # 家长页面
│   │   ├── dashboard.html
│   │   ├── tasks.html
│   │   └── rewards.html
│   ├── child/           # 小孩页面
│   │   ├── dashboard.html
│   │   ├── tasks.html
│   │   └── rewards.html
│   └── common/          # 公共片段
│       ├── header.html
│       └── footer.html
└── static/
    ├── css/
    ├── js/
    └── images/
```

**常用Thymeleaf语法：**
```html
<!-- 文本绑定 -->
<span th:text="${task.title}">任务标题</span>

<!-- 列表迭代 -->
<div th:each="task : ${tasks}">
    <p th:text="${task.title}">任务</p>
</div>

<!-- 条件判断 -->
<div th:if="${task.active}">任务进行中</div>
<div th:unless="${task.active}">任务已暂停</div>

<!-- 表单绑定 -->
<form th:object="${task}" th:action="@{/tasks}" method="post">
    <input type="text" th:field="*{title}" />
    <input type="number" th:field="*{points}" />
    <button type="submit">提交</button>
</form>

<!-- 片段包含 -->
<div th:replace="~{common/header :: header}"></div>

<!-- 链接URL -->
<a th:href="@{/tasks/{id}(id=${task.id})">查看详情</a>

<!-- 动画类 -->
<div class="animate__animated animate__fadeIn">
    <h1>欢迎来到积分系统！</h1>
</div>
```

**Animate.css集成：**
```html
<!-- 在head中引入 -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/animate.css/4.1.1/animate.min.css"/>

<!-- 使用动画 -->
<button class="btn btn-primary animate__animated animate__pulse animate__infinite">
    点击我
</button>
```

### 测试规范

**单元测试（Service层）：**
```java
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    void createTask_ShouldReturnTaskDTO() {
        // Given
        CreateTaskRequest request = new CreateTaskRequest("测试任务", 10, TaskType.ONE_TIME);
        Task savedTask = new Task(1L, "测试任务", 10, TaskType.ONE_TIME);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // When
        TaskDTO result = taskService.createTask(request);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("测试任务");
        verify(taskRepository).save(any(Task.class));
    }
}
```

**测试命名规范：**
- 测试类：`{ClassName}Test`
- 测试方法：`{MethodName}_{Should}_{ExpectedResult}`

---

## 数据库规范

### JPA实体与表映射
- 实体名使用单数：`Task`, `User`, `Reward`
- 表名使用复数：`tasks`, `users`, `rewards`
- 使用`@Column`指定列名和约束

**关键实体关系：**
```java
// 用户（家长/小孩）
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;  // BCrypt加密

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;  // PARENT, CHILD

    // 家长与小孩的一对多关系
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Child> children = new ArrayList<>();
}

// 小孩实体
@Entity
@Table(name = "children")
public class Child extends User {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer points = 0;

    @OneToMany(mappedBy = "child")
    private List<TaskCompletion> taskCompletions = new ArrayList<>();

    @OneToMany(mappedBy = "child")
    private List<RewardRedemption> rewardRedemptions = new ArrayList<>();
}

// 任务实体
@Entity
@Table(name = "tasks")
public class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer points;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type;  // ONE_TIME, REPEATABLE, DAILY_ONCE

    @Enumerated(EnumType.STRING)
    private TaskStatus status;  // DRAFT, APPROVED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_child_id")
    private Child assignedChild;

    @Column(nullable = false)
    private boolean active = true;
}

// 任务完成记录
@Entity
@Table(name = "task_completions")
public class TaskCompletion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompletionStatus status;  // PENDING, APPROVED, REJECTED

    @Column(columnDefinition = "TEXT")
    private String proof;  // 完成证明（如照片URL）

    @Column(nullable = false)
    private LocalDateTime completedAt;

    @Column
    private LocalDateTime approvedAt;
}

// 礼物实体
@Entity
@Table(name = "rewards")
public class Reward {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 999")
    private Integer quantity = 999;

    @Column(nullable = false)
    private Integer pointsRequired;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private boolean active = true;
}

// 礼物兑换记录
@Entity
@Table(name = "reward_redemptions")
public class RewardRedemption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @Column(nullable = false)
    private LocalDateTime redeemedAt;

    @Column(length = 500)
    private String note;
}
```

### 数据初始化脚本
- 使用`import.sql`（Spring Boot自动执行）
- 位置：`src/main/resources/import.sql`

**示例import.sql：**
```sql
-- 插入家长账号（密码: parent123）
INSERT INTO users (username, password, role) VALUES ('parent', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'PARENT');

-- 插入小孩账号（密码: child123）
INSERT INTO users (username, password, role) VALUES ('child', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'CHILD');
INSERT INTO children (id, parent_id, points) VALUES (LAST_INSERT_ID(), 1, 0);

-- 插入示例任务
INSERT INTO tasks (title, description, points, type, status, created_by_id, assigned_child_id, active) VALUES
('完成作业', '按时完成学校作业', 10, 'DAILY_ONCE', 'APPROVED', 1, 2, true),
('打扫房间', '整理自己的房间', 5, 'REPEATABLE', 'APPROVED', 1, 2, true);

-- 插入示例礼物
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES
('游戏时间', '30分钟游戏时间', 999, 20, true),
('零花钱', '10元零花钱', 50, 100, true),
('外出游玩', '周末去公园玩', 5, 200, true);
```

### 查询优化
- 使用`JOIN FETCH`避免N+1问题：
```java
@Query("SELECT t FROM Task t JOIN FETCH t.assignedChild WHERE t.active = true")
List<Task> findActiveTasksWithChild();
```
- 对于大集合，使用分页：`Page<T>`和`Pageable`

---

## 日志规范

**SLF4J使用：**
```java
@Slf4j  // Lombok注解
@Service
public class TaskServiceImpl implements TaskService {

    public void completeTask(Long taskId, Long childId) {
        log.info("开始完成任务: taskId={}, childId={}", taskId, childId);

        try {
            // 业务逻辑
            log.debug("任务{}已完成", taskId);
        } catch (Exception e) {
            log.error("完成任务失败: taskId={}, childId={}, error={}", taskId, childId, e.getMessage(), e);
            throw e;
        }
    }
}
```

**日志级别使用：**
- `ERROR`：系统错误、异常堆栈（生产环境需监控）
- `WARN`：可恢复的异常、业务规则违反
- `INFO`：重要业务操作（创建任务、兑换礼物、积分调整）
- `DEBUG`：调试信息（方法参数、中间状态）

**注意事项：**
- 不记录敏感信息：密码、身份证号、银行卡号
- 不记录过长的字符串（如完整请求体）
- 日志消息使用占位符：`log.info("User {} logged in", username)`
- 不写入文件，仅输出到控制台

---

## 前端规范

### Bootstrap 5集成
```html
<!-- 在head中引入 -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
```

### 颜色主题
使用活泼友好的颜色，适合小孩使用：
```css
:root {
    --primary-color: #4F46E5;    /* 蓝紫色 - 专业 */
    --success-color: #10B981;    /* 绿色 - 成功/积分 */
    --warning-color: #F59E0B;   /* 橙色 - 任务 */
    --danger-color: #EF4444;     /* 红色 - 扣分/警告 */
    --info-color: #3B82F6;       /* 浅蓝色 - 信息 */
    --bg-color: #F3F4F6;         /* 浅灰背景 */
}
```

### UI组件规范
**按钮样式：**
```html
<button class="btn btn-primary btn-lg animate__animated animate__bounce">
    完成任务
</button>
<button class="btn btn-success">
    <i class="bi bi-gift"></i> 兑换礼物
</button>
```

**卡片布局：**
```html
<div class="card shadow-sm animate__animated animate__fadeInUp">
    <div class="card-body">
        <h5 class="card-title" th:text="${task.title}">任务标题</h5>
        <p class="card-text" th:text="${task.description}">任务描述</p>
        <span class="badge bg-success" th:text="${task.points + ' 积分'}">10 积分</span>
        <a th:href="@{/tasks/{id}(id=${task.id})}" class="btn btn-primary">查看详情</a>
    </div>
</div>
```

### 动画效果（Animate.css）
```html
<!-- 页面加载动画 -->
<div class="container animate__animated animate__fadeIn">
    <h1>欢迎回来，<span th:text="${child.username}">小孩</span>！</h1>
</div>

<!-- 任务列表动画 -->
<div class="row g-4">
    <div th:each="task, iterStat : ${tasks}"
         class="col-md-4 animate__animated animate__fadeInUp"
         th:style="'animation-delay: ' + ${iterStat.count * 100} + 'ms'">
        <div class="card">...</div>
    </div>
</div>

<!-- 成功提示动画 -->
<div id="successMessage" class="alert alert-success animate__animated animate__pulse animate__infinite">
    恭喜！你获得了 <span th:text="${points}">10</span> 积分！
</div>
```

### 响应式设计
- 使用Bootstrap Grid系统：`col-md-4`, `col-lg-3`
- 移动优先，确保在手机上友好使用
- 按钮和表单元素有足够点击区域（至少44px高度）

### 用户体验
- 操作成功显示友好的提示信息
- 使用图标增强视觉效果（Bootstrap Icons）
- 积分变化显示动画效果
- 加载状态使用Spinner
- 表单验证提示清晰明确

---

## 开发工作流

1. 创建功能分支：`git checkout -b feature/task-management`
2. 编写代码，遵循上述规范
3. 运行测试：`mvn test`
4. 提交代码：`git commit -m "feat: 添加任务管理功能"`
5. 合并到主分支

---

## 重要提示

- **数据迁移**：H2数据库配置在`application-dev.properties`，MySQL配置在`application-prod.properties`
- **安全性**：所有密码必须使用BCrypt加密后存储
- **事务管理**：涉及多表操作的方法必须使用`@Transactional`
- **权限控制**：家长和小孩的权限严格分离，使用Spring Security方法级注解
- **性能优化**：避免N+1查询，使用JOIN FETCH或@EntityGraph
- **代码质量**：提交前确保所有测试通过，无lint警告

---

## 实现状态 (2026-02-03)

### ✅ 已完成功能

#### 1. 核心任务管理
- 家长创建、编辑、删除任务
- 孩子查看和完成任务
- 家长审批任务完成请求
- 积分发放和更新

#### 2. 每日任务限制功能
- `DAILY_ONCE` 任务类型每日完成限制
- 日期范围检查（考虑时区）
- 待审批和已批准状态都计入限制
- 友好的错误提示

#### 3. 家长审批界面
- 专门的审批页面 (`/parent/approvals`)
- 待审批任务列表
- 批准和拒绝功能
- 积分自动发放

#### 4. 礼物兑换系统
- 孩子查看可用礼物
- 积分兑换礼物
- 兑换历史记录
- 库存管理

#### 5. 仪表板增强
- 活动消息基于状态显示
- 孩子和家长的不同视图
- 积分余额和任务统计

#### 6. 任务市场功能
- 家长创建市场任务（不分配具体孩子）
- 孩子浏览和领取市场任务
- 先到先得领取机制
- 家庭隔离（只能看到自己家长的任务）
- 任务放弃和返回市场功能

### 🔧 技术实现亮点

#### 数据库层
- 修复了 `TaskCompletionRepository.existsCompletionToday()` 查询
- 使用日期范围参数替代 `DATE()` 函数，提高数据库兼容性
- 优化了关联查询，避免 N+1 问题

#### 业务逻辑层
- 完整的每日限制检查逻辑
- 事务管理确保数据一致性
- 错误处理使用自定义 `BusinessException`

#### 前端界面
- 响应式设计，移动端友好
- 使用 Bootstrap 5 和 Animate.css
- 实时反馈和错误提示
- 中文界面，适合家庭使用

#### 测试覆盖
- 单元测试：19个测试全部通过
- 集成测试：覆盖关键业务场景
- 每日限制功能有专门测试用例

### 📊 项目状态
- **编译状态**：✅ `mvn clean compile` 成功
- **测试状态**：✅ 核心业务测试通过（28/28服务层测试）
- **功能完整性**：✅ 核心功能全部实现，包括任务市场
- **代码质量**：✅ 遵循项目规范
- **打包状态**：✅ `mvn clean package` 成功（跳过测试）

### 🎯 后续建议
1. **时区配置**：添加系统时区配置，支持跨时区家庭使用
2. **自定义限制**：允许家长配置不同类型的任务限制规则
3. **可视化日历**：在 UI 中显示孩子每日任务完成情况
4. **批量操作**：支持家长批量审批任务完成请求

---

## 任务市场功能 (2026-02-03) - **已重构**

**重要更新 (最新)**：
Task 实体不再包含 `assignedChild` 或 `pickedByChild` 字段。所有任务与孩子的关联都通过 `TaskJob` 实体管理。

### 核心设计理念

**Task（任务定义）** = 任务模板/定义，家长创建的任务，放在市场上
**TaskJob（任务实例）** = 孩子领取任务后创建的关联记录，一个 Task 可以对应多个 TaskJob

**之前**（错误设计）：
- Task 实体同时承担"任务模板"和"任务实例"两种角色
- `assignedChild` 和 `pickedByChild` 语义混淆

**现在**（正确设计）：
- **Task** = 任务模板（市场任务定义），**没有** `assignedChild` 或 `pickedByChild` 字段
- **TaskJob** = 任务实例（记录哪个孩子领取了任务），有 `childId` 和 `assignedAt` 字段
- 家长创建任务 → Task 入库（无 assignedChild）
- 孩子领取任务 → 创建 TaskJob 记录关联
- 家长直接分配 → 同时创建 Task + TaskJob

**需求描述**：
实现任务市场功能，让孩子可以从市场中选择任务，完成任务后获得积分。家长可以创建任务并选择是否放入市场或直接分配给孩子。

**具体功能要求**：

#### 1. 家长创建市场任务
- 家长创建任务时可以选择：
  - **直接分配**：直接分配给特定孩子
  - **放入市场**：不分配具体孩子，放入任务市场供孩子选择
- 市场任务对所有孩子可见（同一家庭内）
- 家长可以随时将市场任务转为直接分配任务

#### 2. 孩子浏览任务市场
- 孩子在"任务市场"页面可以浏览所有可用的市场任务
- 显示任务标题、描述、积分、任务类型
- 孩子可以选择"领取任务"将任务加入自己的任务列表
- 每个市场任务只能被一个孩子领取（先到先得）

#### 3. 任务领取机制
- 孩子领取任务后，任务从市场消失，其他孩子无法再领取
- 领取的任务出现在孩子的"我的任务"列表中
- 孩子可以完成领取的任务，获得积分
- 孩子可以"放弃任务"将任务放回市场（如果未开始）

#### 4. 我的已领取任务
- 孩子在"任务市场"页面可以看到自己已领取的任务
- 显示领取时间、任务状态
- 可以直接从市场页面跳转到任务完成页面

**技术实现**：

#### 数据库层
- **Task 实体**：**移除** `assignedChild` 和 `pickedByChild` 字段，Task 只保存任务定义
- **TaskJob 实体**：负责任务与孩子的关联，包含 `childId`、`assignedAt`、`status` 等字段
- **Repository 查询**：
  - `findAvailableMarketplaceTasksByParentId()` - 查找没有 TaskJob 的任务（可领取）
  - `findPickedTasksByChildId()` - 通过 JOIN TaskJob 查找孩子已领取的任务
  - `findActiveTasksWithChild()` - 通过 JOIN TaskJob 查找孩子的任务

#### 业务逻辑层
- **TaskService新增方法**：
  - `pickTask()` - 孩子领取市场任务
  - `unpickTask()` - 孩子放弃已领取任务
  - `getMarketplaceTasks()` - 获取可领取的市场任务
  - `getPickedTasks()` - 获取孩子已领取的任务
- **验证逻辑**：
  - 家庭隔离：孩子只能看到/领取自己家长创建的任务
  - 先到先得：任务被领取后立即从市场消失
  - 状态检查：只能领取 `ACTIVE` 状态的任务

#### 控制器层
- **ViewController新增端点**：
  - `GET /child/marketplace` - 孩子浏览任务市场
  - `POST /child/marketplace/{taskId}/pick` - 孩子领取任务
  - `POST /child/marketplace/{taskId}/unpick` - 孩子放弃任务
- **家长任务创建**：修改 `POST /parent/tasks` 支持市场任务创建

#### 前端界面
- **child/marketplace.html** - 任务市场页面
  - 可领取任务网格展示
  - 已领取任务列表
  - AJAX 领取/放弃功能
  - 统计卡片（总积分、任务数量）
- **parent/tasks.html** - 家长任务创建表单增强
  - 添加"放入市场"复选框
  - 孩子选择下拉框可选"不分配（任务市场）"
- **导航更新**：所有孩子页面添加"任务市场"链接

#### 测试覆盖
- **TaskServiceMarketplaceTest** - 9个测试用例覆盖：
  - 孩子领取市场任务成功
  - 重复领取失败（任务已被领取）
  - 孩子放弃已领取任务
  - 家庭隔离验证（不能领取其他家庭的任务）
  - 市场任务查询正确性
  - 已领取任务查询正确性

**实现状态**：
- ✅ 数据库层：Task 实体修改完成（移除 assignedChild/pickedByChild）
- ✅ Repository 层：查询方法实现完成（通过 TaskJob JOIN）
- ✅ Service 层：业务逻辑实现完成
- ✅ Controller 层：端点实现完成
- ✅ 前端界面：市场页面和表单增强完成
- ✅ 测试覆盖：测试用例更新完成
- ✅ 编译验证：`mvn clean compile` 成功
- ✅ 打包验证：`mvn clean package` 成功（跳过测试）

**关键设计决策**：
1. **Task 与 TaskJob 分离**：Task 只保存任务定义，TaskJob 保存任务与孩子的关联
2. **家庭隔离**：孩子只能看到/领取自己家长创建的任务
3. **先到先得**：简单的领取机制，任务被领取后立即从市场消失
4. **无缝集成**：领取的任务与直接分配的任务在"我的任务"列表中统一显示
5. **状态保持**：放弃的任务返回市场，保持原有状态

**使用流程**：
```
家长创建任务 → 选择"放入市场" → 任务出现在市场
孩子浏览市场 → 选择"领取任务" → 任务加入"我的任务"
孩子完成任务 → 家长审批 → 孩子获得积分
孩子放弃任务 → 任务返回市场 → 其他孩子可领取
```

---

## 新增功能 (2026-02-02)

### 草稿任务功能

**需求描述**：
孩子可以创建草稿任务，但需要家长审批后才能成为正式任务。

**具体功能要求**：

#### 1. 孩子创建草稿任务
- 孩子在"我的任务"页面可以创建草稿任务
- 草稿任务使用 `TaskStatus.DRAFT` 状态
- 创建时需填写：任务标题、描述、期望积分、任务类型、分配给自己
- 提交后显示"等待家长审批"提示

#### 2. 家长审批草稿任务
- 家长在"任务审批"页面可以切换到"草稿任务审批"标签
- 显示所有孩子创建的待审批草稿任务
- 家长可以：
  - **批准**：草稿任务变为 `APPROVED` 状态，成为正式任务
  - **拒绝**：草稿任务变为 `REJECTED` 状态
- 批准的草稿任务会自动出现在孩子的任务列表中

#### 3. 任务状态流转
```
草稿任务流程：
DRAFT (孩子创建) → APPROVED (家长批准) → 成为正式任务
                → REJECTED (家长拒绝) → 任务结束

任务完成流程：
APPROVED (正式任务) → 孩子标记完成 → PENDING (待审批) → APPROVED (获得积分)
```

**技术实现**：

- **Repository层**：
  - `TaskRepository.findDraftTasksByParentId()` - 查询家长所有孩子的草稿任务
  - `TaskRepository.findByCreatedById()` - 查询指定用户创建的任务

- **Service层**：
  - `TaskService.createDraftTask()` - 创建草稿任务（状态为 DRAFT）
  - `TaskService.approveDraftTask()` - 批准草稿任务（DRAFT → APPROVED）
  - `TaskService.rejectDraftTask()` - 拒绝草稿任务（DRAFT → REJECTED）
  - `TaskService.getDraftTasksByParent()` - 获取家长的草稿任务列表
  - `TaskService.getDraftTasksByChild()` - 获取孩子自己的草稿任务

- **Controller层**：
  - `POST /child/tasks` - 孩子创建草稿任务
  - `GET /child/tasks/drafts` - 孩子查看自己的草稿任务
  - `GET /parent/drafts` - 家长查看待审批的草稿任务
  - `POST /parent/drafts/{taskId}/approve` - 批准草稿任务
  - `POST /parent/drafts/{taskId}/reject` - 拒绝草稿任务

- **前端页面**：
  - `child/tasks.html` - 添加草稿任务创建表单
  - `child/drafts.html` - 孩子查看自己的草稿任务
  - `parent/drafts.html` - 家长审批草稿任务
  - `parent/approvals.html` - 添加导航标签切换到草稿任务审批

### 每日任务限制功能 (补充说明)

**已知问题**：
- 如果孩子同一天尝试提交同类型 `DAILY_ONCE` 任务两次，系统会抛出 `DAILY_LIMIT_EXCEEDED` 错误
- 前端 JavaScript 会捕获错误并显示提示信息

**前端错误处理**：
```javascript
.catch(function(error) {
    alert('提交失败: ' + (error.message || '未知错误'));
    button.disabled = false;
    button.innerHTML = originalText;
});
```

**后端验证**：
- `TaskCompletionRepository.existsCompletionToday()` 查询检查 `PENDING` 和 `APPROVED` 状态
- 确保同一天只能有一个待审批或已批准的任务完成记录

---

## 任务实体重构 (2026-03-14) - **重要架构更新**

### 核心设计变更

**之前**（错误设计）：
- Task 实体同时承担"任务模板"和"任务实例"双重角色
- 包含 `assignedChild`（直接分配）和 `pickedByChild`（市场领取）字段
- 语义混淆，职责不清

**现在**（正确设计）：
- **Task（任务定义）** = 纯粹的任务模板，家长创建的任务定义，放在市场上
  - **没有** `assignedChild` 字段
  - **没有** `pickedByChild` 字段
  - **没有** `pickedAt` 字段
  - 只保存任务的基本信息（标题、描述、积分、类型等）
  
- **TaskJob（任务实例）** = 任务与孩子的关联记录
  - 包含 `childId`（哪个孩子领取/被分配）
  - 包含 `assignedAt`（分配/领取时间）
  - 包含 `status`（ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED）
  - 包含快照字段（snapshotTitle, snapshotDescription 等）

### 业务流程

```
家长创建任务 → Task 入库（无 assignedChild）
孩子领取任务 → 创建 TaskJob 记录关联
家长直接分配 → 同时创建 Task + TaskJob
```

### 数据库变更

**Task 表删除字段**：
```sql
ALTER TABLE tasks DROP COLUMN assigned_child_id;
ALTER TABLE tasks DROP COLUMN picked_by_child_id;
```

**TaskJob 表（已存在）**：
```sql
CREATE TABLE task_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    child_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    assigned_at DATETIME NOT NULL,
    snapshot_title VARCHAR(100),
    snapshot_description VARCHAR(500),
    snapshot_points INT,
    snapshot_task_type VARCHAR(50),
    FOREIGN KEY (task_id) REFERENCES tasks(id),
    FOREIGN KEY (child_id) REFERENCES children(id)
);
```

### 查询逻辑变更

所有查询从 `WHERE t.pickedByChild.id = :childId` 改为通过 TaskJob JOIN：

```java
// 之前
@Query("SELECT t FROM Task t WHERE t.pickedByChild.id = :childId ORDER BY t.id DESC")

// 现在
@Query("SELECT t FROM Task t INNER JOIN TaskJob tj ON t.id = tj.task.id " +
       "WHERE tj.child.id = :childId AND tj.status IN ('ASSIGNED', 'IN_PROGRESS') " +
       "ORDER BY tj.assignedAt DESC")
```

### 时间字段说明

**Task.createdAt** = 任务创建时间（家长创建任务的时间）
**TaskJob.assignedAt** = 任务分配/领取时间（孩子领取任务或家长分配任务的时间）

**显示规则**：
- 进行中的任务列表：显示 TaskJob.assignedAt（领取时间）
- 如果 TaskJob.assignedAt 为空（直接分配任务）：显示 Task.createdAt
- 家长待审批任务：同时显示"领取时间"和"完成时间"

---

## 任务列表排序优化 (2026-03-14)

### 需求
进行中的任务按领取时间倒序显示，最新领取的任务在前。

### 实现
所有查询统一使用 `ORDER BY tj.assignedAt DESC` 或 `ORDER BY t.createdAt DESC`：

| 查询方法 | 排序字段 | 说明 |
|---------|---------|------|
| `findActiveTasksWithChild` | `tj.assignedAt DESC` | 孩子的任务列表 |
| `findPickedTasksByChildId` | `tj.assignedAt DESC` | 已领取任务 |
| `findVisibleMarketplaceTasksByParentId` | `t.createdAt DESC` | 市场任务 |
| `findAvailableMarketplaceTasksByParentId` | `t.createdAt DESC` | 可领取任务 |

### 前端显示

**child/tasks.html** - 进行中的任务：
```html
<td>
    <small th:if="${task.pickedAt != null}"
           th:text="${#temporals.format(task.pickedAt, 'yyyy-MM-dd HH:mm')}">
    </small>
    <small th:unless="${task.pickedAt != null}"
           th:text="${#temporals.format(task.createdAt, 'yyyy-MM-dd HH:mm')}">
    </small>
</td>
```

**parent/approvals.html** - 待审批任务：
```html
<td>领取时间</td>  <!-- 新增列 -->
<td>完成时间</td>
```

---

## 任务市场搜索功能 (2026-03-14)

### 需求
孩子浏览任务市场时，可以通过搜索框过滤可领取的任务。

### 实现

**Repository 层**：
```java
@Query("SELECT t FROM Task t " +
       "LEFT JOIN FETCH t.createdBy " +
       "WHERE t.createdBy.id = :parentId " +
       "AND t.active = true " +
       "AND t.status = 'APPROVED' " +
       "AND NOT EXISTS (SELECT tj FROM TaskJob tj WHERE tj.task = t AND tj.status IN ('ASSIGNED', 'IN_PROGRESS')) " +
       "AND (:keyword IS NULL OR :keyword = '' OR t.title LIKE %:keyword% OR t.description LIKE %:keyword%) " +
       "ORDER BY t.createdAt DESC")
List<Task> findVisibleMarketplaceTasksByParentIdWithSearch(@Param("parentId") Long parentId,
                                                            @Param("keyword") String keyword);
```

**Service 层**：
```java
public List<TaskDTO> getMarketplaceTasksWithSearch(Long childId, String keyword) {
    // 获取孩子和家长信息
    Child child = childRepository.findById(childId)...;
    User parent = child.getParent()...;
    
    // 根据是否有搜索关键词选择查询方法
    List<Task> tasks;
    if (keyword != null && !keyword.trim().isEmpty()) {
        tasks = taskRepository.findVisibleMarketplaceTasksByParentIdWithSearch(
            parent.getId(), keyword.trim());
    } else {
        tasks = taskRepository.findVisibleMarketplaceTasksByParentId(parent.getId());
    }
    return tasks.stream().map(this::toDTO).collect(Collectors.toList());
}
```

**Controller 层**：
```java
@GetMapping("/child/marketplace")
public String marketplace(@AuthenticationPrincipal UserDetails userDetails, 
                         @RequestParam(required = false) String search,
                         Model model) {
    // 处理搜索参数
    List<TaskDTO> marketplaceTasks;
    if (search != null && !search.trim().isEmpty()) {
        marketplaceTasks = taskService.getMarketplaceTasksWithSearch(user.getId(), search.trim());
    } else {
        marketplaceTasks = taskService.getMarketplaceTasks(user.getId());
    }
    model.addAttribute("marketplaceTasks", marketplaceTasks);
    model.addAttribute("searchKeyword", search != null ? search : "");
    return "child/marketplace";
}
```

**前端界面**：
```html
<form th:action="@{/child/marketplace}" method="get" class="d-flex">
    <input type="text" name="search" class="form-control form-control-sm me-2" 
           placeholder="搜索任务..." th:value="${searchKeyword}"
           style="width: 200px;">
    <button type="submit" class="btn btn-light btn-sm">
        <i class="bi bi-search"></i> 搜索
    </button>
    <a th:href="@{/child/marketplace}" class="btn btn-outline-light btn-sm ms-1">
        <i class="bi bi-x-circle"></i> 清除
    </a>
</form>
```

---

## 抽奖系统 (2026-03-14)

### 需求
实现完整的抽奖系统，支持家长创建抽奖主题和奖品，孩子消耗积分参与抽奖。

### 核心功能

#### 1. 抽奖主题管理（家长端）
- 创建多个抽奖主题（LotteryTheme）
- 每个主题配置多个奖品（LotteryPrize）
- 设置每个奖品的概率权重（weight）
- 设置每次抽奖消耗的积分（pointsPerDraw）
- 支持启用/禁用主题

#### 2. 抽奖功能（小孩端）
- 选择抽奖主题
- 消耗积分进行抽奖
- 显示中奖结果（可能中多个奖品）
- 查看抽奖历史记录

### 数据库设计

**lottery_themes** - 抽奖主题表
```sql
CREATE TABLE lottery_themes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    points_per_draw INT NOT NULL,
    type VARCHAR(50) NOT NULL,  -- FIXED_PROBABILITY, WEIGHTED_RANDOM, GUARANTEED
    active BOOLEAN NOT NULL DEFAULT true,
    created_by_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (created_by_id) REFERENCES users(id)
);
```

**lottery_prizes** - 奖品表
```sql
CREATE TABLE lottery_prizes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lottery_theme_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    value INT NOT NULL,  -- 奖品价值（积分）
    weight INT NOT NULL DEFAULT 1,  -- 概率权重
    probability INT,  -- 固定概率（百分比）
    quantity INT NOT NULL DEFAULT -1,  -- 库存（-1 为无限）
    redeemed_count INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true,
    image_url VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (lottery_theme_id) REFERENCES lottery_themes(id)
);
```

**lottery_draws** - 抽奖记录表
```sql
CREATE TABLE lottery_draws (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lottery_theme_id BIGINT NOT NULL,
    child_id BIGINT NOT NULL,
    points_cost INT NOT NULL,
    result_status VARCHAR(50) NOT NULL,  -- NO_WIN, WON, JACKPOT
    draw_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (lottery_theme_id) REFERENCES lottery_themes(id),
    FOREIGN KEY (child_id) REFERENCES children(id)
);
```

**lottery_draw_results** - 抽奖结果详情表
```sql
CREATE TABLE lottery_draw_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lottery_draw_id BIGINT NOT NULL,
    prize_id BIGINT NOT NULL,
    prize_name VARCHAR(100) NOT NULL,
    prize_value INT NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (lottery_draw_id) REFERENCES lottery_draws(id),
    FOREIGN KEY (prize_id) REFERENCES lottery_prizes(id)
);
```

### 抽奖算法

**权重随机模式（WEIGHTED_RANDOM）**：
```java
private List<LotteryPrize> performWeightedDraw(List<LotteryPrize> prizes) {
    List<LotteryPrize> wonPrizes = new ArrayList<>();
    
    // 计算总权重
    int totalWeight = prizes.stream().mapToInt(LotteryPrize::getWeight).sum();
    if (totalWeight == 0) {
        return wonPrizes;
    }
    
    // 简单实现：抽取 1-3 个奖品
    int numWins = RANDOM.nextInt(3) + 1; // 1-3 个奖品
    
    for (int i = 0; i < numWins; i++) {
        int random = RANDOM.nextInt(totalWeight);
        int cumulative = 0;
        
        for (LotteryPrize prize : prizes) {
            cumulative += prize.getWeight();
            if (random < cumulative && prize.hasQuantity()) {
                wonPrizes.add(prize);
                break;
            }
        }
    }
    
    return wonPrizes;
}
```

### 积分处理

**抽奖消耗**：
```java
// 扣除小孩积分
Integer originalPoints = child.getPoints();
child.setPoints(originalPoints - theme.getPointsPerDraw());
childRepository.save(child);

// 记录积分扣除历史
pointHistoryService.recordPointChange(
    childId,
    -theme.getPointsPerDraw(),
    PointChangeType.LOTTERY_DRAW,
    "抽奖消耗 - " + theme.getName(),
    theme.getId(),
    "LOTTERY_THEME",
    null
);
```

**中奖获得**：
```java
// 给小孩增加奖品对应的积分
Integer totalWinPoints = wonPrizes.stream().mapToInt(LotteryPrize::getValue).sum();
child.setPoints(child.getPoints() + totalWinPoints);
childRepository.save(child);

// 记录中奖积分
pointHistoryService.recordPointChange(
    childId,
    prize.getValue(),
    PointChangeType.LOTTERY_WIN,
    "抽奖中奖 - " + prize.getName(),
    prize.getId(),
    "LOTTERY_PRIZE",
    null
);
```

### 枚举类型

**LotteryTypeEnum** - 抽奖类型
```java
public enum LotteryTypeEnum {
    FIXED_PROBABILITY("固定概率"),      // 每个奖品有固定概率
    WEIGHTED_RANDOM("权重随机"),        // 根据权重计算概率
    GUARANTEED("必中模式")              // 100% 中奖
}
```

**DrawResultStatus** - 抽奖结果状态
```java
public enum DrawResultStatus {
    NO_WIN("未中奖"),
    WON("中奖"),
    JACKPOT("特等奖")
}
```

**PointChangeType** - 积分变动类型（新增）
```java
public enum PointChangeType {
    TASK_COMPLETION("任务完成"),
    REWARD_REDEMPTION("礼物兑换"),
    LOTTERY_DRAW("抽奖消耗"),          // 新增
    LOTTERY_WIN("抽奖获奖"),           // 新增
    INITIAL("初始积分"),
    OTHER("其他")
}
```

### REST API

**家长端**：
```
POST   /api/v1/lottery/themes          - 创建抽奖主题
GET    /api/v1/lottery/themes          - 获取主题列表
PUT    /api/v1/lottery/themes/{id}     - 更新主题
DELETE /api/v1/lottery/themes/{id}     - 删除主题
POST   /api/v1/lottery/themes/{id}/toggle - 启用/禁用主题

POST   /api/v1/lottery/prizes          - 创建奖品
PUT    /api/v1/lottery/prizes/{id}     - 更新奖品
DELETE /api/v1/lottery/prizes/{id}     - 删除奖品
GET    /api/v1/lottery/themes/{id}/prizes - 获取奖品列表
```

**小孩端**：
```
GET    /api/v1/lottery/themes/active   - 获取可用抽奖主题
POST   /api/v1/lottery/themes/{id}/draw - 执行抽奖
GET    /api/v1/lottery/history         - 获取抽奖历史
```

### 前端页面

**parent/lottery.html** - 家长抽奖管理
- 创建抽奖主题表单
- 主题列表展示
- 管理奖品弹窗
- 启用/禁用主题
- 删除主题

**child/lottery.html** - 需要创建
- 抽奖主题卡片展示
- 抽奖按钮
- 抽奖结果弹窗动画
- 抽奖历史记录

### 实现状态

**已完成**：
- ✅ 数据库层：所有实体类和枚举类
- ✅ Repository 层：所有查询接口
- ✅ Service 层：完整业务逻辑（包括抽奖算法）
- ✅ Controller 层：REST API 端点
- ✅ DTO 层：所有数据传输对象
- ✅ 家长端页面：完整管理界面
- ✅ 积分集成：自动扣除和发放
- ✅ 历史记录：完整记录每次抽奖

**待完成**：
- ❌ 小孩端页面：`child/lottery.html`
- ❌ 抽奖动画效果
- ❌ 集成测试

---

---

## 积分过期系统 (2026-03-14)

### 需求描述
实现积分过期功能，孩子赚取的积分在 180 天后自动过期，鼓励及时使用并平衡收入/消费。

### 核心设计原则

**PointWallet（积分钱包）** = 独立的积分批次，每次赚取创建新批次
**FIFO 消费** = 最早赚取的积分先消费
**自动过期** = 每天凌晨 2 点检查所有过期积分

**关键特性**：
1. **每批次独立** - 每次赚取创建独立批次，有自己的过期日期
2. **180 天过期** - 默认 180 天过期期
3. **FIFO 消费** - 最早的批次先消费
4. **容错设计** - 如果定时任务错过，下次运行会过期 ALL 已过期批次

### 容错机制（重要）

**问题**：如果凌晨 2 点的定时任务因故错过几天怎么办？

**解决方案**：
```java
@Scheduled(cron = "${points.expiration.check-cron:0 0 2 * * ?}")
public int checkAndExpirePoints() {
    // 查找 ALL 过期批次，不只是今天过期的
    List<PointWallet> expiredBatches = pointWalletRepository.findAllExpiredPoints(today);
    
    // 过期所有已过期但未标记的批次
    for (PointWallet batch : expiredBatches) {
        batch.markAsExpired(); // 标记为过期，清空剩余积分
    }
}
```

**保证**：
- ✅ 即使任务错过 5 天，第 6 天运行时会过期所有 5 天的批次
- ✅ 不会遗漏任何过期积分
- ✅ 日志会记录"包含前几天错过的过期批次"

### 数据库设计

**point_wallet** - 积分钱包表
```sql
CREATE TABLE point_wallet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id BIGINT NOT NULL,
    original_points INT NOT NULL,          -- 原始积分
    remaining_points INT NOT NULL,         -- 剩余积分
    earned_date DATETIME NOT NULL,         -- 赚取日期
    expiration_date DATE,                   -- 过期日期
    source_type VARCHAR(50),               -- 来源类型
    source_id BIGINT,                       -- 来源 ID
    fully_spent BOOLEAN DEFAULT FALSE,     -- 是否已用完
    expired BOOLEAN DEFAULT FALSE,          -- 是否已过期
    expired_date DATETIME,                  -- 过期日期
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (child_id) REFERENCES children(id)
);
```

### 技术实现

#### 实体类
- **PointWallet** - 积分批次实体
- **PointExpirationProperties** - 过期配置

#### Repository
- **PointWalletRepository** - 8 个查询方法：
  - `findByChildAndRemainingPointsGreaterThanAndFullySpentFalse` - 查找可用批次
  - `findAllExpiredPoints` - 查找所有过期批次（容错关键）
  - `findExpiringPoints` - 查找即将过期的批次
  - `getTotalPointsByChild` - 获取总积分
  - `getPointsExpiringByDate` - 获取指定日期范围内过期的积分

#### Service
- **PointWalletService** - 核心业务逻辑：
  - `addPoints()` - 添加积分批次（设置过期日期）
  - `spendPoints()` - 消费积分（FIFO）
  - `checkAndExpirePoints()` - 检查并过期（定时任务）
  - `getPointsExpiringSoon()` - 获取即将过期的积分

#### 配置
```properties
# 积分过期设置
points.expiration.enabled=true
points.expiration.expiration-days=180
points.expiration.check-cron=0 0 2 * * ?  # 每天凌晨 2 点
```

### 消费算法示例

**场景**：孩子消费 120 积分

| 批次 | 赚取日期 | 过期日期 | 原始积分 | 消费过程 | 剩余 |
|------|---------|---------|---------|---------|------|
| 批次 1 | 2024-01-01 | 2024-06-29 | 100 | 消费 100 | 0 (用完) |
| 批次 2 | 2024-02-01 | 2024-07-30 | 50 | 消费 20 | 30 |
| 批次 3 | 2024-03-01 | 2024-08-28 | 200 | 未消费 | 200 |

**结果**：
- 总消费：120 积分
- 批次 1：标记为 fully_spent
- 批次 2：剩余 30 积分
- 批次 3：未动

### 过期处理示例

**场景**：定时任务错过 5 天

```
第 1 天：批次 A 过期（任务错过）
第 2 天：批次 B 过期（任务错过）
第 3 天：批次 C 过期（任务错过）
第 4 天：任务错过
第 5 天：任务错过
第 6 天：任务运行 → 过期 A, B, C 所有批次 ✅
```

**日志输出**：
```
WARN: Point expiration check complete: 3 batches expired with 225 total points.
      Note: This includes points that may have expired on previous days if job missed runs.
```

### 定时任务配置

**启用调度**：
```java
@EnableScheduling
@SpringBootApplication
public class CreditAppApplication {
    // ...
}
```

**修改执行时间**：
```properties
# 改为每天早上 6 点
points.expiration.check-cron=0 0 6 * * ?

# 改为每 12 小时检查
points.expiration.check-cron=0 0 */12 * * ?

# 禁用（测试用）
points.expiration.check-cron=-
```

### 测试覆盖

**单元测试**：
- ✅ 添加积分批次
- ✅ FIFO 消费逻辑
- ✅ 过期检查
- ✅ 容错场景（错过多次运行）

**集成测试**：
- ✅ 多孩子过期
- ✅ 部分消费后过期
- ✅ 未来过期不影响

**BDD 场景** (Cucumber)：
- ✅ 14 个完整场景覆盖所有用例

### API 端点（待实现）

```
GET    /api/v1/wallet/points           - 获取总积分
GET    /api/v1/wallet/batches          - 获取所有批次
GET    /api/v1/wallet/expiring         - 获取即将过期的积分
POST   /api/v1/wallet/spend            - 消费积分
```

### 前端展示（待实现）

**孩子钱包页面**：
```
我的积分钱包
┌─────────────────────────────────────────┐
│ 总积分：500                              │
│ 即将过期 (7 天内): 100                    │
└─────────────────────────────────────────┘

积分明细：
1. [100 分] 赚取：2024-01-01, 过期：2024-06-29 ✅ 有效
2. [50 分]  赚取：2024-02-01, 过期：2024-07-30 ✅ 有效
3. [200 分] 赚取：2024-03-01, 过期：2024-08-28 ✅ 有效
4. [100 分] 赚取：2023-07-01, 过期：2023-12-28 ❌ 已过期
```

**家长仪表板**：
```
积分过期监控
- 本周过期：150 分
- 下周将过期：200 分
- 本月已过期：100 分
```

### 迁移现有积分

```sql
-- 将现有积分迁移到钱包（不设置过期，保持兼容）
INSERT INTO point_wallet (
    child_id, original_points, remaining_points,
    earned_date, expiration_date, fully_spent,
    expired, created_at
)
SELECT id, points, points, NOW(), NULL, FALSE, FALSE, NOW()
FROM children
WHERE points > 0;
```

### 实现状态

**已完成**：
- ✅ 数据库层：实体类和 Repository
- ✅ Service 层：完整业务逻辑
- ✅ 定时任务：容错过期检查
- ✅ 测试：集成测试和 BDD 场景
- ✅ 配置：可配置的过期设置

**待完成**：
- ❌ 集成到 PointHistoryService
- ❌ API 端点
- ❌ 前端页面
- ❌ 过期通知

### 关键日志

**正常过期**：
```
INFO: Point expiration check complete: No expired points found
```

**有过期批次**：
```
INFO: Expired 100 points from batch 5 for child 小明 (expired on 2024-06-29, processed on 2024-06-30)
WARN: Point expiration check complete: 1 batches expired with 100 total points.
      Note: This includes points that may have expired on previous days if job missed runs.
```

**积分添加**：
```
INFO: Adding 50 points to child 小明 wallet from TASK_COMPLETION
INFO: Points will expire on: 2024-12-28
INFO: Point wallet entry created: id=10, child=小明，points=50, expires=2024-12-28
```

---

## 违规扣分系统 (2026-03-24) - **已实现**

### 需求描述

当小孩犯错时，家长需要扣除小孩的积分。但扣分不应是即兴的，应为每个具体犯错的事件制定具体的扣分分值，确保规则透明、教育性。

### 核心设计

**PenaltyRule（扣分规则）** = 预定义的违规类型和对应扣分分值
- 家长可创建常用扣分规则，如"打人 -10分"、"说脏话 -5分"等
- 规则可复用，避免每次即兴决定扣分

**PenaltyRecord（扣分记录）** = 已执行的扣分记录
- 记录哪个小孩被扣分
- 记录使用的规则和扣分分值
- 记录备注说明
- 记录操作人和时间

### 具体功能

#### 1. 规则管理
- 家长创建扣分规则（名称、描述、分值）
- 查看所有规则列表
- 删除不再使用的规则
- 规则仅创建者可见（家庭隔离）

#### 2. 执行扣分
- 选择小孩
- 选择预定义的扣分规则
- 可选添加备注说明
- 系统自动扣除积分
- 如果积分不足，操作失败

#### 3. 记录查看
- 查看所有扣分记录
- 显示时间、小孩、规则、分值、备注
- 按时间倒序排列

### 技术实现

#### 数据库层

**penalty_rules** - 扣分规则表
```sql
CREATE TABLE penalty_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    points INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_by_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (created_by_id) REFERENCES users(id)
);
```

**penalty_records** - 扣分记录表
```sql
CREATE TABLE penalty_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id BIGINT NOT NULL,
    penalty_rule_id BIGINT NOT NULL,
    points INT NOT NULL,
    note VARCHAR(500),
    applied_by_id BIGINT NOT NULL,
    applied_at DATETIME NOT NULL,
    FOREIGN KEY (child_id) REFERENCES children(id),
    FOREIGN KEY (penalty_rule_id) REFERENCES penalty_rules(id),
    FOREIGN KEY (applied_by_id) REFERENCES users(id)
);
```

#### Entity 层
- `PenaltyRule.java` - 扣分规则实体
- `PenaltyRecord.java` - 扣分记录实体

#### Repository 层
- `PenaltyRuleRepository.java` - 规则查询（按创建者ID查询活跃规则）
- `PenaltyRecordRepository.java` - 记录查询（按家长ID或小孩ID查询）

#### Service 层
- `PenaltyService.java` - 服务接口
- `PenaltyServiceImpl.java` - 业务逻辑
  - `createRule()` - 创建规则
  - `getRulesByParent()` - 获取规则列表
  - `deleteRule()` - 删除规则
  - `applyPenalty()` - 执行扣分
  - `getPenaltyRecordsByParent()` - 获取扣分记录

#### DTO 层
- `PenaltyRuleDTO.java` - 规则DTO
- `PenaltyRecordDTO.java` - 记录DTO
- `CreatePenaltyRuleRequest.java` - 创建规则请求
- `ApplyPenaltyRequest.java` - 执行扣分请求

#### Controller 层

**ViewController 新增端点：**
- `GET /parent/penalties` - 规则管理和记录查看页面
- `POST /parent/penalties` - 创建规则
- `GET /parent/penalties/apply` - 执行扣分页面
- `POST /parent/penalties/apply` - 执行扣分
- `POST /parent/penalties/{ruleId}/delete` - 删除规则

#### 前端页面
- `parent/penalties.html` - 规则管理+记录查看
- `parent/apply-penalty.html` - 执行扣分

### 使用流程

```
家长创建规则 → "打人" -10分
          ↓
家长执行扣分 → 选择小孩小明
          ↓
          选择规则 "打人"
          ↓
          可选添加备注
          ↓
          系统自动扣除10分
          ↓
          创建扣分记录
```

### 页面访问
- 管理规则：`/parent/penalties`
- 执行扣分：`/parent/penalties/apply`

### 导航链接
在家长导航栏添加：
```html
<li class="nav-item">
    <a class="nav-link" href="/parent/penalties">
        <i class="bi bi-exclamation-triangle"></i> 违规扣分
    </a>
</li>
```

### 实现状态
- ✅ Entity 层：`PenaltyRule`、`PenaltyRecord`
- ✅ Repository 层：`PenaltyRuleRepository`、`PenaltyRecordRepository`
- ✅ Service 层：完整业务逻辑
- ✅ DTO 层：所有DTO和Request类
- ✅ Controller 层：ViewController 新增端点
- ✅ 前端页面：`penalties.html`、`apply-penalty.html`
- ✅ 导航链接：已添加到 dashboard.html
- ✅ 编译验证：`mvn clean compile` 成功

---

## 参考资源

- Spring Boot官方文档：https://spring.io/projects/spring-boot
- Spring Security参考：https://docs.spring.io/spring-security/reference/
- Thymeleaf文档：https://www.thymeleaf.org/documentation.html
- Bootstrap 5文档：https://getbootstrap.com/docs/5.3/
- Animate.css：https://animate.style/

---

## 优惠券管理功能 (2026-03-31 新增)

### 需求描述
家长需要管理优惠券（coupon），可以批量导入、创建、编辑、删除优惠券，并追踪优惠券的使用情况。
**只有 PARENT 角色的用户可以访问优惠券管理功能**。

### 优惠券数据格式
优惠券文件 (coupon.txt) 格式示例：
```
id=2 enabled=yes comment= username=Y74GNZMKZ2 expires=0 timeout=1800 used=0
id=3 enabled=yes comment= username=4711785411 expires=0 timeout=600 used=0
```

**字段说明**：
- `id`: 优惠券唯一标识
- `enabled`: 是否启用 (yes/no)
- `comment`: 备注说明
- `username`: 指定使用的小孩用户名（可选，留空表示所有小孩可用）
- `expires`: 过期天数（0=永不过期）
- `timeout`: 有效期（秒），从领取开始计算
- `used`: 已使用次数

### 功能要求

#### 1. CRUD 操作（仅 PARENT）
- **创建优惠券**: 单个创建，指定代码、积分、有效期等
- **编辑优惠券**: 修改现有优惠券信息
- **删除优惠券**: 删除不再需要的优惠券
- **查看优惠券**: 列表显示所有优惠券，支持搜索和筛选

#### 2. 批量导入（仅 PARENT）
- 上传 coupon.txt 文件批量导入优惠券
- 自动解析文件格式，创建优惠券记录
- 跳过已存在的优惠券代码
- 显示导入结果（成功/失败数量）

#### 3. 搜索与筛选（仅 PARENT）
- 按优惠券代码搜索
- 按用户名筛选（查看指定小孩的优惠券）
- 按状态筛选（启用/禁用）

#### 4. 优惠券兑换（CHILD 可用）
- 小孩可以兑换优惠券
- 验证优惠券是否有效（未过期、未禁用）
- 验证是否指定给当前小孩（如果设置了 username）
- 兑换后增加 used_count

#### 5. 统计信息（仅 PARENT）
- 优惠券总数
- 启用/禁用数量
- 总兑换次数

### 技术实现

#### 数据库表
```sql
CREATE TABLE coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(100) UNIQUE NOT NULL,
    points INT NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    comment VARCHAR(500),
    username VARCHAR(50),
    expires_at TIMESTAMP NOT NULL,
    timeout_seconds INT NOT NULL,
    used_count INT DEFAULT 0,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);
```

#### API 端点
- `GET /api/v1/coupons` - 获取所有优惠券（PARENT）
- `POST /api/v1/coupons` - 创建优惠券（PARENT）
- `PUT /api/v1/coupons/{id}` - 更新优惠券（PARENT）
- `DELETE /api/v1/coupons/{id}` - 删除优惠券（PARENT）
- `GET /api/v1/coupons/search` - 搜索优惠券（PARENT）
- `POST /api/v1/coupons/import` - 批量导入（PARENT）
- `POST /api/v1/coupons/redeem` - 兑换优惠券（CHILD）

#### 权限控制
- 所有管理端点需要 PARENT 角色
- 兑换端点 CHILD 和 PARENT 都可以使用
- 使用 Spring Security 进行权限验证

#### 前端页面
- `/parent/coupons` - 优惠券管理页面
- 支持创建、编辑、删除、搜索、导入功能
- 显示统计信息和优惠券列表

### 文件位置
- **Entity**: `src/main/java/com/creditapp/entity/Coupon.java`
- **Repository**: `src/main/java/com/creditapp/repository/CouponRepository.java`
- **Service**: `src/main/java/com/creditapp/service/CouponService.java`
- **Controller**: `src/main/java/com/creditapp/controller/CouponController.java`
- **DTO**: `src/main/java/com/creditapp/dto/CouponDTO.java`, `CreateCouponRequest.java`
- **UI**: `src/main/resources/templates/coupons.html`
- **Migration**: `src/main/resources/db/changelog/changes/017-create-coupons-table.yaml`

### 测试要点
1. PARENT 用户可以访问所有管理功能
2. CHILD 用户访问管理端点应返回 403 禁止访问
3. 批量导入正确解析 coupon.txt 格式
4. 优惠券兑换验证逻辑正确（过期、禁用、用户名匹配）
5. 搜索和筛选功能正常工作
