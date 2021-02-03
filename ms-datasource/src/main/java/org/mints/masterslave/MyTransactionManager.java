package org.mints.masterslave;

import cn.hutool.core.collection.CollectionUtil;
import org.mints.masterslave.logger.MsLogger;
import org.mints.masterslave.strategy.DsStrategy;
import org.springframework.lang.Nullable;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import javax.persistence.EntityManagerFactory;
import java.util.Map;

import static org.mints.masterslave.strategy.DsStrategy.DsStrategyStage.AOP_TRANSACTION_STAGE;

public class MyTransactionManager extends JpaTransactionManager {
    private static final MsLogger logger = MsLogger.getLogger(MyTransactionManager.class);
    private static final ThreadLocal<Object> suspendedResources = new ThreadLocal<Object>();

    DsStrategy dsStrategy;

    public MyTransactionManager(DsStrategy dsStrategy){
        this.dsStrategy = dsStrategy;
    }


    void suspendIfNeed(Object transaction){
        Map<Object, Object> resourceMap = TransactionSynchronizationManager.getResourceMap();
        Object actualKey = obtainEntityManagerFactory();
//        Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(this.obtainEntityManagerFactory());
        if (!resourceMap.containsKey(actualKey)){
            return;
        }
        suspendedResources.set(super.doSuspend(transaction));
    }

    void resumeIfNeed(Object transaction){
        if (suspendedResources.get() != null) {
            super.doResume(transaction, suspendedResources.get());
            suspendedResources.remove();
        }
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        if (definition.isReadOnly()){
            logger.info("[事务]开始-只读 {}" , definition);
        }else {
            logger.info("[事务]开始-非只读 {}" , definition);
        }
        dsStrategy.doStrategy(AOP_TRANSACTION_STAGE, definition.isReadOnly());

        suspendIfNeed(transaction);
        super.doBegin(transaction, definition);
    }

//    @Override
//    protected void resume(@Nullable Object transaction, @Nullable AbstractPlatformTransactionManager.SuspendedResourcesHolder resourcesHolder) throws TransactionException {
//    }

    @Override
    protected void doResume(@Nullable Object transaction, Object suspendedResources){
        dsStrategy.clear(false,AOP_TRANSACTION_STAGE);
        logger.info("[事务]doResume");
        super.doResume(transaction,suspendedResources);
        resumeIfNeed(transaction);
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        dsStrategy.clear(false,AOP_TRANSACTION_STAGE);
        logger.info("[事务]doCleanupAfterCompletion");
        super.doCleanupAfterCompletion(transaction);
        resumeIfNeed(transaction);
    }

//    if (this.getDataSource() != null) {
//            ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(this.getDataSource());
//            if(conHolder != null) {
//                conHolder.unbound();
//                conHolder.setSynchronizedWithTransaction(true);
//            }
//            logger.info("[事务]断开缓存");
//        }
}