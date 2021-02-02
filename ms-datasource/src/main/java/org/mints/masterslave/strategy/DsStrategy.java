package org.mints.masterslave.strategy;


public interface DsStrategy {

    /**
     * 策略类型
     */
    enum DsStrategyType {
        NORMAL_RW, TX_WRITE_FIRST
    }

    /**
     * 策略阶段
     */
    enum DsStrategyStage{
        AOP_ANNOTATION_STAGE,   //注解切面阶段
        AOP_INVOKE_METHOD_STAGE,//调用方法切面阶段
        AOP_TRANSACTION_STAGE   //事务切面阶段
    }

    /**
     * 执行多数据源策略
     * @param stage 阶段
     * @param read 是否只读
     */
    void doStrategy(DsStrategyStage stage,boolean read);

    /**
     * 清除
     * @param isRequestOver 是否请求结束
     * @param stage 阶段
     */
    void clear(boolean isRequestOver, DsStrategyStage stage);
}
