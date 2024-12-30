package com.github.xudli.raft;

public enum NodeState {
    FOLLOWER,    // 跟随者
    CANDIDATE,   // 候选人
    LEADER       // 领导者
} 