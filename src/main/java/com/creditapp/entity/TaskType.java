package com.creditapp.entity;

public enum TaskType {
    ONE_TIME,      // 一次性任务
    REPEATABLE,    // 可重复任务
    DAILY_ONCE,    // 每日一次
    MANDATORY      // 强制任务（未完成扣分）
}
