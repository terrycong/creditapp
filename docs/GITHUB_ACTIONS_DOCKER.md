# GitHub Actions Docker 镜像构建指南

## 自动构建流程

本项目使用 GitHub Actions 自动构建和推送 Docker 镜像到 GitHub Container Registry (GHCR)。

## 镜像地址

构建成功后，镜像地址为：
```
ghcr.io/terrycong/creditapp:<tag>
```

### 可用标签

Docker 镜像标签基于 `pom.xml` 中的版本号：

- `1.0.0-SNAPSHOT` - recover-branch 分支（开发版本）
- `1.0.0` - 正式版本标签（pom.xml version）
- `branch-recover-branch` - 分支名称前缀
- `sha-abc1234` - Git commit SHA

**示例**：
```
pom.xml version: 1.0.0-SNAPSHOT
→ Docker 镜像：ghcr.io/terrycong/creditapp:1.0.0-SNAPSHOT

pom.xml version: 1.0.0
→ Docker 镜像：ghcr.io/terrycong/creditapp:1.0.0
```

## 触发条件

### 1. 推送分支
```bash
git push origin recover-branch
```
自动构建镜像并推送为 `ghcr.io/terrycong/creditapp:recover-branch`

### 2. 创建标签
```bash
git tag v1.0.0
git push origin v1.0.0
```
自动构建镜像并推送为 `ghcr.io/terrycong/creditapp:v1.0.0`

### 3. Pull Request
创建 PR 时自动构建测试镜像

## 使用镜像

### 1. 拉取镜像
```bash
docker pull ghcr.io/terrycong/creditapp:recover-branch
```

### 2. 运行容器
```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e MYSQL_URL=jdbc:mysql://your-db:3306/creditapp \
  -e MYSQL_USERNAME=root \
  -e MYSQL_PASSWORD=yourpassword \
  --name creditapp \
  ghcr.io/terrycong/creditapp:recover-branch
```

### 3. 使用 Docker Compose
```yaml
version: '3.8'
services:
  creditapp:
    image: ghcr.io/terrycong/creditapp:recover-branch
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - MYSQL_URL=jdbc:mysql://mysql:3306/creditapp
      - MYSQL_USERNAME=root
      - MYSQL_PASSWORD=root
    depends_on:
      - mysql
  
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: creditapp
    volumes:
      - mysql-data:/var/lib/mysql

volumes:
  mysql-data:
```

## 查看构建状态

1. 访问 GitHub 仓库
2. 点击 "Actions" 标签
3. 选择 "Build and Push Docker Image" 工作流
4. 查看构建日志和结果

## 本地测试构建

### 构建镜像
```bash
# 1. 先构建 JAR
mvn clean package -DskipTests

# 2. 构建 Docker 镜像
docker build -t creditapp:local .
```

### 运行测试
```bash
docker run -p 8080:8080 creditapp:local
```

## 镜像信息

- **基础镜像**: eclipse-temurin:21-jre-alpine
- **Java 版本**: OpenJDK 21
- **应用端口**: 8080
- **健康检查**: `/actuator/health`
- **支持平台**: linux/amd64, linux/arm64

## 故障排查

### 构建失败
1. 检查 GitHub Actions 日志
2. 确认 `pom.xml` 配置正确
3. 本地先执行 `mvn clean package` 测试

### 镜像拉取失败
```bash
# 登录 GitHub Container Registry
docker login ghcr.io -u YOUR_USERNAME -p YOUR_TOKEN

# 重试拉取
docker pull ghcr.io/terrycong/creditapp:recover-branch
```

### 容器启动失败
```bash
# 查看日志
docker logs creditapp

# 进入容器调试
docker exec -it creditapp sh
```

## 安全建议

1. **使用 Secrets**: 敏感信息使用 GitHub Secrets
2. **限制权限**: 最小化 GITHUB_TOKEN 权限
3. **定期更新**: 定期更新基础镜像和依赖
4. **扫描漏洞**: 使用 Trivy 等工具扫描镜像漏洞

## 相关文档

- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [Docker Buildx 文档](https://docs.docker.com/buildx/working-with-buildx/)
- [GHCR 文档](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
