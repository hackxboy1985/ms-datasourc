package org.mints.masterslave;

import org.mints.masterslave.logger.MsLogger;
import org.mints.masterslave.strategy.DsStrategy;
import org.springframework.lang.Nullable;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

import static org.mints.masterslave.strategy.DsStrategy.DsStrategyStage.AOP_TRANSACTION_STAGE;

public class MyTransactionManager extends JpaTransactionManager {
    private static final MsLogger logger = MsLogger.getLogger(MyTransactionManager.class);
    private static final ThreadLocal<Object> suspendedResources = new ThreadLocal<Object>();

    DsStrategy dsStrategy;

    public MyTransactionManager(DsStrategy dsStrategy) {
        this.dsStrategy = dsStrategy;
    }


    void suspendIfNeed(Object transaction) {
        Map<Object, Object> resourceMap = TransactionSynchronizationManager.getResourceMap();
        Object actualKey = obtainEntityManagerFactory();
//        Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(this.obtainEntityManagerFactory());
        if (!resourceMap.containsKey(actualKey)) {
            return;
        }
        suspendedResources.set(super.doSuspend(transaction));
    }

    void resumeIfNeed(Object transaction) {
        if (suspendedResources.get() != null) {
            super.doResume(transaction, suspendedResources.get());
            suspendedResources.remove();
        }
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        if (definition.isReadOnly()) {
            logger.info("[ms-ds][事务]开始-只读 {} {}", definition, definition.getName());
        } else {
            logger.info("[ms-ds][事务]开始-非只读 {} {}", definition, definition.getName());
        }
        dsStrategy.doStrategy(AOP_TRANSACTION_STAGE, definition.isReadOnly(), definition.getName());

        suspendIfNeed(transaction);
        try {
            super.doBegin(transaction, definition);
        } catch (TransactionException e) {
            /**
             * 事务进行suspendIfNeed后如果dobegin异常而此时不resumeIfNeed进行恢复上下文，当此线程后续还有请求时，会出现资源不平衡
             * {@link TransactionSynchronizationManager#unbindResource} 会抛出异常: No value for key [org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean@6de610c6] bound to thread
             */
            resumeIfNeed(transaction);
            throw e;
        }
    }

    @Override
    protected void doResume(@Nullable Object transaction, Object suspendedResources) {
        dsStrategy.clear(false, AOP_TRANSACTION_STAGE);
        logger.info("[ms-ds][事务]结束-doResume");
        super.doResume(transaction, suspendedResources);
        resumeIfNeed(transaction);
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        dsStrategy.clear(false, AOP_TRANSACTION_STAGE);
        logger.info("[ms-ds][事务]结束-doCleanupAfterCompletion");
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

    //    public static int testTrigger = 0;
//    void testEx() {
//      if(testTrigger!=0) {
//          throw new CannotCreateTransactionException("Could not open JPA EntityManager for transaction");
//      }
//    }
}