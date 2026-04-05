package com.creditapp.controller;

import com.creditapp.service.DatabaseBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 数据库备份控制器
 * 提供手动触发备份的 API 接口
 */
@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
@Slf4j
public class BackupController {

    private final DatabaseBackupService backupService;

    @Value("${database.backup.dir:./backups}")
    private String backupDir;

    /**
     * 手动触发数据库备份
     * POST /api/backup/trigger
     */
    @PostMapping("/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> triggerBackup() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("手动触发数据库备份请求");
            String backupPath = backupService.backupDatabase();
            
            response.put("success", true);
            response.put("message", "数据库备份成功");
            response.put("backupPath", backupPath);
            response.put("timestamp", new Date());
            
            log.info("手动备份完成：{}", backupPath);
            
        } catch (Exception e) {
            log.error("手动备份失败", e);
            response.put("success", false);
            response.put("message", "备份失败：" + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.internalServerError().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取备份列表
     * GET /api/backup/list
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> listBackups() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Path backupPath = Paths.get(backupDir);
            if (!Files.exists(backupPath)) {
                response.put("success", true);
                response.put("backups", new ArrayList<>());
                response.put("count", 0);
                return ResponseEntity.ok(response);
            }
            
            List<Map<String, Object>> backups = new ArrayList<>();
            try (Stream<Path> paths = Files.list(backupPath)) {
                paths.filter(p -> p.toString().endsWith(".xml.gz"))
                    .sorted(Comparator.comparingLong(p -> {
                        try {
                            return -Files.getLastModifiedTime(p).toMillis();
                        } catch (Exception e) {
                            return 0L;
                        }
                    }))
                    .forEach(path -> {
                        Map<String, Object> backup = new HashMap<>();
                        backup.put("filename", path.getFileName().toString());
                        backup.put("path", path.toString());
                        try {
                            backup.put("size", Files.size(path));
                            backup.put("lastModified", Files.getLastModifiedTime(path).toMillis());
                            backup.put("sizeFormatted", formatFileSize(Files.size(path)));
                            backup.put("lastModifiedFormatted", new Date(Files.getLastModifiedTime(path).toMillis()).toLocaleString());
                        } catch (Exception e) {
                            log.error("读取文件信息失败：{}", path, e);
                        }
                        backups.add(backup);
                    });
            }
            
            response.put("success", true);
            response.put("backups", backups);
            response.put("count", backups.size());
            response.put("backupDir", backupDir);
            
        } catch (Exception e) {
            log.error("获取备份列表失败", e);
            response.put("success", false);
            response.put("message", "获取备份列表失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 下载备份文件
     * GET /api/backup/download?filename=xxx
     */
    @GetMapping("/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadBackup(@RequestParam String filename) {
        try {
            Path filePath = Paths.get(backupDir, filename);
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(filePath.toFile());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(Files.size(filePath)))
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("下载备份失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除备份文件
     * DELETE /api/backup/delete?filename=xxx
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteBackup(@RequestParam String filename) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Path filePath = Paths.get(backupDir, filename);
            if (!Files.exists(filePath)) {
                response.put("success", false);
                response.put("message", "备份文件不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            Files.delete(filePath);
            response.put("success", true);
            response.put("message", "备份已删除");
            response.put("filename", filename);
            
            log.info("删除备份：{}", filename);
            
        } catch (Exception e) {
            log.error("删除备份失败", e);
            response.put("success", false);
            response.put("message", "删除失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 健康检查接口
     * GET /api/backup/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("backupEnabled", true);
        response.put("schedule", "每天 12:00 自动执行");
        response.put("backupDir", backupDir);
        return ResponseEntity.ok(response);
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024.0));
        return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
}
