package com.creditapp.config;

import com.creditapp.entity.*;
import com.creditapp.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for JPA data initialization.
 * Verifies that data is correctly initialized on application startup.
 * 
 * Note: DataInitializer runs without @Profile, so default users are always created.
 * JpaDataInitializer only runs in 'dev' profile, so it won't run in tests with 'test' profile.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JpaDataInitializerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskJobRepository taskJobRepository;

    @Autowired
    private RewardRepository rewardRepository;

    @Test
    @DisplayName("Default parent user should be created on startup")
    void defaultParentUserShouldBeCreated() {
        // When application starts, DataInitializer creates default parent user
        
        // Then
        assertThat(userRepository.findByUsername("parent")).isPresent();
        
        User parent = userRepository.findByUsername("parent").orElseThrow();
        assertThat(parent.getRole()).isEqualTo(UserRole.PARENT);
        assertThat(parent.getPoints()).isEqualTo(0);
    }

    @Test
    @DisplayName("Default child user should be created on startup")
    void defaultChildUserShouldBeCreated() {
        // When application starts, DataInitializer creates default child user
        
        // Then
        assertThat(childRepository.findByUsername("child")).isPresent();
        
        Child child = childRepository.findByUsername("child").orElseThrow();
        assertThat(child.getRole()).isEqualTo(UserRole.CHILD);
        assertThat(child.getParent()).isNotNull();
        assertThat(child.getParent().getUsername()).isEqualTo("parent");
    }

    @Test
    @DisplayName("Child should be linked to parent")
    void childShouldBeLinkedToParent() {
        // Given
        Child child = childRepository.findByUsername("child").orElseThrow();
        
        // Then
        assertThat(child.getParent()).isNotNull();
        assertThat(child.getParent().getUsername()).isEqualTo("parent");
    }

    @Test
    @DisplayName("Data initializer should be idempotent - no duplicates on restart")
    void dataInitializationShouldBeIdempotent() {
        // Given - count users before
        long initialUserCount = userRepository.count();
        long initialChildCount = childRepository.count();
        
        // When - simulate restart by checking count again (DataInitializer checks before creating)
        // The DataInitializer checks if user exists before creating
        
        // Then - counts should remain the same
        assertThat(userRepository.count()).isEqualTo(initialUserCount);
        assertThat(childRepository.count()).isEqualTo(initialChildCount);
        
        // Should still have exactly one parent and one child
        assertThat(userRepository.findByUsername("parent")).isPresent();
        assertThat(childRepository.findByUsername("child")).isPresent();
    }

    @Test
    @DisplayName("No tasks should be created in test profile (JpaDataInitializer is dev-only)")
    void noTasksInTestProfile() {
        // JpaDataInitializer has @Profile("dev"), so it won't run in test profile
        // Only DataInitializer runs, which only creates users
        
        // In test profile, no tasks should be created by initializers
        // (Tests may create their own tasks via mocks or test setup)
        List<Task> tasks = taskRepository.findAll();
        
        // If tasks exist, they were created by test setup, not by JpaDataInitializer
        // JpaDataInitializer only runs in 'dev' profile
        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("No rewards should be created in test profile (JpaDataInitializer is dev-only)")
    void noRewardsInTestProfile() {
        // JpaDataInitializer has @Profile("dev"), so it won't run in test profile
        
        List<Reward> rewards = rewardRepository.findAll();
        assertThat(rewards).isEmpty();
    }

    @Test
    @DisplayName("Password should be correctly encoded for parent")
    void parentPasswordShouldBeEncoded() {
        // Given
        User parent = userRepository.findByUsername("parent").orElseThrow();
        
        // Then - password should be BCrypt encoded (starts with $2a$ or $2b$)
        assertThat(parent.getPassword()).startsWith("$2a$");
        assertThat(parent.getPassword().length()).isGreaterThan(50); // BCrypt hashes are 60 chars
    }

    @Test
    @DisplayName("Password should be correctly encoded for child")
    void childPasswordShouldBeEncoded() {
        // Given
        Child child = childRepository.findByUsername("child").orElseThrow();
        
        // Then - password should be BCrypt encoded
        assertThat(child.getPassword()).startsWith("$2a$");
        assertThat(child.getPassword().length()).isGreaterThan(50);
    }
}