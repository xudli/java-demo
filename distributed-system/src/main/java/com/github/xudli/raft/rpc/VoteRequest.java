package com.github.xudli.raft.rpc;

public class VoteRequest {
    private final int term;
    private final String candidateId;
    private final int lastLogIndex;
    private final int lastLogTerm;
    
    public VoteRequest(int term, String candidateId, int lastLogIndex, int lastLogTerm) {
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }
    
    public int getTerm() {
        return term;
    }
    
    public String getCandidateId() {
        return candidateId;
    }
    
    public int getLastLogIndex() {
        return lastLogIndex;
    }
    
    public int getLastLogTerm() {
        return lastLogTerm;
    }
} 