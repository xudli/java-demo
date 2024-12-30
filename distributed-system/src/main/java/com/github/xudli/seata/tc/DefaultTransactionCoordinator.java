package com.github.xudli.seata.tc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class DefaultTransactionCoordinator implements TransactionCoordinator {
    
    // 全局事务记录
    private final Map<String, GlobalTransaction> globalTransactions = new ConcurrentHashMap<>();
    // 分支事务记录
    private final Map<Long, BranchTransaction> branchTransactions = new ConcurrentHashMap<>();
    
    @Override
    public String begin() throws Exception {
        String xid = UUID.randomUUID().toString();
        GlobalTransaction global = new GlobalTransaction(xid);
        globalTransactions.put(xid, global);
        return xid;
    }
    
    @Override
    public boolean commit(String xid) throws Exception {
        GlobalTransaction global = globalTransactions.get(xid);
        if (global == null) {
            throw new Exception("Global transaction not exist: " + xid);
        }
        
        try {
            // 提交所有分支事务
            for (BranchTransaction branch : global.getBranches()) {
                branch.getResourceManager().commit(branch.getBranchId());
            }
            globalTransactions.remove(xid);
            return true;
        } catch (Exception e) {
            // 如果提交失败，触发回滚
            rollback(xid);
            throw e;
        }
    }
    
    @Override
    public boolean rollback(String xid) throws Exception {
        GlobalTransaction global = globalTransactions.get(xid);
        if (global == null) {
            throw new Exception("Global transaction not exist: " + xid);
        }
        
        // 回滚所有分支事务
        for (BranchTransaction branch : global.getBranches()) {
            try {
                branch.getResourceManager().rollback(branch.getBranchId());
            } catch (Exception e) {
                // 记录回滚失败的分支
                e.printStackTrace();
            }
        }
        
        globalTransactions.remove(xid);
        return true;
    }
    
    @Override
    public long registerBranch(String xid, String resourceId, String lockKeys) throws Exception {
        GlobalTransaction global = globalTransactions.get(xid);
        if (global == null) {
            throw new Exception("Global transaction not exist: " + xid);
        }
        
        long branchId = generateBranchId();
        BranchTransaction branch = new BranchTransaction(branchId, xid, resourceId);
        global.addBranch(branch);
        branchTransactions.put(branchId, branch);
        return branchId;
    }
    
    @Override
    public void reportBranchStatus(String xid, long branchId, BranchStatus status) throws Exception {
        BranchTransaction branch = branchTransactions.get(branchId);
        if (branch != null) {
            branch.setStatus(status);
        }
    }
    
    private long generateBranchId() {
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }
} 