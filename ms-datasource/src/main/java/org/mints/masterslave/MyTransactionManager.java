package org.mints.masterslave;

import org.mints.masterslave.logger.MsLogger;
import org.mints.masterslave.strategy.DsStrategy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;

import static org.mints.masterslave.strategy.DsStrategy.DsStrategyStage.AOP_TRANSACTION_STAGE;

public class MyTransactionManager extends JpaTransactionManager {
    private static final MsLogger logger = MsLogger.getLogger(MyTransactionManager.class);

    DsStrategy dsStrategy;

    public MyTransactionManager(DsStrategy dsStrategy){
        this.dsStrategy = dsStrategy;
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {

//        if (definition.isReadOnly()){
//            logger.info("[事务]开始-只读 {}" , definition);
//        }else {
//            logger.info("[事务]开始-非只读 {}" , definition);
//        }

        dsStrategy.doStrategy(AOP_TRANSACTION_STAGE, definition.isReadOnly());

        super.doBegin(transaction, definition);
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {

        dsStrategy.clear(false,AOP_TRANSACTION_STAGE);
//        logger.info("[事务]结束");
        super.doCleanupAfterCompletion(transaction);
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