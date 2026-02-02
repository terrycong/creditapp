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

## 实现状态 (2026-02-02)

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
- **测试状态**：✅ 所有测试通过
- **功能完整性**：✅ 核心功能全部实现
- **代码质量**：✅ 遵循项目规范

### 🎯 后续建议
1. **时区配置**：添加系统时区配置，支持跨时区家庭使用
2. **自定义限制**：允许家长配置不同类型的任务限制规则
3. **可视化日历**：在 UI 中显示孩子每日任务完成情况
4. **批量操作**：支持家长批量审批任务完成请求

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

---

## 参考资源

- Spring Boot官方文档：https://spring.io/projects/spring-boot
- Spring Security参考：https://docs.spring.io/spring-security/reference/
- Thymeleaf文档：https://www.thymeleaf.org/documentation.html
- Bootstrap 5文档：https://getbootstrap.com/docs/5.3/
- Animate.css：https://animate.style/
