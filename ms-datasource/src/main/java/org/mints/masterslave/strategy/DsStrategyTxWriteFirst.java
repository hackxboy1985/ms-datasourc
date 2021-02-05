package org.mints.masterslave.strategy;

import org.mints.masterslave.datasource.DynamicDataSourceContextPriorityWrapper;
import org.mints.masterslave.logger.MsLogger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 写事务优先策略:出现写事务，则后续数据源不变
 * @desc 优先级见本类NormalPriority枚举,值越大，优先级越高
 */
public class DsStrategyTxWriteFirst implements DsStrategy {
    private static final MsLogger LOG = MsLogger.getLogger(DsStrategyTxWriteFirst.class);
//    private static final Marker DS_MARKER = MarkerFactory.getMarker("DS");

    enum NormalPriority{
        AOP_INVOKE_METHOD_STAGE(1),//方法名切面
        AOP_ANNOTATION_STAGE(2),//注解切面
        AOP_TRANSACTION_STAGE(3);//事务切面

        int priority;
        NormalPriority(int priority){
            this.priority=priority;
        }
    }

    int getPriority(DsStrategyStage stage){
        if (stage == null)
            return 0;
        return NormalPriority.valueOf(stage.name()).priority;
    }

    @Override
    public void doStrategy(DsStrategyStage stage, boolean read, String desc) {
        int priority = getPriority(stage);
        Integer current = DynamicDataSourceContextPriorityWrapper.getPriority();

        if (DynamicDataSourceContextPriorityWrapper.getTxWriteStatus()){
            //TODO:当前已是写事务状态,使用主库
            LOG.info("[ms-ds]{} 存在写事务，使用MASTER {} 事务:{}", stage, desc, TransactionSynchronizationManager.isActualTransactionActive());
            return;
        }

        if (priority > current) {

            if (read == false && priority == NormalPriority.AOP_TRANSACTION_STAGE.priority) {
                //TODO:写事务优先策略下，强制
                DynamicDataSourceContextPriorityWrapper.setTxWriteStatus();
                DynamicDataSourceContextPriorityWrapper.setMaster(stage.name(), priority,desc);
                return;
            }

            if (read) {
                DynamicDataSourceContextPriorityWrapper.setSlave(stage.name(), priority,desc);
            } else {
                DynamicDataSourceContextPriorityWrapper.setMaster(stage.name(), priority,desc);
            }
        }
    }

    /**
     * 清除数据源
     * @desc 根据stage的优先级决定是否能清除，如果当前所于写事务状态，无须清除，如果isRequestOver代表所有请求结束，必须清除所有线程相关数据
     * @param isRequestOver 是否请求结束
     * @param stage 阶段
     */
    @Override
    public void clear(boolean isRequestOver, DsStrategyStage stage){
        int priority = getPriority(stage);
        Integer current = DynamicDataSourceContextPriorityWrapper.getPriority();

        if (isRequestOver) {
            DynamicDataSourceContextPriorityWrapper.clear(isRequestOver);
        } else{
            if (DynamicDataSourceContextPriorityWrapper.getTxWriteStatus()){
                return;
            }
            if (priority >= current) {
                DynamicDataSourceContextPriorityWrapper.clear(isRequestOver);
            }
        }
    }
}
