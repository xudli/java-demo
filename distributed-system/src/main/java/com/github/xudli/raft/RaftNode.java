package com.github.xudli.raft;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.github.xudli.raft.rpc.VoteRequest;
import com.github.xudli.raft.rpc.VoteResponse;

/**
 * Raft 节点实现
 * 实现了领导者选举的核心功能
 */
public class RaftNode {
    // 节点基本属性
    private final String nodeId;                     // 节点唯一标识
    private volatile NodeState state;                // 当前节点状态（FOLLOWER/CANDIDATE/LEADER）
    private volatile int currentTerm;                // 当前任期号
    private volatile String votedFor;                // 当前任期投票给谁
    private final List<LogEntry> log;                // 日志条目列表
    private final List<String> peers;                // 集群中其他节点列表
    
    // 领导者相关
    private volatile String currentLeader;           // 当前领导者ID
    private final AtomicInteger commitIndex;         // 已提交的最高日志索引
    private final AtomicInteger lastApplied;         // 已应用到状态机的最高日志索引
    
    // 并发控制
    private final ReentrantLock lock;                // 用于保护状态更新的锁
    private final ScheduledExecutorService scheduler; // 定时器调度器
    private ScheduledFuture<?> electionTimer;        // 选举超时定时器
    private ScheduledFuture<?> heartbeatTimer;       // 心跳定时器
    
    // 常量配置
    private static final int ELECTION_TIMEOUT_MIN = 150;  // 最小选举超时时间(ms)
    private static final int ELECTION_TIMEOUT_MAX = 300;  // 最大选举超时时间(ms)
    private static final int HEARTBEAT_INTERVAL = 100;    // 心跳间隔时间(ms)
    
    /**
     * 构造函数
     * @param nodeId 节点ID
     * @param peers 集群中其他节点的ID列表
     */
    public RaftNode(String nodeId, List<String> peers) {
        this.nodeId = nodeId;
        this.state = NodeState.FOLLOWER;
        this.currentTerm = 0;
        this.votedFor = null;
        this.log = new ArrayList<>();
        this.peers = new ArrayList<>(peers);
        this.peers.remove(nodeId);
        
        this.currentLeader = null;
        this.commitIndex = new AtomicInteger(0);
        this.lastApplied = new AtomicInteger(0);
        
        this.lock = new ReentrantLock();
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        MessageBus.getInstance().registerNode(nodeId, this);
        resetElectionTimer();
    }
    
    /**
     * 重置选举超时定时器
     * 使用随机超时时间避免选举冲突
     */
    private void resetElectionTimer() {
        lock.lock();
        try {
            if (electionTimer != null) {
                electionTimer.cancel(false);
            }
            
            int timeout = new Random().nextInt(ELECTION_TIMEOUT_MAX - ELECTION_TIMEOUT_MIN) 
                + ELECTION_TIMEOUT_MIN;
                
            electionTimer = scheduler.schedule(this::startElection, 
                timeout, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * 开始领导者选举
     * 1. 转换为候选人状态
     * 2. 增加当前任期号
     * 3. 投票给自己
     * 4. 并行发送投票请求给其他节点
     */
    private void startElection() {
        lock.lock();
        try {
            if (state == NodeState.LEADER) {
                return;
            }
            
            // 转换为候选人状态
            state = NodeState.CANDIDATE;
            currentTerm++;
            votedFor = nodeId;
            currentLeader = null;
            
            // 获取最后一条日志信息
            int lastLogIndex = log.size() - 1;
            int lastLogTerm = lastLogIndex >= 0 ? log.get(lastLogIndex).getTerm() : 0;
            
            // 创建投票请求
            VoteRequest request = new VoteRequest(
                currentTerm,
                nodeId,
                lastLogIndex,
                lastLogTerm
            );
            
            // 收集投票
            CountDownLatch voteLatch = new CountDownLatch(peers.size());
            AtomicInteger voteCount = new AtomicInteger(1); // 包含自己的一票
            
            // 并行发送投票请求
            for (String peer : peers) {
                CompletableFuture.supplyAsync(() -> requestVote(peer, request))
                    .thenAccept(response -> {
                        if (response != null && response.getTerm() > currentTerm) {
                            stepDown(response.getTerm());
                        } else if (response != null && response.isVoteGranted()) {
                            voteCount.incrementAndGet();
                        }
                        voteLatch.countDown();
                    });
            }
            
            // 等待投票结果
            try {
                voteLatch.await(ELECTION_TIMEOUT_MIN, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            
            lock.lock();
            try {
                if (state == NodeState.CANDIDATE) {
                    if (voteCount.get() > (peers.size() + 1) / 2) {
                        becomeLeader();
                    } else {
                        state = NodeState.FOLLOWER;
                        resetElectionTimer();
                    }
                }
            } finally {
                lock.unlock();
            }
            
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * 发现更高任期时降级为跟随者
     */
    private void stepDown(int newTerm) {
        lock.lock();
        try {
            currentTerm = newTerm;
            state = NodeState.FOLLOWER;
            votedFor = null;
            resetElectionTimer();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * 处理投票请求
     * 实现了Raft论文中的投票规则
     */
    public VoteResponse handleVoteRequest(VoteRequest request) {
        lock.lock();
        try {
            if (request.getTerm() < currentTerm) {
                return new VoteResponse(currentTerm, false);
            }
            
            if (request.getTerm() > currentTerm) {
                stepDown(request.getTerm());
            }
            
            // 检查是否已经投票给其他候选人
            if (votedFor == null || votedFor.equals(request.getCandidateId())) {
                // 检查日志是否至少和自己一样新
                int lastLogIndex = log.size() - 1;
                int lastLogTerm = lastLogIndex >= 0 ? log.get(lastLogIndex).getTerm() : 0;
                
                if (request.getLastLogTerm() > lastLogTerm ||
                    (request.getLastLogTerm() == lastLogTerm && 
                     request.getLastLogIndex() >= lastLogIndex)) {
                    votedFor = request.getCandidateId();
                    resetElectionTimer();
                    return new VoteResponse(currentTerm, true);
                }
            }
            
            return new VoteResponse(currentTerm, false);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * 转换为领导者状态
     */
    private void becomeLeader() {
        state = NodeState.LEADER;
        currentLeader = nodeId;
        
        if (electionTimer != null) {
            electionTimer.cancel(false);
        }
        
        heartbeatTimer = scheduler.scheduleAtFixedRate(
            this::sendHeartbeat,
            0,
            HEARTBEAT_INTERVAL,
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * 发送心跳
     */
    private void sendHeartbeat() {
        if (state != NodeState.LEADER) {
            return;
        }
        
        for (String peer : peers) {
            System.out.println(nodeId + " sending heartbeat to " + peer);
        }
    }
    
    /**
     * 发送投票请求到指定节点
     * @param peer 目标节点ID
     * @param request 投票请求
     * @return 投票响应
     */
    private VoteResponse requestVote(String peer, VoteRequest request) {
        return MessageBus.getInstance().sendVoteRequest(nodeId, peer, request);
    }
    
    // 基本的getter方法
    public NodeState getState() { return state; }
    public int getCurrentTerm() { return currentTerm; }
    public String getCurrentLeader() { return currentLeader; }
    
    /**
     * 关闭节点
     */
    public void shutdown() {
        MessageBus.getInstance().unregisterNode(nodeId);
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
} 