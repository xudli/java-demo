package com.github.xudli.snowflake;

/**
 * 雪花算法ID生成器
 * 
 * 64位ID (42(毫秒)+5(机器ID)+5(业务编码)+12(重复累加))
 * |42位时间戳|5位机器ID|5位业务编码|12位序列号|
 */
public class SnowflakeIdGenerator {
    
    // 开始时间截 (2024-01-01)
    private final static long START_TIMESTAMP = 1704067200000L;
    
    // 机器ID所占的位数
    private final static long WORKER_ID_BITS = 5L;
    // 业务编码所占的位数
    private final static long BIZ_ID_BITS = 5L;
    // 序列在ID中占的位数
    private final static long SEQUENCE_BITS = 12L;
    
    // 支持的最大机器ID，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
    private final static long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    // 支持的最大业务编码，结果是31
    private final static long MAX_BIZ_ID = ~(-1L << BIZ_ID_BITS);
    
    // 机器ID向左移17位
    private final static long WORKER_ID_SHIFT = SEQUENCE_BITS + BIZ_ID_BITS;
    // 业务编码向左移12位
    private final static long BIZ_ID_SHIFT = SEQUENCE_BITS;
    // 时间截向左移22位
    private final static long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + BIZ_ID_BITS + WORKER_ID_BITS;
    
    // 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
    private final static long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    
    // 工作机器ID(0~31)
    private long workerId;
    // 业务编码(0~31)
    private long bizId;
    // 毫秒内序列(0~4095)
    private long sequence = 0L;
    // 上次生成ID的时间截
    private long lastTimestamp = -1L;
    
    /**
     * 构造函数
     * @param workerId 工作机器ID (0~31)
     * @param bizId 业务编码 (0~31)
     */
    public SnowflakeIdGenerator(long workerId, long bizId) {
        // 检查机器ID和业务编码的合法性
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                String.format("Worker ID can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (bizId > MAX_BIZ_ID || bizId < 0) {
            throw new IllegalArgumentException(
                String.format("Biz ID can't be greater than %d or less than 0", MAX_BIZ_ID));
        }
        
        this.workerId = workerId;
        this.bizId = bizId;
    }
    
    /**
     * 获得下一个ID (该方法是线程安全的)
     * @return snowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();
        
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过，应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format(
                "Clock moved backwards. Refusing to generate id for %d milliseconds",
                lastTimestamp - timestamp));
        }
        
        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            // 序列号自增，通过与运算保证序列号不会超出最大值
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 毫秒内序列溢出，阻塞到下一个毫秒
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，序列重置
            sequence = 0L;
        }
        
        // 保存上次生成ID的时间截
        lastTimestamp = timestamp;
        
        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT)  // 时间戳部分
                | (workerId << WORKER_ID_SHIFT)                        // 机器ID部分
                | (bizId << BIZ_ID_SHIFT)                             // 业务编码部分
                | sequence;                                            // 序列号部分
    }
    
    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }
    
    /**
     * 返回当前时间，以毫秒为单位
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }
} 