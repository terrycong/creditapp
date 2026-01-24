package com.creditapp.service.impl;

import com.creditapp.dto.ChildDTO;
import com.creditapp.entity.Child;
import com.creditapp.entity.User;
import com.creditapp.entity.UserRole;
import com.creditapp.exception.BusinessException;
import com.creditapp.exception.ResourceNotFoundException;
import com.creditapp.repository.ChildRepository;
import com.creditapp.repository.UserRepository;
import com.creditapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final ChildRepository childRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Child createChild(Long parentId, String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("USERNAME_EXISTS", "用户名已存在");
        }

        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", parentId));

        if (parent.getRole() != UserRole.PARENT) {
            throw new BusinessException("INVALID_ROLE", "只有家长可以创建小孩账号");
        }

        Child child = new Child();
        child.setUsername(username);
        child.setPassword(passwordEncoder.encode(password));
        child.setRole(UserRole.CHILD);
        child.setParent(parent);
        child.setPoints(0);

        return childRepository.save(child);
    }

    @Override
    @Transactional
    public void adjustChildPoints(Long childId, Integer points) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child", childId));

        int newPoints = child.getPoints() + points;
        if (newPoints < 0) {
            throw new BusinessException("INSUFFICIENT_POINTS", "积分不能为负数");
        }

        child.setPoints(newPoints);
        log.info("Adjusted points for child {}: {} -> {}", childId, child.getPoints(), newPoints);
    }

    @Override
    public ChildDTO getChildById(Long id) {
        Child child = childRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Child", id));

        return ChildDTO.builder()
                .id(child.getId())
                .username(child.getUsername())
                .points(child.getPoints())
                .parentId(child.getParent() != null ? child.getParent().getId() : null)
                .parentName(child.getParent() != null ? child.getParent().getUsername() : null)
                .build();
    }

    @Override
    public List<ChildDTO> getChildrenByParentId(Long parentId) {
        List<Child> children = childRepository.findByParentId(parentId);
        return children.stream()
                .map(child -> ChildDTO.builder()
                        .id(child.getId())
                        .username(child.getUsername())
                        .points(child.getPoints())
                        .parentId(parentId)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public java.util.Optional<User> findByUsername(String username) {
        log.debug("Searching for user with username: {}", username);
        
        // First, try to find in users table
        java.util.Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            log.debug("Found user in users table: {}", username);
            return user;
        }
        
        // If not found in users table, try to find in children table
        java.util.Optional<Child> child = childRepository.findByUsername(username);
        if (child.isPresent()) {
            log.debug("Found child in children table: {}", username);
            // Convert Child to User for authentication
            User childAsUser = new User();
            childAsUser.setId(child.get().getId());
            childAsUser.setUsername(child.get().getUsername());
            childAsUser.setPassword(child.get().getPassword());
            childAsUser.setRole(child.get().getRole());
            childAsUser.setPoints(child.get().getPoints());
            childAsUser.setParent(child.get().getParent());
            return java.util.Optional.of(childAsUser);
        }
        
        log.debug("User not found in either table: {}", username);
        return java.util.Optional.empty();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
