package com.github.xudli.seata.tc;

import java.util.List;

public interface TransactionCoordinator {
    // 开启全局事务
    String begin() throws Exception;
    
    // 提交全局事务
    boolean commit(String xid) throws Exception;
    
    // 回滚全局事务
    boolean rollback(String xid) throws Exception;
    
    // 注册分支事务
    long registerBranch(String xid, String resourceId, String lockKeys) throws Exception;
    
    // 报告分支事务状态
    void reportBranchStatus(String xid, long branchId, BranchStatus status) throws Exception;
} 