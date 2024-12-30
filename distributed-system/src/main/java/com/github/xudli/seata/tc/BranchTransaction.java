package com.github.xudli.seata.tc;

import com.github.xudli.seata.rm.ResourceManager;

public class BranchTransaction {
    private final long branchId;
    private final String xid;
    private final String resourceId;
    private BranchStatus status;
    private ResourceManager resourceManager;
    
    public BranchTransaction(long branchId, String xid, String resourceId) {
        this.branchId = branchId;
        this.xid = xid;
        this.resourceId = resourceId;
        this.status = BranchStatus.REGISTERED;
    }
    
    public long getBranchId() {
        return branchId;
    }
    
    public String getXid() {
        return xid;
    }
    
    public String getResourceId() {
        return resourceId;
    }
    
    public BranchStatus getStatus() {
        return status;
    }
    
    public void setStatus(BranchStatus status) {
        this.status = status;
    }
    
    public ResourceManager getResourceManager() {
        return resourceManager;
    }
    
    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }
} 