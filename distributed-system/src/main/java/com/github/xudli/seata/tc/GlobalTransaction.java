package com.github.xudli.seata.tc;

import java.util.ArrayList;
import java.util.List;

public class GlobalTransaction {
    private final String xid;
    private TransactionStatus status;
    private final List<BranchTransaction> branches;
    private final long beginTime;
    
    public GlobalTransaction(String xid) {
        this.xid = xid;
        this.status = TransactionStatus.BEGIN;
        this.branches = new ArrayList<>();
        this.beginTime = System.currentTimeMillis();
    }
    
    public String getXid() {
        return xid;
    }
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
    
    public List<BranchTransaction> getBranches() {
        return branches;
    }
    
    public void addBranch(BranchTransaction branch) {
        branches.add(branch);
    }
    
    public long getBeginTime() {
        return beginTime;
    }
} 