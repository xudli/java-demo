package com.github.xudli.lock;

import java.util.concurrent.TimeUnit;

public interface DistributedLock {
    // 尝试获取锁
    boolean tryLock(String key, long timeout, TimeUnit unit) throws Exception;
    
    // 释放锁
    void unlock(String key) throws Exception;
    
    // 续期锁
    boolean renewLock(String key) throws Exception;
} 