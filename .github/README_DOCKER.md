# Docker 镜像构建说明

## 架构说明

- **源代码**：托管在 Gitee 私有仓库 (`gitee.com/terrycong/creditapp`)
- **Docker 镜像**：通过 GitHub Actions 构建，推送到 GitHub Container Registry (GHCR) 私有仓库

### 隐私性保障

- ✅ **源代码私有** - Gitee 私有仓库，仅授权人员可访问
- ✅ **镜像私有** - GHCR 私有镜像，仅授权人员可拉取
- ✅ **构建过程私有** - GitHub Actions 日志仅仓库成员可见
- ✅ **访问控制** - 通过 Gitee 和 GitHub 权限系统分别管理

## 配置步骤

### 1. 在 GitHub 创建私有仓库

创建空的私有仓库（例如：`your-username/creditapp-docker`）

### 2. 配置 Gitee 访问令牌

在 GitHub 仓库设置中添加 Secret：
1. Settings → Secrets and variables → Actions
2. New repository secret
3. 添加：
   - Name: `GITEE_TOKEN`
   - Value: [在 Gitee 生成个人访问令牌](https://gitee.com/profile/personal_access_tokens)

### 3. 推送 GitHub Actions 配置

```bash
cd /home/admin/.openclaw/workspace/creditapp
git add .github
git commit -m "feat: 添加 GitHub Actions Docker 构建配置"
git push origin master
```

### 4. 手动触发首次构建

在 GitHub 仓库的 Actions 页面，点击 "Build and Push Docker Image" → "Run workflow"

### 5. 拉取私有镜像

```bash
# 登录 GHCR
docker login ghcr.io -u YOUR_GITHUB_USERNAME

# 拉取镜像
docker pull ghcr.io/YOUR_GITHUB_USERNAME/creditapp-docker:latest
```

## 镜像访问权限管理

在 GitHub 仓库设置中：
1. Settings → Packages → Container registry
2. 点击镜像 → Package settings
3. 在 "Manage access" 中添加/移除有权限的用户

## 镜像标签

- `latest` - 最新稳定版本
- `master` - master 分支最新构建
- `<sha>` - 具体 commit SHA 对应的构建

## 自动同步

GitHub Actions 会每天北京时间 8 点自动检查 Gitee 更新并构建（如果代码有更新）。
