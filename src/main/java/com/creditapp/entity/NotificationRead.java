package com.creditapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Notification read status tracking entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification_reads", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"notification_id", "child_id"}))
public class NotificationRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Column(name = "child_id", nullable = false)
    private Long childId;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        readAt = LocalDateTime.now();
    }
}
