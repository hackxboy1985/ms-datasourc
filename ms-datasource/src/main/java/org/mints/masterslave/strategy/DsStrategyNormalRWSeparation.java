package org.mints.masterslave.strategy;


import org.mints.masterslave.datasource.DynamicDataSourceContextPriorityWrapper;

/**
 * 正常读写策略
 * @desc 优先级见本类NormalPriority枚举,值越大，优先级越高
 */
public class DsStrategyNormalRWSeparation implements DsStrategy {

    enum NormalPriority{
        AOP_INVOKE_METHOD_STAGE(1),//方法名切面
        AOP_TRANSACTION_STAGE(2),//事务切面
        AOP_ANNOTATION_STAGE(3);//注解切面

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
        if (priority > current) {
            if (read) {
                DynamicDataSourceContextPriorityWrapper.setSlave(stage.name(), priority);
            } else {
                DynamicDataSourceContextPriorityWrapper.setMaster(stage.name(), priority);
            }
        }
    }

    @Override
    public void clear(boolean isRequestOver, DsStrategyStage stage) {
        int priority = getPriority(stage);
        Integer current = DynamicDataSourceContextPriorityWrapper.getPriority();
        if (isRequestOver) {
            DynamicDataSourceContextPriorityWrapper.clear(isRequestOver);
        } else {
            if (priority >= current && current > 0) {
                DynamicDataSourceContextPriorityWrapper.clear(isRequestOver);
            }
        }
    }
}
