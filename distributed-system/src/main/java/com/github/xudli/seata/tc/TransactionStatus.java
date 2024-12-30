package com.github.xudli.seata.tc;

public enum TransactionStatus {
    BEGIN,           // 事务开始
    COMMITTING,      // 提交中
    COMMITTED,       // 已提交
    ROLLING_BACK,    // 回滚中
    ROLLED_BACK,     // 已回滚
    TIMEOUT,         // 超时
    FINISHED         // 已完成
} 