package com.github.xudli.raft;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.xudli.raft.rpc.VoteRequest;
import com.github.xudli.raft.rpc.VoteResponse;

public class MessageBus {
    private static final MessageBus INSTANCE = new MessageBus();
    private final Map<String, RaftNode> nodes = new ConcurrentHashMap<>();
    
    private MessageBus() {}
    
    public static MessageBus getInstance() {
        return INSTANCE;
    }
    
    public void registerNode(String nodeId, RaftNode node) {
        nodes.put(nodeId, node);
    }
    
    public void unregisterNode(String nodeId) {
        nodes.remove(nodeId);
    }
    
    public VoteResponse sendVoteRequest(String from, String to, VoteRequest request) {
        RaftNode targetNode = nodes.get(to);
        if (targetNode != null) {
            return targetNode.handleVoteRequest(request);
        }
        return null;
    }
} 