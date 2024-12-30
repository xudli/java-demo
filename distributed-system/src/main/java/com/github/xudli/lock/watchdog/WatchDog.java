package com.github.xudli.lock.watchdog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁看门狗
 * 负责监控锁的存活状态并自动续期
 */
public class WatchDog {
    // 单例模式
    private static final WatchDog INSTANCE = new WatchDog();
    
    // 续期任务调度器
    private final ScheduledExecutorService scheduler;
    
    // 存储所有被监控的锁
    // key: lockKey, value: 续期任务
    private final Map<String, RenewalTask> renewalTasks;
    
    // 默认续期间隔（锁过期时间的1/3）
    private static final double RENEWAL_INTERVAL_RATIO = 1.0 / 3.0;
    
    private WatchDog() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.renewalTasks = new ConcurrentHashMap<>();
    }
    
    public static WatchDog getInstance() {
        return INSTANCE;
    }
    
    /**
     * 添加要监控的锁
     * @param lockKey 锁的key
     * @param lockValue 锁的值（用于验证身份）
     * @param expireSeconds 锁的过期时间（秒）
     * @param renewalCallback 续期回调函数
     */
    public void watchLock(String lockKey, String lockValue, int expireSeconds, RenewalCallback renewalCallback) {
        // 计算续期间隔
        long intervalMillis = (long) (expireSeconds * 1000 * RENEWAL_INTERVAL_RATIO);
        
        // 创建续期任务
        RenewalTask task = new RenewalTask(
            lockKey,
            lockValue,
            expireSeconds,
            renewalCallback,
            scheduler.scheduleAtFixedRate(
                () -> doRenewal(lockKey, lockValue, expireSeconds, renewalCallback),
                intervalMillis,
                intervalMillis,
                TimeUnit.MILLISECONDS
            )
        );
        
        // 保存任务
        RenewalTask oldTask = renewalTasks.put(lockKey, task);
        if (oldTask != null) {
            oldTask.cancel();
        }
    }
    
    /**
     * 移除对锁的监控
     * @param lockKey 锁的key
     */
    public void removeLock(String lockKey) {
        RenewalTask task = renewalTasks.remove(lockKey);
        if (task != null) {
            task.cancel();
        }
    }
    
    /**
     * 执行续期操作
     */
    private void doRenewal(String lockKey, String lockValue, int expireSeconds, RenewalCallback renewalCallback) {
        try {
            boolean success = renewalCallback.renew(lockKey, lockValue, expireSeconds);
            if (!success) {
                // 续期失败，可能锁已经不存在，移除监控
                removeLock(lockKey);
            }
        } catch (Exception e) {
            // 续期异常，记录日志但不中断续期任务
            System.err.println("Failed to renew lock: " + lockKey + ", error: " + e.getMessage());
        }
    }
    
    /**
     * 关闭看门狗
     */
    public void shutdown() {
        // 取消所有续期任务
        renewalTasks.values().forEach(RenewalTask::cancel);
        renewalTasks.clear();
        
        // 关闭调度器
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 续期任务包装类
     */
    private static class RenewalTask {
        private final String lockKey;
        private final String lockValue;
        private final int expireSeconds;
        private final RenewalCallback renewalCallback;
        private final ScheduledFuture<?> future;
        
        public RenewalTask(String lockKey, String lockValue, int expireSeconds, 
                          RenewalCallback renewalCallback, ScheduledFuture<?> future) {
            this.lockKey = lockKey;
            this.lockValue = lockValue;
            this.expireSeconds = expireSeconds;
            this.renewalCallback = renewalCallback;
            this.future = future;
        }
        
        public void cancel() {
            if (future != null && !future.isCancelled()) {
                future.cancel(false);
            }
        }
    }
} 