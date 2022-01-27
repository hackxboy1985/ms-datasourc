package org.mints.masterslave.datasource;


import com.alibaba.ttl.TransmittableThreadLocal;
import org.mints.masterslave.logger.MsLogger;

/**
 * @description
 * @author: born
 * @date: 2021/4/20 12:10
 */
public class SuitRoutingDataSourceContext {

    private static final MsLogger log = MsLogger.getLogger(SuitRoutingDataSourceContext.class);

    //主数据源key
    public final static String MAIN_KEY = "main";
    public final static String SUIT_SEPERATE = "##";

    //账套数据源缺省库name
    private static String SUID_DS_DEFAULT_KEY = DataSourceKey.MASTER.name().toLowerCase();

    //库 key
//    static final ThreadLocal<String> threadLocalDataSourceKey = new ThreadLocal<>();


    //产品 key
    static final ThreadLocal<String> threadLocalProductKey = new TransmittableThreadLocal<>();

    /**
     * 获取主数据库的key
     * @return
     */
    public static String getMainKey() {
        return MAIN_KEY + SUIT_SEPERATE + SUID_DS_DEFAULT_KEY;
    }

    /**
     * 获取数据库key
     * @return
     */
    public static String getDataSourceRoutingKey() {
        //TODO:当未使用注解进行设置数据源时，默认使用主库
        //String key = threadLocalDataSourceKey.get();
        DataSourceKey key = DynamicDataSourceContextHolder.get();
        if (key == null){
            return MAIN_KEY + SUIT_SEPERATE + SUID_DS_DEFAULT_KEY;
        }
        //TODO:当使用了注解进行设置数据源时，产品key才生效
        String product = threadLocalProductKey.get();
        product = product == null ? MAIN_KEY : product;
        return product + SUIT_SEPERATE + key.name().toLowerCase();
    }

    /**
     * 获得当前数据库的key
     * @return
     */
    public static String getDataSourceKey() {
//        String key = threadLocalDataSourceKey.get();
        DataSourceKey key = DynamicDataSourceContextHolder.get();
        if (key == null){
            return SUID_DS_DEFAULT_KEY;
        }
        return key.name();
    }

    /**
     * 设置数据库的key
     * @param key
     */
    public static void setThreadLocalDataSourceKey(String key) {
        log.info("threadlocal db set:{}",key);
        //threadLocalDataSourceKey.set(key);
        if (DataSourceKey.MASTER.name().equals(key))
            DynamicDataSourceContextHolder.setMaster();
        else
            DynamicDataSourceContextHolder.setSlave();
    }

    public static void setThreadLocalDataSourceDefaultKey() {
        DynamicDataSourceContextHolder.setMaster();
        //threadLocalDataSourceKey.set(SUID_DS_DEFAULT_KEY);
    }



    /**
     * 清除数据库的key
     */
    public static void clearThreadLocalDataSourceKey() {
        log.info("清除 ds threadlocal");
        //threadLocalDataSourceKey.remove();
        DynamicDataSourceContextHolder.clear();
//        threadLocalProductKey.remove();
    }

    /**
     * 清除数据库的key
     */
    public static void clearThreadLocalAllKey() {
        log.info("清除 all threadlocal");
        //threadLocalDataSourceKey.remove();
        DynamicDataSourceContextHolder.clear();
        threadLocalProductKey.remove();
    }


    /**
     * 获取产品key
     * @return
     */
    public static String getDataSourceProductKey() {
        String key = threadLocalProductKey.get();
        return key == null ? MAIN_KEY : key;
    }

    /**
     * 设置产品key
     * @param key
     */
    public static void setDataSourceProductKey(String key) {
        log.info("threadlocal product set:{}",key);
        threadLocalProductKey.set(key);
    }

    /**
     * 设置账套缺省数据源名
     * @param name
     */
    static void setSuitDsDefaultKey(String name){
        SUID_DS_DEFAULT_KEY = name;
    }

//    public static String getSuitDsWriteKey(){
//        return SUID_DS_DEFAULT_KEY;
//    }




}
