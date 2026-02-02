package com.creditapp.entity;

public enum NotificationStatus {
    PENDING,     // 待处理（家长还未决定是否扣分）
    APPLIED,     // 已扣分
    DISMISSED,   // 家长忽略（不扣分）
    EXPIRED      // 已过期（不再处理）
}
