package com.github.xudli.lock.zk;

import com.github.xudli.lock.DistributedLock;

import org.apache.zookeeper.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ZookeeperDistributedLock implements DistributedLock {
    private final ZooKeeper zooKeeper;
    private final String lockPath;
    private String currentLockPath;
    private static final String LOCK_PREFIX = "/lock_";
    
    public ZookeeperDistributedLock(ZooKeeper zooKeeper, String lockPath) {
        this.zooKeeper = zooKeeper;
        this.lockPath = lockPath;
        // 确保锁的根节点存在
        try {
            if (zooKeeper.exists(lockPath, false) == null) {
                zooKeeper.create(lockPath, new byte[0], 
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            throw new RuntimeException("初始化ZK锁失败", e);
        }
    }

    @Override
    public boolean tryLock(String key, long timeout, TimeUnit unit) throws Exception {
        // 创建临时顺序节点
        String path = lockPath + LOCK_PREFIX;
        currentLockPath = zooKeeper.create(path, new byte[0], 
            ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        
        long startTime = System.currentTimeMillis();
        long millisTimeout = unit.toMillis(timeout);
        
        while (System.currentTimeMillis() - startTime < millisTimeout) {
            // 获取所有子节点并排序
            List<String> children = zooKeeper.getChildren(lockPath, false);
            Collections.sort(children);
            
            // 获取当前节点的名称
            String currentNode = currentLockPath.substring(lockPath.length() + 1);
            
            // 如果当前节点是最小的，则获取锁成功
            if (currentNode.equals(children.get(0))) {
                return true;
            }
            
            // 监听前一个节点
            int index = Collections.binarySearch(children, currentNode);
            String prevNode = lockPath + "/" + children.get(index - 1);
            
            final CountDownLatch latch = new CountDownLatch(1);
            Watcher watcher = event -> {
                if (event.getType() == Watcher.Event.EventType.NodeDeleted) {
                    latch.countDown();
                }
            };
            
            // 如果前一个节点不存在，说明已经被删除，重新循环
            if (zooKeeper.exists(prevNode, watcher) == null) {
                continue;
            }
            
            // 等待前一个节点释放锁
            if (!latch.await(millisTimeout - (System.currentTimeMillis() - startTime), 
                TimeUnit.MILLISECONDS)) {
                // 超时则删除当前节点并返回false
                zooKeeper.delete(currentLockPath, -1);
                return false;
            }
        }
        
        // 超时则删除当前节点并返回false
        zooKeeper.delete(currentLockPath, -1);
        return false;
    }

    @Override
    public void unlock(String key) throws Exception {
        // 删除当前节点即可释放锁
        if (currentLockPath != null) {
            zooKeeper.delete(currentLockPath, -1);
            currentLockPath = null;
        }
    }

    @Override
    public boolean renewLock(String key) throws Exception {
        // ZooKeeper的临时节点会自动续期，只需要保持会话存活即可
        // 返回当前节点是否存在
        return zooKeeper.exists(currentLockPath, false) != null;
    }
} 