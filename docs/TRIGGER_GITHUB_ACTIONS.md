# 触发 GitHub Actions 构建流程

## 方法一：通过 GitHub 网页手动触发（推荐）

### 步骤：

1. **访问 GitHub 仓库**
   ```
   https://github.com/terrycong/creditapp
   ```

2. **点击 "Actions" 标签**

3. **选择 "Build and Push Docker Image" 工作流**

4. **点击 "Run workflow" 按钮**

5. **选择分支**：`recover-branch`

6. **点击 "Run workflow"**

### 优点：
- ✅ 不需要配置 token
- ✅ 可以立即看到构建状态
- ✅ 可以选择任意分支

---

## 方法二：配置 GitHub Remote 后推送

### 1. 生成 GitHub Personal Access Token

访问：https://github.com/settings/tokens

创建新 token，权限：
- ✅ `repo` (Full control of private repositories)
- ✅ `workflow` (Update GitHub Action workflows)

### 2. 配置 GitHub Remote

```bash
# 添加 GitHub remote（替换 YOUR_TOKEN 为你的 token）
git remote add github https://YOUR_TOKEN@github.com/terrycong/creditapp.git

# 验证
git remote -v
```

### 3. 推送到 GitHub

```bash
git push github recover-branch
```

### 4. 查看构建状态

访问：https://github.com/terrycong/creditapp/actions

---

## 方法三：使用 Git Credential Manager

### Windows/Mac：
```bash
# 移除现有 remote
git remote remove github

# 重新添加（会弹出登录窗口）
git remote add github https://github.com/terrycong/creditapp.git

# 推送（会自动提示登录）
git push github recover-branch
```

---

## 自动触发条件

配置的工作流会在以下情况自动触发：

### 1. 推送到分支
```bash
git push origin recover-branch
git push origin master
```

### 2. 创建版本标签
```bash
git tag v1.1.0
git push origin v1.1.0
```

### 3. Pull Request
创建 PR 时自动触发

---

## 查看构建结果

### 1. 访问 Actions 页面
```
https://github.com/terrycong/creditapp/actions
```

### 2. 查看工作流运行
- 点击 "Build and Push Docker Image"
- 查看实时日志
- 下载构建产物

### 3. 查看 Docker 镜像
```
https://github.com/terrycong/creditapp/pkgs/container/creditapp
```

---

## 常见问题

### Q: 推送失败 "could not read Username"
**A**: 需要配置认证
```bash
# 方法 1: 使用 token
git remote add github https://YOUR_TOKEN@github.com/terrycong/creditapp.git

# 方法 2: 使用 SSH
git remote add github git@github.com:terrycong/creditapp.git
```

### Q: 权限不足 "403 Forbidden"
**A**: 检查 token 权限
- 确保有 `repo` 和 `workflow` 权限
- 确保是仓库 collaborator

### Q: Actions 没有触发
**A**: 检查
- 工作流文件是否在 `.github/workflows/` 目录
- 分支名是否匹配（recover-branch, master）
- 是否有语法错误

---

## 快速命令

```bash
# 查看 remote
git remote -v

# 添加 GitHub remote
git remote add github https://github.com/terrycong/creditapp.git

# 推送到 GitHub（需要 token）
git push github recover-branch

# 查看最新提交
git log --oneline -1

# 查看当前分支
git branch
```

---

## 下一步

构建成功后：

### 1. 拉取镜像
```bash
docker pull ghcr.io/terrycong/creditapp:recover-branch
```

### 2. 运行容器
```bash
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  ghcr.io/terrycong/creditapp:recover-branch
```

### 3. 验证健康检查
```bash
curl http://localhost:8080/actuator/health
```
