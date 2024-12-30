package com.github.xudli.seata.tc;

public enum BranchStatus {
    REGISTERED,      // 已注册
    COMMITTING,      // 提交中
    COMMITTED,       // 已提交
    ROLLING_BACK,    // 回滚中
    ROLLED_BACK,     // 已回滚
    TIMEOUT,         // 超时
    FAILED           // 失败
} 