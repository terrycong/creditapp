package com.creditapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "children")
public class Child {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer points = 0;

    @OneToMany(mappedBy = "child")
    private List<TaskCompletion> taskCompletions = new ArrayList<>();

    @OneToMany(mappedBy = "child")
    private List<RewardRedemption> rewardRedemptions = new ArrayList<>();

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public User getParent() {
        return parent;
    }

    public void setParent(User parent) {
        this.parent = parent;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public List<TaskCompletion> getTaskCompletions() {
        return taskCompletions;
    }

    public void setTaskCompletions(List<TaskCompletion> taskCompletions) {
        this.taskCompletions = taskCompletions;
    }

    public List<RewardRedemption> getRewardRedemptions() {
        return rewardRedemptions;
    }

    public void setRewardRedemptions(List<RewardRedemption> rewardRedemptions) {
        this.rewardRedemptions = rewardRedemptions;
    }
}
