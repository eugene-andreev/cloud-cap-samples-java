package com.sap.demo.config;

import com.sap.cds.transaction.TransactionManager;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Configuration
public class CdsTransactionManager implements TransactionManager {

    @Override
    public boolean isActive() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    @Override
    public void setRollbackOnly() {
        TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
    }
}