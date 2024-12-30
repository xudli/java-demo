package com.github.xudli.lock.redis;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.github.xudli.lock.DistributedLock;
import com.github.xudli.lock.watchdog.WatchDog;

import io.lettuce.core.api.StatefulRedisConnection;

public class RedisDistributedLock implements DistributedLock {
    private final StatefulRedisConnection<String, String> connection;
    private final WatchDog watchDog;
    private final String lockValue;
    
    public RedisDistributedLock(StatefulRedisConnection<String, String> connection) {
        this.connection = connection;
        this.watchDog = WatchDog.getInstance();
        this.lockValue = UUID.randomUUID().toString();
    }
    
    @Override
    public boolean tryLock(String key, long timeout, TimeUnit unit) throws Exception {
        long millisToWait = unit.toMillis(timeout);
        long startMillis = System.currentTimeMillis();
        
        // 转换为秒，向上取整
        int expireSeconds = (int) Math.ceil(unit.toSeconds(timeout));
        
        do {
            boolean acquired = acquireLock(key, expireSeconds);
            if (acquired) {
                // 获取锁成功，启动自动续期
                watchDog.watchLock(key, lockValue, expireSeconds, this::renewLock);
                return true;
            }
            
            // 计算剩余等待时间
            long remainingMillis = millisToWait - (System.currentTimeMillis() - startMillis);
            if (remainingMillis <= 0) {
                return false;
            }
            
            // 短暂休眠后重试
            Thread.sleep(Math.min(100, remainingMillis));
        } while (true);
    }
    
    @Override
    public void unlock(String key) throws Exception {
        // 停止自动续期
        watchDog.removeLock(key);
        
        // 使用Lua脚本确保原子性
        String script = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";
        
        connection.sync().eval(script, 
            io.lettuce.core.ScriptOutputType.INTEGER,
            new String[]{key},
            lockValue);
    }
    
    @Override
    public boolean renewLock(String key) throws Exception {
        return renewLock(key, lockValue, (int) TimeUnit.SECONDS.toSeconds(30)); // 默认续期30秒
    }
    
    private boolean acquireLock(String key, int expireSeconds) {
        // 使用SET命令尝试获取锁
        String result = connection.sync().set(key, lockValue, 
            io.lettuce.core.SetArgs.Builder.nx().ex(expireSeconds));
        return "OK".equals(result);
    }
    
    private boolean renewLock(String key, String value, int expireSeconds) {
        // 使用Lua脚本续期，确保原子性
        String script = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('expire', KEYS[1], ARGV[2]) " +
            "else " +
            "    return 0 " +
            "end";
        
        Long result = connection.sync().eval(script,
            io.lettuce.core.ScriptOutputType.INTEGER,
            new String[]{key},
            value,
            String.valueOf(expireSeconds));
        
        return result != null && result == 1;
    }
} 