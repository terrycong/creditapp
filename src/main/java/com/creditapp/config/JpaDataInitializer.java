package com.creditapp.config;

import com.creditapp.entity.*;
import com.creditapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * JPA-based Data Initializer
 * Replaces SQL import scripts with type-safe Java code
 * Runs in all profiles except 'test' (to avoid interfering with tests)
 * 
 * Note: Previously this was @Profile("dev") only, which meant marketplace tasks
 * were not created in production. Now it runs in all non-test profiles.
 */
@Slf4j
@Configuration
@Profile("!test")
@RequiredArgsConstructor
public class JpaDataInitializer {

    private final UserRepository userRepository;
    private final ChildRepository childRepository;
    private final TaskRepository taskRepository;
    private final TaskJobRepository taskJobRepository;
    private final RewardRepository rewardRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            log.info("=== JPA Data Initialization Started ===");
            
            // Skip if data already exists
            if (userRepository.count() > 0) {
                log.info("Data already exists, skipping initialization");
                return;
            }

            initializeUsers();
            initializeTasks();
            initializeRewards();
            
            log.info("=== JPA Data Initialization Completed ===");
            log.info("Created {} users", userRepository.count());
            log.info("Created {} children", childRepository.count());
            log.info("Created {} tasks", taskRepository.count());
            log.info("Created {} rewards", rewardRepository.count());
        };
    }

    /**
     * Initialize default users (parent and child)
     */
    private void initializeUsers() {
        log.info("Creating default users...");

        // Create parent user
        User parent = new User();
        parent.setUsername("parent");
        parent.setPassword(passwordEncoder.encode("parent123"));
        parent.setRole(UserRole.PARENT);
        parent.setPoints(0);
        parent = userRepository.save(parent);
        log.info("Created parent user: {}", parent.getUsername());

        // Create child user
        Child child = new Child();
        child.setUsername("child");
        child.setPassword(passwordEncoder.encode("child123"));
        child.setRole(UserRole.CHILD);
        child.setParent(parent);
        child.setPoints(0);
        child = childRepository.save(child);
        log.info("Created child user: {}", child.getUsername());
    }

    /**
     * Initialize sample tasks
     */
    private void initializeTasks() {
        log.info("Creating sample tasks...");

        User parent = userRepository.findByUsername("parent")
                .orElseThrow(() -> new IllegalStateException("Parent user not found"));
        Child child = childRepository.findByUsername("child")
                .orElseThrow(() -> new IllegalStateException("Child user not found"));

        // Direct assigned tasks
        createDirectTask(parent, child, "完成作业", "按时完成学校作业", 10, TaskType.DAILY_ONCE);
        createDirectTask(parent, child, "打扫房间", "整理自己的房间", 5, TaskType.REPEATABLE);
        createDirectTask(parent, child, "阅读书籍", "每天阅读 30 分钟课外书", 8, TaskType.DAILY_ONCE);
        createDirectTask(parent, child, "帮助做家务", "帮忙洗碗或扫地", 7, TaskType.REPEATABLE);

        // Marketplace tasks (not assigned to anyone initially)
        createMarketplaceTask(parent, "洗碗一次", "帮忙洗晚餐的碗", 15, TaskType.REPEATABLE);
        createMarketplaceTask(parent, "倒垃圾", "把家里的垃圾倒掉", 5, TaskType.DAILY_ONCE);
        createMarketplaceTask(parent, "整理客厅", "整理客厅的沙发和桌子", 10, TaskType.REPEATABLE);
    }

    /**
     * Create a directly assigned task with TaskJob
     */
    private void createDirectTask(User parent, Child child, String title, String description, 
                                   int points, TaskType type) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setPoints(points);
        task.setType(type);
        task.setStatus(TaskStatus.APPROVED);
        task.setCreatedBy(parent);
        task.setActive(true);
        task = taskRepository.save(task);

        // Create TaskJob to link task to child
        TaskJob taskJob = new TaskJob();
        taskJob.setTask(task);
        taskJob.setChild(child);
        taskJob.setStatus(JobStatus.ASSIGNED);
        taskJob.setSnapshotTitle(title);
        taskJob.setSnapshotDescription(description);
        taskJob.setSnapshotPoints(points);
        taskJob.setSnapshotTaskType(type);
        taskJob.setAssignedAt(LocalDateTime.now());
        taskJobRepository.save(taskJob);

        log.info("Created direct task: {} → {}", title, child.getUsername());
    }

    /**
     * Create a marketplace task (available for any child to pick)
     */
    private void createMarketplaceTask(User parent, String title, String description, 
                                        int points, TaskType type) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setPoints(points);
        task.setType(type);
        task.setStatus(TaskStatus.APPROVED);
        task.setCreatedBy(parent);
        task.setActive(true);
        taskRepository.save(task);

        log.info("Created marketplace task: {}", title);
    }

    /**
     * Initialize sample rewards
     */
    private void initializeRewards() {
        log.info("Creating sample rewards...");

        // Entertainment rewards
        createReward("游戏时间", "30 分钟游戏时间", 999, 20);
        createReward("看电影", "选择一部喜欢的电影观看", 999, 40);
        createReward("去游乐场", "周末去游乐场玩半天", 999, 150);

        // Food rewards
        createReward("冰淇淋", "喜欢的冰淇淋一份", 999, 15);
        createReward("零花钱", "10 元零花钱", 50, 100);
        createReward("披萨大餐", "全家一起吃披萨", 999, 100);

        // Item rewards
        createReward("新玩具", "买一个喜欢的玩具", 999, 500);
        createReward("图书", "购买一本喜欢的图书", 999, 40);
        createReward("文具套装", "获得一套新文具", 999, 50);

        // Privilege rewards
        createReward("选择周末活动", "决定周末全家去哪里玩", 999, 80);
        createReward("晚睡 1 小时特权", "周末可以晚睡 1 小时", 999, 40);
        createReward("免做家务一次", "可以免除一次家务任务", 999, 25);
    }

    /**
     * Create a reward
     */
    private void createReward(String name, String description, int quantity, int pointsRequired) {
        Reward reward = new Reward();
        reward.setName(name);
        reward.setDescription(description);
        reward.setQuantity(quantity);
        reward.setPointsRequired(pointsRequired);
        reward.setActive(true);
        rewardRepository.save(reward);
        log.info("Created reward: {} ({} points)", name, pointsRequired);
    }
}
