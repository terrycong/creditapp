package com.creditapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 数据库备份服务
 * 每天中午 12 点自动备份 MySQL 数据库所有表数据
 */
@Service
@Slf4j
public class DatabaseBackupService {

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
    public String backupDatabase() throws IOException, InterruptedException {
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
        
        // 构建 mysqldump 命令
        List<String> command = buildMysqldumpCommand(databaseName, backupFilePath);

        log.info("执行备份命令：{}", String.join(" ", command));

        // 执行命令
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // 读取输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("mysqldump: {}", line);
            }
        }

        // 等待命令执行完成
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            File backupFile = new File(backupFilePath);
            if (backupFile.exists() && backupFile.length() > 0) {
                log.info("备份文件创建成功：{} ({} bytes)", backupFilePath, backupFile.length());
                return backupFilePath;
            } else {
                throw new IOException("备份文件创建失败或为空");
            }
        } else {
            throw new IOException("mysqldump 命令执行失败，退出码：" + exitCode);
        }
    }

    /**
     * 构建 mysqldump 命令
     */
    private List<String> buildMysqldumpCommand(String databaseName, String outputFile) {
        List<String> command = new ArrayList<>();
        
        // mysqldump 路径（如果在系统 PATH 中可直接使用）
        command.add("mysqldump");
        
        // 添加 --single-transaction 参数，确保一致性（适用于 InnoDB）
        command.add("--single-transaction");
        
        // 添加 --routines 备份存储过程和函数
        command.add("--routines");
        
        // 添加 --triggers 备份触发器
        command.add("--triggers");
        
        // 添加 --events 备份事件
        command.add("--events");
        
        // 添加 --set-gtid-purged=OFF 避免 GTID 问题（MySQL 5.7+）
        command.add("--set-gtid-purged=OFF");
        
        // 数据库连接参数
        command.add("-h");
        command.add(extractHost(databaseUrl));
        
        command.add("-P");
        command.add(extractPort(databaseUrl));
        
        command.add("-u");
        command.add(databaseUsername);
        
        if (databasePassword != null && !databasePassword.isEmpty()) {
            command.add("-p" + databasePassword);
        }
        
        // 数据库名
        command.add(databaseName);
        
        // 输出重定向
        command.add("-r");
        command.add(outputFile);
        
        return command;
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
     * 从 JDBC URL 中提取主机名
     */
    private String extractHost(String jdbcUrl) {
        // jdbc:mysql://host:port/database
        String[] parts = jdbcUrl.split("/");
        if (parts.length > 2) {
            String hostPort = parts[2];
            return hostPort.split(":")[0];
        }
        return "localhost";
    }

    /**
     * 从 JDBC URL 中提取端口
     */
    private String extractPort(String jdbcUrl) {
        // jdbc:mysql://host:port/database
        String[] parts = jdbcUrl.split("/");
        if (parts.length > 2) {
            String hostPort = parts[2];
            String[] hp = hostPort.split(":");
            return hp.length > 1 ? hp[1] : "3306";
        }
        return "3306";
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
