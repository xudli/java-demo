# 分布式系统组件实现

本项目包含三个主要部分：分布式锁实现、简易版 Seata 分布式事务框架和 Raft 一致性算法实现。

## 一、分布式锁实现

### 1. 核心接口

``` java
public interface DistributedLock {
   boolean tryLock(String key, long timeout, TimeUnit unit) throws Exception;
   void unlock(String key) throws Exception;
   boolean renewLock(String key) throws Exception;
}

```

### 2. Redis实现

- 基于 SETNX 命令实现互斥
- 使用 Lua 脚本保证原子性
- 支持锁超时和续期机制
- 通过 UUID 识别锁持有者

### 3. ZooKeeper实现

- 基于临时顺序节点实现互斥
- 利用 Watcher 机制实现等待
- 通过临时节点特性自动处理节点失效
- 支持公平锁（按序获取）

## 二、简易版 Seata 框架

### 1. 核心组件

#### TC (Transaction Coordinator)

``` java
public interface TransactionCoordinator {
   String begin() throws Exception;
   boolean commit(String xid) throws Exception;
   boolean rollback(String xid) throws Exception;
   long registerBranch(String xid, String resourceId, String lockKeys) throws Exception;
   void reportBranchStatus(String xid, long branchId, BranchStatus status) throws Exception;
}
```

#### TM (Transaction Manager)

``` java
public interface TransactionManager {
   String begin() throws Exception;
   boolean commit(String xid) throws Exception;
   boolean rollback(String xid) throws Exception;
}
```

#### RM (Resource Manager)

```java
public interface ResourceManager {
   boolean commit(long branchId) throws Exception;
   boolean rollback(long branchId) throws Exception;
   boolean prepareBranch(long branchId) throws Exception;
}
```

### 2. 事务状态管理

#### 全局事务状态

```java
public enum TransactionStatus {
    BEGIN, // 事务开始
    COMMITTING, // 提交中
    COMMITTED, // 已提交
    ROLLING_BACK, // 回滚中
    ROLLED_BACK, // 已回滚
    TIMEOUT, // 超时
    FINISHED // 已完成
}
```

#### 分支事务状态

```java
public enum BranchStatus {
    REGISTERED, // 已注册
    COMMITTING, // 提交中
    COMMITTED, // 已提交
    ROLLING_BACK, // 回滚中
    ROLLED_BACK, // 已回滚
    TIMEOUT, // 超时
    FAILED // 失败
}
```

### 3. 使用方式

#### 注解方式

```java

@Service
public class OrderService {
    @Autowired
    private AccountService accountService;
    @Autowired
    private ProductService productService;

    @GlobalTransactional
    public void createOrder(String userId, String productId, int amount) {
        productService.reduceStock(productId, amount);
        accountService.deductBalance(userId, amount);
        createOrderRecord(userId, productId, amount);
    }
}
```

### 4. 实现特点

#### 已实现功能

- 全局事务的开启、提交和回滚
- 分支事务的注册和状态管理
- 基于注解的声明式事务
- 事务状态跟踪
- 基本的异常处理

#### 简化部分

- 使用内存存储代替持久化
- 省略了网络通信层
- 简化了锁机制
- 未实现事务恢复

### 5. 待优化方向

1. 存储层
    - 添加数据库持久化
    - 实现事务日志

2. 锁机制
    - 实现全局锁表
    - 添加死锁检测

3. 可用性
    - TC 集群化
    - 故障转移机制

4. 性能优化
    - 事务并发处理
    - 异步提交机制

5. 监控运维
    - 事务状态监控
    - 性能指标收集

## 三、Raft 一致性算法实现

### 1. 算法简介

Raft 是一种分布式一致性算法，相比 Paxos 更容易理解和实现。主要特点：

1. 领导者选举（Leader Election）
2. 日志复制（Log Replication）
3. 安全性（Safety）

### 2. 核心概念

- 节点状态：Follower、Candidate、Leader
- 任期编号：Term
- 心跳机制：Heartbeat
- 超时机制：Election Timeout

### 3. 实现架构

#### 3.1 核心组件

##### 节点状态管理 (NodeState.java)

```java
public enum NodeState {
    FOLLOWER,    // 跟随者
    CANDIDATE,   // 候选人
    LEADER       // 领导者
}
```

##### 日志条目 (LogEntry.java)

```java
public class LogEntry {
    private final int term;      // 任期号
    private final String command; // 命令内容
    private final int index;     // 日志索引
}
```

#### 3.2 核心机制

##### 选举机制

1. 选举超时随机化（150ms-300ms）
2. 状态转换：Follower -> Candidate -> Leader
3. 多数投票原则
4. 任期号递增

##### 心跳机制

1. 固定间隔（100ms）发送心跳
2. 重置选举超时
3. 维持领导者地位

#### 3.3 并发控制

1. 状态保护：使用 ReentrantLock 和 volatile 变量
2. 定时器管理：使用 ScheduledExecutorService
3. 原子操作：使用 AtomicInteger

### 4. 简化部分

1. 网络通信
   - 使用模拟的 RPC 调用
   - 未实现实际的网络传输

2. 日志复制
   - 仅实现了日志结构
   - 未实现日志复制和提交

3. 持久化
   - 使用内存存储
   - 未实现持久化机制

### 5. 待优化方向

1. 网络层
   - 实现真实的 RPC 通信
   - 添加网络超时和重试机制

2. 日志复制
   - 实现完整的日志复制
   - 添加日志压缩机制

3. 持久化
   - 添加状态持久化
   - 实现崩溃恢复

4. 成员变更
   - 实现动态成员变更
   - 添加配置变更日志

### 6. 使用示例

``` java
// 创建三节点集群
RaftNode node1 = new RaftNode("node1", Arrays.asList("node2", "node3"));
RaftNode node2 = new RaftNode("node2", Arrays.asList("node1", "node3"));
RaftNode node3 = new RaftNode("node3", Arrays.asList("node1", "node2"));

// 等待选举完成
TimeUnit.SECONDS.sleep(2);

// 验证领导者选举
int leaderCount = 0;
if (node1.getState() == NodeState.LEADER) leaderCount++;
if (node2.getState() == NodeState.LEADER) leaderCount++;
if (node3.getState() == NodeState.LEADER) leaderCount++;
assertEquals(1, leaderCount);

// 关闭节点
node1.shutdown();
node2.shutdown();
node3.shutdown();
```

### 7. 注意事项

1. 这是一个教学演示版本，简化了许多实现细节
2. 不建议在生产环境中使用
3. 完整的 Raft 实现需要考虑更多边界情况
4. 建议参考 etcd 等成熟的 Raft 实现

## 环境要求

- JDK 8+
- Spring Framework 6.1.8
- Redis (可选)
- ZooKeeper 3.7.1 (可选)
