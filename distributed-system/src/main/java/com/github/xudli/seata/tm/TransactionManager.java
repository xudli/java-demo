package com.github.xudli.seata.tm;

public interface TransactionManager {
    // 开启全局事务
    String begin() throws Exception;
    
    // 提交全局事务
    boolean commit(String xid) throws Exception;
    
    // 回滚全局事务
    boolean rollback(String xid) throws Exception;
} 