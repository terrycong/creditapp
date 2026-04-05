package com.creditapp.service;

import lombok.extern.slf4j.Slf4j;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

/**
 * 数据库备份服务（DBUnit）
 * 每天中午 12 点自动备份 MySQL 数据库所有表数据
 * 使用 DBUnit 框架导出数据为 XML 格式
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
     * 手动触发备份
     */
    public String backupDatabase() throws Exception {
        // 确保备份目录存在
        Path backupPath = Paths.get(backupDir);
        if (!Files.exists(backupPath)) {
            Files.createDirectories(backupPath);
            log.info("创建备份目录：{}", backupDir);
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupFileName = "backup_" + timestamp + ".xml.gz";
        String backupFilePath = backupDir + File.separator + backupFileName;

        log.info("开始使用 DBUnit 备份数据库...");
        
        try (Connection conn = dataSource.getConnection()) {
            // 创建 DBUnit 数据库连接
            IDatabaseConnection dbUnitConn = new DatabaseConnection(conn);
            
            // 获取整个数据库的数据集
            log.info("正在导出数据库数据...");
            IDataSet dataSet = dbUnitConn.createDataSet();
            
            // 压缩输出
            try (OutputStream outputStream = new GZIPOutputStream(new FileOutputStream(backupFilePath));
                 Writer writer = new OutputStreamWriter(outputStream, "UTF-8")) {
                
                // 使用 FlatXmlDataSet 写入 XML
                FlatXmlDataSet.write(dataSet, writer);
                
                long fileSize = Files.size(Paths.get(backupFilePath));
                log.info("数据库数据导出完成：{} ({} bytes)", backupFilePath, fileSize);
            }
        }
        
        return backupFilePath;
    }

    /**
     * 清理过期备份
     */
    private void cleanupOldBackups() {
        try {
            Path backupPath = Paths.get(backupDir);
            if (!Files.exists(backupPath)) {
                return;
            }

            Date cutoffDate = new Date(System.currentTimeMillis() - (retentionDays * 24L * 60 * 60 * 1000));
            
            Files.list(backupPath)
                .filter(path -> path.toString().endsWith(".xml.gz"))
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
