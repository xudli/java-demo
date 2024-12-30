package com.github.xudli.raft;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class RaftNodeTest {
    
    @Test
    public void testLeaderElection() throws InterruptedException {
        // 创建三个节点的集群
        RaftNode node1 = new RaftNode("node1", Arrays.asList("node2", "node3"));
        RaftNode node2 = new RaftNode("node2", Arrays.asList("node1", "node3"));
        RaftNode node3 = new RaftNode("node3", Arrays.asList("node1", "node2"));
        
        // 等待选举完成
        TimeUnit.SECONDS.sleep(2);
        
        // 确保只有一个领导者
        int leaderCount = 0;
        String leaderId = null;
        
        if (node1.getState() == NodeState.LEADER) {
            leaderCount++;
            leaderId = "node1";
        }
        if (node2.getState() == NodeState.LEADER) {
            leaderCount++;
            leaderId = "node2";
        }
        if (node3.getState() == NodeState.LEADER) {
            leaderCount++;
            leaderId = "node3";
        }
        
        assertEquals(1, leaderCount, "Should have exactly one leader");
        
        // 确保其他节点认可这个领导者
        if (leaderId != null) {
            if (!leaderId.equals("node1")) {
                assertEquals(NodeState.FOLLOWER, node1.getState());
                assertEquals(leaderId, node1.getCurrentLeader());
            }
            if (!leaderId.equals("node2")) {
                assertEquals(NodeState.FOLLOWER, node2.getState());
                assertEquals(leaderId, node2.getCurrentLeader());
            }
            if (!leaderId.equals("node3")) {
                assertEquals(NodeState.FOLLOWER, node3.getState());
                assertEquals(leaderId, node3.getCurrentLeader());
            }
        }
        
        // 清理资源
        node1.shutdown();
        node2.shutdown();
        node3.shutdown();
    }
} 