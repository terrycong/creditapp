package com.creditapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 备份管理页面控制器
 */
@Controller
@RequestMapping("/admin")
@Slf4j
public class BackupPageController {

    /**
     * 数据库备份管理页面
     * GET /admin/backups
     */
    @GetMapping("/backups")
    @PreAuthorize("hasRole('ADMIN')")
    public String backupsPage() {
        return "admin/backups";
    }
}
