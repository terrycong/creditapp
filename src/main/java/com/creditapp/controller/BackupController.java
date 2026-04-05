package com.creditapp.controller;

import com.creditapp.service.DatabaseBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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
     * 健康检查接口（无需权限）
     * GET /api/backup/health
     */
    @PostMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("backupEnabled", true);
        response.put("schedule", "每天 12:00 自动执行");
        return ResponseEntity.ok(response);
    }
}
