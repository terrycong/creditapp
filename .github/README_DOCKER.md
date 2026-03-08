# Docker 镜像构建说明

## 私有镜像配置

本项目的 Docker 镜像通过 GitHub Actions 自动构建，并推送到 **GitHub Container Registry (GHCR)**。

### 镜像隐私性

- ✅ **镜像默认私有** - 只有仓库有访问权限的用户才能拉取镜像
- ✅ **构建过程私有** - GitHub Actions 的构建日志仅仓库成员可见
- ✅ **访问控制** - 通过 GitHub 权限系统管理镜像访问

### 使用步骤

1. **在 GitHub 创建私有仓库**
   ```bash
   # 创建新的私有仓库，然后将代码推送过去
   git remote add github https://github.com/YOUR_USERNAME/creditapp.git
   git push -u github master
   ```

2. **自动触发构建**
   - 推送代码到 `master` 分支会自动触发构建
   - 或在 GitHub Actions 页面手动触发

3. **拉取私有镜像**
   ```bash
   # 需要先登录 GHCR
   docker login ghcr.io -u YOUR_USERNAME
   
   # 拉取镜像
   docker pull ghcr.io/YOUR_USERNAME/creditapp:latest
   ```

### 镜像访问权限管理

在 GitHub 仓库设置中：
1. Settings → Packages → Container registry
2. 点击镜像 → Package settings
3. 在 "Manage access" 中添加/移除有权限的用户

### 镜像标签

- `latest` - 最新稳定版本
- `master` - master 分支最新构建
- `<sha>` - 具体 commit SHA 对应的构建
