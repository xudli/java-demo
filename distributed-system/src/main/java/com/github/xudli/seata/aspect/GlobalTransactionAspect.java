package com.github.xudli.seata.aspect;

import com.github.xudli.seata.tm.TransactionManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class GlobalTransactionAspect {
    
    private final TransactionManager transactionManager;
    
    public GlobalTransactionAspect(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    
    @Around("@annotation(com.github.xudli.seata.annotation.GlobalTransactional)")
    public Object handleGlobalTransaction(ProceedingJoinPoint point) throws Throwable {
        String xid = transactionManager.begin();
        try {
            Object result = point.proceed();
            transactionManager.commit(xid);
            return result;
        } catch (Throwable throwable) {
            transactionManager.rollback(xid);
            throw throwable;
        }
    }
} 