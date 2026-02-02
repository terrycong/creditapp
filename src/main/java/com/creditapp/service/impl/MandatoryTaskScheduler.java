package com.creditapp.service.impl;

import com.creditapp.entity.User;
import com.creditapp.repository.UserRepository;
import com.creditapp.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 定时任务服务 - 检查强制任务截止时间
 * 每天晚上11点检查当天强制任务是否完成，向未完成的小孩的家长发送通知
 */
@Service
@RequiredArgsConstructor
public class MandatoryTaskScheduler {

    private static final Logger log = LoggerFactory.getLogger(MandatoryTaskScheduler.class);

    private final TaskService taskService;
    private final UserRepository userRepository;

    /**
     * 每天晚上11点检查强制任务截止时间
     * 对于DAILY类型的强制任务，如果当天没有完成，向家长发送通知
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public void checkDailyMandatoryTaskDeadlines() {
        log.info("开始执行强制任务截止时间检查...");

        // 获取所有家长账户
        List<User> parents = userRepository.findAll().stream()
                .filter(u -> u.getRole() == com.creditapp.entity.UserRole.PARENT)
                .toList();

        log.info("找到 {} 个家长账户需要检查", parents.size());

        for (User parent : parents) {
            try {
                taskService.checkAndNotifyMandatoryTaskDeadline(parent.getId());
                log.info("完成家长 {} 的强制任务检查", parent.getId());
            } catch (Exception e) {
                log.error("检查家长 {} 的强制任务时出错: {}", parent.getId(), e.getMessage(), e);
            }
        }

        log.info("强制任务截止时间检查完成");
    }
}
