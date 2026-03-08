package com.creditapp.config;

import com.creditapp.entity.Child;
import com.creditapp.entity.User;
import com.creditapp.entity.UserRole;
import com.creditapp.repository.ChildRepository;
import com.creditapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * Data initializer for creating default users.
 * This runs on application startup to ensure default accounts exist.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final ChildRepository childRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDefaultUsers() {
        return args -> {
            log.info("Initializing default users...");
            
            // Create default parent user
            createDefaultParent();
            
            // Create default child user
            createDefaultChild();
            
            log.info("Default users initialization complete.");
        };
    }
    
    private void createDefaultParent() {
        String username = "parent";
        String correctPasswordHash = "$2a$10$2MyFMPJGOb2L/DzwdVbZoudIkkx.nd/uVypToy.V9g2llpHBAtgJS";
        
        Optional<User> existingUser = userRepository.findByUsername(username);
        
        if (existingUser.isPresent()) {
            log.info("Default parent user '{}' already exists. Updating password...", username);
            // Update password to ensure it's correct
            User parent = existingUser.get();
            parent.setPassword(correctPasswordHash);
            userRepository.save(parent);
            log.info("Updated password for parent user: {}", username);
            return;
        }
        
        User parent = new User();
        parent.setUsername(username);
        parent.setPassword(correctPasswordHash);
        parent.setRole(UserRole.PARENT);
        parent.setPoints(0);
        
        userRepository.save(parent);
        log.info("Created default parent user: {} (password: parent123)", username);
    }
    
    private void createDefaultChild() {
        String username = "child";
        String correctPasswordHash = "$2a$10$GDRp0PUTp2ncxbWpNzn/g.mg6lFLiqWtpNIC75gU9oamFMGLucley";
        
        // Check if child already exists
        Optional<Child> existingChild = childRepository.findByUsername(username);
        if (existingChild.isPresent()) {
            log.info("Default child user '{}' already exists. Updating password...", username);
            // Update password to ensure it's correct
            Child child = existingChild.get();
            child.setPassword(correctPasswordHash);
            childRepository.save(child);
            log.info("Updated password for child user: {}", username);
            return;
        }
        
        // Find parent user
        Optional<User> parentOpt = userRepository.findByUsername("parent");
        if (parentOpt.isEmpty()) {
            log.warn("Cannot create default child: parent user not found.");
            return;
        }
        
        Child child = new Child();
        child.setUsername(username);
        child.setPassword(correctPasswordHash);
        child.setRole(UserRole.CHILD);
        child.setParent(parentOpt.get());
        child.setPoints(0);
        
        childRepository.save(child);
        log.info("Created default child user: {} (password: child123)", username);
    }
}
