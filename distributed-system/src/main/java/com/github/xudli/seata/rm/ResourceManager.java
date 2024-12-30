package com.github.xudli.seata.rm;

public interface ResourceManager {
    // 分支事务提交
    boolean commit(long branchId) throws Exception;
    
    // 分支事务回滚
    boolean rollback(long branchId) throws Exception;
    
    // 准备分支事务的资源（如加锁等）
    boolean prepareBranch(long branchId) throws Exception;
} 