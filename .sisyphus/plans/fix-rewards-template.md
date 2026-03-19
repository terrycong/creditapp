# 修复 parent/rewards.html Thymeleaf 模板解析错误（完整版）

## TL;DR

> **问题 1**：第 224 行使用 `th:onclick` 直接传递字符串变量，违反 Thymeleaf 安全机制。
> **问题 2**：第 271 行模态框表单使用 `${reward.id}`，但 `reward` 变量在模态框上下文中不存在。
> 
> **解决方案**：
> 1. 改用 `data-*` 属性存储数据，JavaScript 从 data 属性读取
> 2. 移除表单的 `th:action`，改用 JavaScript 动态设置
> 
> **预计工作量**：快速（5 分钟）

---

## Context

### 错误信息
```
错误 1: org.attoparser.ParseException: Only variable expressions returning numbers or booleans 
       are allowed in this context (line 224)
       
错误 2: org.thymeleaf.exceptions.TemplateProcessingException: Exception evaluating SpringEL 
       expression: "reward.id" (line 271)
```

---

## Work Objectives

### 核心目标
修复 Thymeleaf 模板解析错误，使礼物管理页面可以正常加载和编辑。

---

## TODOs

- [ ] 1. 修复按钮的 th:onclick（第 223-226 行）

  **修改内容**：
  ```html
  <!-- 修改前 -->
  <button class="btn btn-sm btn-outline-primary w-100 mb-2" 
          th:onclick="'openEditModal(' + ${reward.id} + ', \'' + ${reward.name} + '\', ' + ${reward.pointsRequired} + ', ' + ${reward.quantity} + ', \'' + ${reward.description} + '\')'">
  
  <!-- 修改后 -->
  <button class="btn btn-sm btn-outline-primary w-100 mb-2 edit-btn"
          th:data-id="${reward.id}"
          th:data-name="${reward.name}"
          th:data-points="${reward.pointsRequired}"
          th:data-quantity="${reward.quantity}"
          th:data-description="${reward.description} ?: ''">
  ```

- [ ] 2. 更新 JavaScript 使用事件监听器（第 302-320 行）

  **修改内容**：
  ```javascript
  // 替换整个 openEditModal 函数为事件监听器
  document.querySelectorAll('.edit-btn').forEach(function(button) {
      button.addEventListener('click', function() {
          const id = this.getAttribute('data-id');
          const name = this.getAttribute('data-name');
          const points = this.getAttribute('data-points');
          const quantity = this.getAttribute('data-quantity');
          const description = this.getAttribute('data-description');
          
          document.getElementById('editRewardId').value = id;
          document.getElementById('editName').value = name || '';
          document.getElementById('editPointsRequired').value = points || 20;
          document.getElementById('editDescription').value = description || '';
          document.getElementById('editRewardForm').action = '/parent/rewards/' + id + '/edit';
          
          editModal.show();
      });
  });
  ```

- [ ] 3. 修复模态框表单的 th:action（第 271 行）

  **修改内容**：
  ```html
  <!-- 修改前 -->
  <form th:action="@{/parent/rewards/{id}/edit(id=${reward.id})}" method="post" id="editRewardForm">
  
  <!-- 修改后 -->
  <form method="post" id="editRewardForm">
  ```

  **原因**：`${reward}` 变量只在 `th:each` 循环中可用，模态框在循环外无法访问。

---

## Verification

```bash
# 编译
mvn clean compile -DskipTests

# 启动应用
mvn spring-boot:run

# 验证无错误
curl -s http://localhost:8080/parent/rewards | grep -c "Exception"
# 期望：0
```

---

## Commit Strategy

- **1**: `fix: 修复 rewards.html Thymeleaf 模板解析错误` — rewards.html

---

## Success Criteria

- [ ] 编译成功
- [ ] 应用启动无模板错误
- [ ] 编辑按钮可以打开模态框
- [ ] 模态框显示正确的礼物数据
