package org.mints.masterslave.datasource;

import org.mints.masterslave.logger.MsLogger;

/**
 * @author a
 * @version 1.0
 */
class DynamicDataSourceContextHolder {
    private static final MsLogger LOG = MsLogger.getLogger(DynamicDataSourceContextHolder.class);
    private static final ThreadLocal<DataSourceKey> currentDatesource = new ThreadLocal<DataSourceKey>();

    /**
     * 清除当前数据源
     */
    public static void clear() {
        currentDatesource.remove();
    }

    /**
     * 获取当前使用的数据源
     *
     * @return 当前使用数据源的ID
     */
    public static DataSourceKey get() {
        return currentDatesource.get();
    }

    /**
     * 设置当前使用的数据源
     *
     * @param value 需要设置的数据源ID
     */
    private static void set(DataSourceKey value) {
        //LOG.info("设置数据源:{}",value.name());
        currentDatesource.set(value);
    }

    public static void setMaster() {
        DynamicDataSourceContextHolder.set(DataSourceKey.MASTER);
    }

    /**
     * 设置从从库读取数据
     * 这里可以随便切换不同的从库
     */
    public static void setSlave() {
        DynamicDataSourceContextHolder.set(DataSourceKey.SLAVE);
    }
}


