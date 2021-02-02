package org.mints.masterslave.datasource;


import org.mints.masterslave.logger.MsLogger;

/**
 * 强制使用包装器
 * 普通读写分离模式：根据priority的大小进行设置，大的不会被小的替换
 * 事务写优先模式：根据priority的大小进行设置，大的不会被小的替换，如出现写事务，则后续沿用写事务的主库不再变更，写事务优先级最高
 */
public class DynamicDataSourceContextPriorityWrapper {
    private static final MsLogger LOG = MsLogger.getLogger(DynamicDataSourceContextPriorityWrapper.class);

    private static final ThreadLocal<Integer> priorityDatesource = new ThreadLocal<Integer>();
    private static final ThreadLocal<Boolean> writeDatesource = new ThreadLocal<Boolean>();

    public static Integer getPriority(){
        return priorityDatesource.get() != null ? priorityDatesource.get() : -1;
    }

    private static void setPriority(String stage, Integer priority, boolean master){

        if (priorityDatesource.get() != null && priorityDatesource.get() > priority)
            return;

        LOG.info("{} 使用数据源模式:{}",stage, master?"MASTER":"SLAVE");
        priorityDatesource.set(priority);
    }

    private static void clearPriority(){
        priorityDatesource.remove();
    }

    public static void setMaster(String stage, Integer priority) {
        if (priority < getPriority())
            return;
        setPriority(stage,priority,true);
        DynamicDataSourceContextHolder.setMaster();
    }

    /**
     * 设置从从库读取数据
     */
    public static void setSlave(String stage, Integer priority) {
        if (priority < getPriority())
            return;
        setPriority(stage,priority,false);
        DynamicDataSourceContextHolder.setSlave();
    }

    public static void clear(boolean isOver){
        if (isOver) {
            LOG.info("线程结束，清理全部数据源  {}", DynamicDataSourceContextHolder.get());
            priorityDatesource.remove();
            writeDatesource.remove();
            DynamicDataSourceContextHolder.clear();
            return;
        }

        clearPriority();
        if (DynamicDataSourceContextHolder.get() != null) {
            LOG.info("清理当前方法数据源  {}", DynamicDataSourceContextHolder.get());
            DynamicDataSourceContextHolder.clear();
        }
    }

    /**********************************************************
     * 事务写状态：事务写优先策略使用，用于设置和判断当前的事务写状态
     **********************************************************/

    /**
     * 获取事务写状态,事务写优先策略使用
     * @return
     */
    public static Boolean getTxWriteStatus(){
        if (writeDatesource.get()== null)
            return false;
        return writeDatesource.get();
    }

    /**
     * 设置事务写状态,事务写优先策略使用
     * @return
     */
    public static void setTxWriteStatus(){
        writeDatesource.set(Boolean.TRUE);
    }
}
