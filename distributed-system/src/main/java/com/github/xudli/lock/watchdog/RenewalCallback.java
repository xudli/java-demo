package com.github.xudli.lock.watchdog;

/**
 * 锁续期回调接口
 */
@FunctionalInterface
public interface RenewalCallback {
    /**
     * 执行续期操作
     * @param lockKey 锁的key
     * @param lockValue 锁的值
     * @param expireSeconds 过期时间（秒）
     * @return 续期是否成功
     */
    boolean renew(String lockKey, String lockValue, int expireSeconds);
} 