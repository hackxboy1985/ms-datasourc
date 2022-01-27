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

    /**
     * 执行多数据源策略:读写分离
     * @param stage 阶段
     * @param read 是否只读
     */
    @Override
    public void doStrategy(DsStrategyStage stage, boolean read, String desc) {
        int priority = getPriority(stage);
        Integer current = DynamicDataSourceContextPriorityWrapper.getPriority();
        if (priority > current) {
            if (read) {
                DynamicDataSourceContextPriorityWrapper.setSlave(stage.name(), priority, desc);
            } else {
                DynamicDataSourceContextPriorityWrapper.setMaster(stage.name(), priority, desc);
            }
        }
    }

    /**
     * 清除数据源
     * @desc 根据stage的优先级决定是否能清除，如果isRequestOver代表所有请求结束，必须清除所有线程相关数据
     * @param isRequestOver 是否请求结束
     * @param stage 阶段
     */
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
