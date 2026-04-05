package com.creditapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 数据库备份服务（纯 Java 实现）
 * 每天中午 12 点自动备份 MySQL 数据库所有表数据
 * 不依赖 mysqldump，使用 JDBC 直接读取数据
 */
@Service
@Slf4j
public class DatabaseBackupService {

    private final DataSource dataSource;

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/creditapp}")
    private String databaseUrl;

    @Value("${spring.datasource.username:root}")
    private String databaseUsername;

    @Value("${spring.datasource.password:}")
    private String databasePassword;

    @Value("${database.backup.dir:./backups}")
    private String backupDir;

    @Value("${database.backup.retention-days:30}")
    private int retentionDays;

    @Value("${database.backup.enabled:true}")
    private boolean backupEnabled;

    public DatabaseBackupService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 每天中午 12 点执行数据库备份
     * Cron 表达式：秒 分 时 日 月 周
     * 0 0 12 * * ? = 每天 12:00:00
     */
    @Scheduled(cron = "0 0 12 * * ?")
    public void scheduledBackup() {
        if (!backupEnabled) {
            log.info("数据库备份功能已禁用，跳过备份");
            return;
        }
        
        log.info("开始执行定时数据库备份...");
        try {
            String backupFilePath = backupDatabase();
            log.info("数据库备份成功：{}", backupFilePath);
            
            // 清理过期备份
            cleanupOldBackups();
            
        } catch (Exception e) {
            log.error("数据库备份失败", e);
        }
    }

    /**
     * 手动触发备份（可通过 API 或命令行调用）
     */
    public String backupDatabase() throws IOException, SQLException {
        // 确保备份目录存在
        Path backupPath = Paths.get(backupDir);
        if (!Files.exists(backupPath)) {
            Files.createDirectories(backupPath);
            log.info("创建备份目录：{}", backupDir);
        }

        // 生成备份文件名：backup_YYYYMMDD_HHMMSS.sql
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupFileName = "backup_" + timestamp + ".sql";
        String backupFilePath = backupDir + File.separator + backupFileName;

        // 从 JDBC URL 中提取数据库名
        String databaseName = extractDatabaseName(databaseUrl);
        
        log.info("开始备份数据库：{}", databaseName);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(backupFilePath))) {
            // 写入文件头注释
            writer.write("-- MySQL Database Backup\n");
            writer.write("-- Database: " + databaseName + "\n");
            writer.write("-- Generated: " + new Date() + "\n");
            writer.write("-- Backup Type: Full (Structure + Data)\n");
            writer.write("\n");
            
            // 设置字符集
            writer.write("SET NAMES utf8mb4;\n");
            writer.write("SET FOREIGN_KEY_CHECKS = 0;\n");
            writer.write("\n");
            
            // 创建数据库（如果不存在）
            writer.write("CREATE DATABASE IF NOT EXISTS `" + databaseName + 
                        "` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\n");
            writer.write("USE `" + databaseName + "`;\n\n");
            
            // 获取所有表
            List<String> tables = getAllTables();
            log.info("找到 {} 个表需要备份", tables.size());
            
            // 备份每个表
            for (String table : tables) {
                backupTable(writer, table);
            }
            
            // 恢复外键检查
            writer.write("\nSET FOREIGN_KEY_CHECKS = 1;\n");
            
            log.info("备份完成，共备份 {} 个表", tables.size());
        }
        
        // 验证备份文件
        File backupFile = new File(backupFilePath);
        if (backupFile.exists() && backupFile.length() > 0) {
            log.info("备份文件创建成功：{} ({} bytes)", backupFilePath, backupFile.length());
            return backupFilePath;
        } else {
            throw new IOException("备份文件创建失败或为空");
        }
    }

    /**
     * 获取数据库中所有表名
     */
    private List<String> getAllTables() throws SQLException {
        List<String> tables = new ArrayList<>();
        String databaseName = extractDatabaseName(databaseUrl);
        
        try (Connection conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getTables(databaseName, null, null, new String[]{"TABLE"})) {
            
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                // 跳过 Liquibase 相关表
                if (!tableName.startsWith("DATABASECHANGELOG")) {
                    tables.add(tableName);
                }
            }
        }
        
        return tables;
    }

    /**
     * 备份单个表的结构和数据
     */
    private void backupTable(BufferedWriter writer, String tableName) throws IOException, SQLException {
        log.debug("备份表：{}", tableName);
        
        // 写入分隔符
        writer.write("-- ----------------------------\n");
        writer.write("-- Table structure for `" + tableName + "`\n");
        writer.write("-- ----------------------------\n");
        
        // 删除旧表（如果存在）
        writer.write("DROP TABLE IF EXISTS `" + tableName + "`;\n\n");
        
        // 获取表结构
        String createTableSQL = getCreateTableSQL(tableName);
        writer.write(createTableSQL);
        writer.write("\n\n");
        
        // 获取并写入数据
        writeTableData(writer, tableName);
        writer.write("\n");
    }

    /**
     * 获取创建表的 SQL 语句
     */
    private String getCreateTableSQL(String tableName) throws SQLException {
        StringBuilder createSQL = new StringBuilder();
        
        try (Connection conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, null)) {
            
            createSQL.append("CREATE TABLE `").append(tableName).append("` (\n");
            
            List<String> columns = new ArrayList<>();
            List<String> primaryKeys = new ArrayList<>();
            
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String columnType = rs.getString("TYPE_NAME");
                int columnSize = rs.getInt("COLUMN_SIZE");
                int decimalDigits = rs.getInt("DECIMAL_DIGITS");
                int nullable = rs.getInt("NULLABLE");
                String defaultValue = rs.getString("COLUMN_DEF");
                String remarks = rs.getString("REMARKS");
                String isAutoIncrementStr = rs.getString("IS_AUTOINCREMENT");
                boolean isAutoIncrement = "YES".equals(isAutoIncrementStr);
                
                StringBuilder columnDef = new StringBuilder();
                columnDef.append("  `").append(columnName).append("` ");
                columnDef.append(getJavaSQLType(columnType, columnSize, decimalDigits));
                
                if (nullable == DatabaseMetaData.columnNoNulls) {
                    columnDef.append(" NOT NULL");
                }
                
                if (defaultValue != null && !defaultValue.isEmpty()) {
                    columnDef.append(" DEFAULT ").append(defaultValue);
                }
                
                if (isAutoIncrement) {
                    columnDef.append(" AUTO_INCREMENT");
                }
                
                if (remarks != null && !remarks.isEmpty()) {
                    columnDef.append(" COMMENT '").append(remarks.replace("'", "\\'")).append("'");
                }
                
                columns.add(columnDef.toString());
            }
            
            // 获取主键
            try (ResultSet pkRs = conn.getMetaData().getPrimaryKeys(null, null, tableName)) {
                while (pkRs.next()) {
                    primaryKeys.add("`" + pkRs.getString("COLUMN_NAME") + "`");
                }
            }
            
            // 添加主键约束
            if (!primaryKeys.isEmpty()) {
                columns.add("  PRIMARY KEY (" + String.join(", ", primaryKeys) + ")");
            }
            
            createSQL.append(String.join(",\n", columns));
            createSQL.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            
            // 添加表注释
            try (ResultSet tableRs = conn.getMetaData().getTables(null, null, tableName, null)) {
                if (tableRs.next()) {
                    String remarks = tableRs.getString("REMARKS");
                    if (remarks != null && !remarks.isEmpty()) {
                        createSQL.append(" COMMENT='").append(remarks.replace("'", "\\'")).append("'");
                    }
                }
            }
            
            createSQL.append(";");
        }
        
        return createSQL.toString();
    }

    /**
     * 将 JDBC 类型映射到 MySQL 类型
     */
    private String getJavaSQLType(String jdbcType, int size, int decimalDigits) {
        switch (jdbcType.toUpperCase()) {
            case "VARCHAR":
                return "VARCHAR(" + size + ")";
            case "CHAR":
                return "CHAR(" + size + ")";
            case "TEXT":
                return "TEXT";
            case "LONGTEXT":
                return "LONGTEXT";
            case "MEDIUMTEXT":
                return "MEDIUMTEXT";
            case "TINYTEXT":
                return "TINYTEXT";
            case "INT":
            case "INTEGER":
                return "INT";
            case "BIGINT":
                return "BIGINT";
            case "SMALLINT":
                return "SMALLINT";
            case "TINYINT":
                return "TINYINT";
            case "DECIMAL":
            case "NUMERIC":
                if (decimalDigits > 0) {
                    return "DECIMAL(" + size + "," + decimalDigits + ")";
                }
                return "DECIMAL(" + size + ",0)";
            case "FLOAT":
                return "FLOAT";
            case "DOUBLE":
                return "DOUBLE";
            case "BOOLEAN":
            case "BIT":
                return "TINYINT(1)";
            case "DATE":
                return "DATE";
            case "TIME":
                return "TIME";
            case "TIMESTAMP":
                return "TIMESTAMP";
            case "DATETIME":
                return "DATETIME";
            case "BLOB":
                return "BLOB";
            case "LONGBLOB":
                return "LONGBLOB";
            case "MEDIUMBLOB":
                return "MEDIUMBLOB";
            case "BINARY":
                return "BINARY(" + size + ")";
            case "VARBINARY":
                return "VARBINARY(" + size + ")";
            default:
                return jdbcType;
        }
    }

    /**
     * 写入表数据
     */
    private void writeTableData(BufferedWriter writer, String tableName) throws IOException, SQLException {
        log.debug("导出表数据：{}", tableName);
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM `" + tableName + "`")) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // 获取列名
            List<String> columnNames = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }
            
            // 写入 INSERT 语句
            int rowCount = 0;
            int batchSize = 1000;
            List<String> batchValues = new ArrayList<>();
            
            while (rs.next()) {
                StringBuilder values = new StringBuilder();
                values.append("(");
                
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) {
                        values.append(", ");
                    }
                    
                    Object value = rs.getObject(i);
                    int columnType = metaData.getColumnType(i);
                    
                    if (value == null) {
                        values.append("NULL");
                    } else if (isNumericType(columnType)) {
                        values.append(value.toString());
                    } else if (columnType == Types.BOOLEAN || columnType == Types.BIT) {
                        values.append(rs.getBoolean(i) ? "1" : "0");
                    } else if (columnType == Types.TIMESTAMP || 
                               columnType == Types.DATE || 
                               columnType == Types.TIME) {
                        values.append("'").append(value.toString()).append("'");
                    } else {
                        // 字符串类型，需要转义
                        String strValue = value.toString();
                        strValue = strValue.replace("\\", "\\\\");
                        strValue = strValue.replace("'", "\\'");
                        strValue = strValue.replace("\n", "\\n");
                        strValue = strValue.replace("\r", "\\r");
                        strValue = strValue.replace("\t", "\\t");
                        values.append("'").append(strValue).append("'");
                    }
                }
                
                values.append(")");
                batchValues.add(values.toString());
                rowCount++;
                
                // 批量写入（每 1000 行）
                if (batchValues.size() >= batchSize) {
                    writeInsertStatement(writer, tableName, columnNames, batchValues);
                    batchValues.clear();
                }
            }
            
            // 写入剩余的行
            if (!batchValues.isEmpty()) {
                writeInsertStatement(writer, tableName, columnNames, batchValues);
            }
            
            log.debug("表 {} 导出 {} 行数据", tableName, rowCount);
        }
    }

    /**
     * 写入 INSERT 语句
     */
    private void writeInsertStatement(BufferedWriter writer, String tableName, 
                                      List<String> columnNames, List<String> values) throws IOException {
        String columns = String.join(", ", columnNames.stream()
                .map(col -> "`" + col + "`")
                .toArray(String[]::new));
        
        for (String value : values) {
            writer.write("INSERT INTO `" + tableName + "` (" + columns + ") VALUES " + value + ";\n");
        }
    }

    /**
     * 判断是否为数值类型
     */
    private boolean isNumericType(int sqlType) {
        return sqlType == Types.INTEGER ||
               sqlType == Types.BIGINT ||
               sqlType == Types.SMALLINT ||
               sqlType == Types.TINYINT ||
               sqlType == Types.DECIMAL ||
               sqlType == Types.NUMERIC ||
               sqlType == Types.FLOAT ||
               sqlType == Types.DOUBLE ||
               sqlType == Types.REAL;
    }

    /**
     * 从 JDBC URL 中提取数据库名
     */
    private String extractDatabaseName(String jdbcUrl) {
        // jdbc:mysql://host:port/database?params...
        String[] parts = jdbcUrl.split("/");
        if (parts.length > 3) {
            String dbWithParams = parts[3];
            return dbWithParams.split("\\?")[0];
        }
        return "creditapp"; // 默认值
    }

    /**
     * 清理过期的备份文件
     */
    private void cleanupOldBackups() {
        try {
            Path backupPath = Paths.get(backupDir);
            if (!Files.exists(backupPath)) {
                return;
            }

            Date cutoffDate = new Date(System.currentTimeMillis() - (retentionDays * 24L * 60 * 60 * 1000));
            
            Files.list(backupPath)
                .filter(path -> path.toString().endsWith(".sql"))
                .filter(path -> {
                    try {
                        FileTime fileTime = Files.getLastModifiedTime(path);
                        return fileTime.toMillis() < cutoffDate.getTime();
                    } catch (IOException e) {
                        log.error("读取文件时间失败：{}", path, e);
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        log.info("删除过期备份：{}", path);
                    } catch (IOException e) {
                        log.error("删除备份失败：{}", path, e);
                    }
                });
                
        } catch (IOException e) {
            log.error("清理过期备份失败", e);
        }
    }
}
