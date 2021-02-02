package org.mints.masterslave.strategy;

import org.mints.masterslave.datasource.DynamicDataSourceContextPriorityWrapper;
import org.mints.masterslave.logger.MsLogger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

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
    public void doStrategy(DsStrategyStage stage, boolean read) {
        int priority = getPriority(stage);
        Integer current = DynamicDataSourceContextPriorityWrapper.getPriority();

        if (DynamicDataSourceContextPriorityWrapper.getTxWriteStatus()){
            //TODO:当前已是写事务状态,使用主库
            LOG.info("{} 存在写事务，使用MASTER", stage);
            //DynamicDataSourceContextPriorityWrapper.setMaster(stage, current);
            return;
        }

        if (priority > current) {

            if (read == false && priority == NormalPriority.AOP_TRANSACTION_STAGE.priority) {
                //TODO:写事务优先策略下，强制
                DynamicDataSourceContextPriorityWrapper.setTxWriteStatus();
                DynamicDataSourceContextPriorityWrapper.setMaster(stage.name(), priority);
                return;
            }

            if (read) {
                DynamicDataSourceContextPriorityWrapper.setSlave(stage.name(), priority);
            } else {
                DynamicDataSourceContextPriorityWrapper.setMaster(stage.name(), priority);
            }
        }
    }

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
