package com.github.xudli.snowflake;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class SnowflakeIdGeneratorTest {
    
    @Test
    public void testIdGenerate() {
        SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);
        
        // 生成一些ID看看
        for (int i = 0; i < 10; i++) {
            long id = idGenerator.nextId();
            System.out.println(id);
            assertTrue(id > 0);
        }
    }
    
    @Test
    public void testIdUnique() throws InterruptedException {
        final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);
        final Set<Long> ids = new HashSet<>();
        final int COUNT = 1000;
        final int THREAD_COUNT = 10;
        
        // 使用CountDownLatch确保所有线程同时开始
        CountDownLatch latch = new CountDownLatch(1);
        // 使用CountDownLatch等待所有线程结束
        CountDownLatch end = new CountDownLatch(THREAD_COUNT);
        
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    latch.await(); // 等待开始信号
                    for (int j = 0; j < COUNT; j++) {
                        synchronized (ids) {
                            long id = idGenerator.nextId();
                            assertTrue(ids.add(id), "ID should be unique: " + id);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    end.countDown();
                }
            });
        }
        
        latch.countDown(); // 发出开始信号
        end.await(); // 等待所有线程结束
        
        assertEquals(COUNT * THREAD_COUNT, ids.size(), "Should generate expected number of unique ids");
        executorService.shutdown();
    }
} 