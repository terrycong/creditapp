# 数据库自动备份功能

## 功能说明

本功能实现每天中午 12 点自动备份 MySQL 数据库的所有表数据，使用 MySQL 官方工具 `mysqldump`。

## 特性

- ✅ 每天 12:00 自动执行
- ✅ 备份所有表结构和数据
- ✅ 包含存储过程、触发器、事件
- ✅ 自动清理过期备份（默认保留 30 天）
- ✅ 支持手动触发备份
- ✅ 使用 `--single-transaction` 确保数据一致性（InnoDB）

## 配置项

在 `application.properties` 中配置：

```properties
# 是否启用备份（默认：true）
database.backup.enabled=true

# 备份文件存储目录（默认：./backups）
database.backup.dir=./backups

# 备份保留天数（默认：30）
database.backup.retention-days=30
```

生产环境可使用环境变量：

```bash
export DB_BACKUP_ENABLED=true
export DB_BACKUP_DIR=/var/backups/creditapp
export DB_BACKUP_RETENTION=30
```

## 备份文件命名

备份文件按时间戳命名：`backup_YYYYMMDD_HHMMSS.sql`

示例：`backup_20260405_120000.sql`

## 手动触发备份

### 方式 1：API 调用

```bash
# 需要 ADMIN 权限
curl -X POST http://localhost:8080/api/backup/trigger \
  -H "Authorization: Bearer YOUR_TOKEN"
```

响应示例：
```json
{
  "success": true,
  "message": "数据库备份成功",
  "backupPath": "./backups/backup_20260405_143022.sql"
}
```

### 方式 2：代码调用

```java
@Autowired
private DatabaseBackupService backupService;

// 手动触发备份
String backupPath = backupService.backupDatabase();
```

## 恢复备份

使用 MySQL 命令行恢复：

```bash
mysql -u root -p creditapp < ./backups/backup_20260405_120000.sql
```

或在 MySQL 客户端中：

```sql
source ./backups/backup_20260405_120000.sql;
```

## 定时任务说明

使用 Spring 的 `@Scheduled` 注解，Cron 表达式：`0 0 12 * * ?`

- 秒：0
- 分：0
- 时：12（中午 12 点）
- 日：每天
- 月：每月
- 周：任意

### 修改备份时间

如需修改备份时间，编辑 `DatabaseBackupService.java`：

```java
// 每天凌晨 2 点备份
@Scheduled(cron = "0 0 2 * * ?")

// 每 6 小时备份一次
@Scheduled(cron = "0 0 */6 * * ?")

// 每周一上午 9 点备份
@Scheduled(cron = "0 0 9 ? * MON")
```

## 依赖要求

- MySQL 客户端工具（包含 `mysqldump` 命令）
- 确保 `mysqldump` 在系统 PATH 中

### 安装 MySQL 客户端

**Ubuntu/Debian:**
```bash
sudo apt-get install mysql-client
```

**CentOS/RHEL:**
```bash
sudo yum install mysql
```

**macOS:**
```bash
brew install mysql
```

## 备份目录权限

确保应用有权限写入备份目录：

```bash
# 创建备份目录
mkdir -p /var/backups/creditapp

# 设置权限
chown -R admin:admin /var/backups/creditapp
chmod 755 /var/backups/creditapp
```

## 日志查看

备份日志会输出到应用日志中：

```
2026-04-05 12:00:00 [main] INFO  DatabaseBackupService - 开始执行定时数据库备份...
2026-04-05 12:00:05 [main] INFO  DatabaseBackupService - 备份文件创建成功：./backups/backup_20260405_120000.sql (1234567 bytes)
2026-04-05 12:00:05 [main] INFO  DatabaseBackupService - 数据库备份成功：./backups/backup_20260405_120000.sql
```

## 故障排除

### 问题 1: mysqldump 命令未找到

```
java.io.IOException: Cannot run program "mysqldump": error=2, No such file or directory
```

**解决：** 安装 MySQL 客户端工具，或将 mysqldump 路径添加到 PATH。

### 问题 2: 权限不足

```
Access denied for user 'root'@'localhost'
```

**解决：** 检查数据库用户名密码配置，确保有备份权限。

### 问题 3: 备份目录不存在

**解决：** 应用会自动创建备份目录，但需确保父目录有写权限。

## 安全建议

1. **生产环境**：使用环境变量存储数据库密码
2. **备份加密**：可添加 GPG 加密步骤
3. **远程备份**：定期将备份文件传输到远程服务器
4. **监控告警**：监控备份任务执行状态，失败时发送告警
