package org.mints.masterslave.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import org.mints.masterslave.entity.SuitDataSource;
import org.mints.masterslave.logger.MsLogger;
import org.mints.masterslave.suit.SuitAcquireInterface;
import org.mints.masterslave.utils.EncryptAESUtil;
import org.mints.masterslave.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @description
 * @author: bn
 * @date: 2022/01/20 0026 11:27
 */
public class SuitRoutingDataSource extends AbstractRoutingDataSource {

    private static final MsLogger log = MsLogger.getLogger(SuitRoutingDataSource.class);

//    @Value("${ms-datasource.main.url}")
//    private String main_url;
//    @Value("${ms-datasource.main.username}")
//    private String main_username;
//    @Value("${ms-datasource.main.password}")
//    private String main_password;

    //账套的缺省数据源名
    @Value("${ms-datasource.main.suit-default-ds-name:master}")
    private String suit_default_ds_name;

    @Autowired
    @Qualifier("dataSourceMain")
    private DruidDataSource dataSourceMain;

    private static Map<Object, Object> dataSources = new HashMap<>();

    private SuitAcquireInterface suitAcquireInterface;


    @PostConstruct
    public void RoutingDataSourceInit() {

        EncryptAESUtil.init("su","mints@0419");

        log.info("[ms-ds][RoutingDataSource]初始化主数据源");
//        if (StringUtils.isEmpty(main_url) || StringUtils.isEmpty(main_username)){
        if (dataSourceMain == null){
            throw new RuntimeException("[ms-ds]未配置主库:ms-datasource.main.url or ms-datasource.main.username is null");
        }
        createAndSaveDataSource(SuitRoutingDataSourceContext.getMainKey());

        SuitRoutingDataSourceContext.setSuitDsDefaultKey(suit_default_ds_name);
    }

    public SuitRoutingDataSource() {
    }

    public void setSuitAcquireInterface(SuitAcquireInterface suitAcquireInterface){
        this.suitAcquireInterface = suitAcquireInterface;
    }


    @Override
    protected Object determineCurrentLookupKey(){
        String currentAccountSuit = SuitRoutingDataSourceContext.getDataSourceRoutingKey();
        if (StringUtils.isEmpty(currentAccountSuit)) {
            throw new RuntimeException("[ms-ds]CurrentSuit["+ currentAccountSuit +"] error!!!");
        }
        log.info("[ms-ds][RoutingDataSource] 当前操作账套:{}", currentAccountSuit);
        Utils.traceStack();
        if (!dataSources.containsKey(currentAccountSuit)){
           log.info("[ms-ds][RoutingDataSource] {}数据源不存在, 创建对应的数据源", currentAccountSuit);
            createAndSaveDataSource(currentAccountSuit);
        } else {
            //log.info("{}数据源已存在不需要创建", currentAccountSuit);
        }
        log.info("[ms-ds][RoutingDataSource] 切换到{}数据源", currentAccountSuit);
        return currentAccountSuit;
    }

    private synchronized void createAndSaveDataSource(String currentAccountSuit) {
        DruidDataSource dataSource = createDataSource(currentAccountSuit);
        checkDs(dataSource);
        dataSources.put(currentAccountSuit, dataSource);
        super.setTargetDataSources(dataSources);
        afterPropertiesSet();
        log.info("[ms-ds][RoutingDataSource] {}数据源创建成功", currentAccountSuit);
    }

    /**
     * 校验数据源是否连接正常 不好使!
     * @param dataSource
     */
    private void checkDs(DruidDataSource dataSource){
        try{
            dataSource.init();
        }catch (SQLException e){
//            throw e;
            log.error(e.getMessage(),e);
            throw new RuntimeException("数据库连接异常");
        }
    }

    /**
     * 创建数据源
     * @param currentAccountSuit
     * @return
     */
    DruidDataSource createDataSource(String currentAccountSuit) {
        SuitDataSource suitDataSource;
        if (currentAccountSuit.equalsIgnoreCase(SuitRoutingDataSourceContext.getMainKey())) {
//            suitDataSource = new SuitDataSource();
//            suitDataSource.setName(SuitRoutingDataSourceContext.MAIN_KEY);
//            suitDataSource.setDbindex(SuitRoutingDataSourceContext.MAIN_KEY);
//            suitDataSource.setUrl(main_url);
//            suitDataSource.setUsername(main_username);
//            suitDataSource.setPassword(main_password);
            return dataSourceMain;
        } else {
            suitDataSource = getSuitDataSource(currentAccountSuit);
        }
        if (suitDataSource == null) {
            throw new InvalidParameterException("账套不存在");
        }
        return createDruidDataSource(suitDataSource);
    }

    /**
     * 通过jdbc从数据库中查找数据源配置
     * @param suitname
     * @return
     */
    private SuitDataSource getSuitDataSource(String suitname) {
        return suitAcquireInterface.getSuitDataSource(suitname);
    }

    private static String driverSpy = "net.sf.log4jdbc.sql.jdbcapi.DriverSpy";
    private static String driverMysql = "com.mysql.jdbc.Driver";
    /**
     * 根据配置创建DruidDataSource
     * @param suitDataSource
     * @return
     */
    public static DruidDataSource createDruidDataSource(SuitDataSource suitDataSource) {
        DruidDataSource dataSource = new DruidDataSource();
//        dataSource.setDriverClassName(driverMysql);
//        dataSource.setDriverClassName(driverSpy);//支持日志打印
        dataSource.setName(suitDataSource.getName() + SuitRoutingDataSourceContext.SUIT_SEPERATE + suitDataSource.getDbindex());
        String url = suitDataSource.getUrl();
        if (url.startsWith("jdbc:mysql")) {
            url = url.replace("jdbc:mysql", "jdbc:log4jdbc:mysql");
            dataSource.setDriverClassName(driverSpy);//支持日志打印
        }else{
//            dataSource.setDriverClassName(driverMysql);
            dataSource.setDriverClassName(driverSpy);//支持日志打印
        }
        dataSource.setUrl(url);
        dataSource.setUsername(suitDataSource.getUsername());
        dataSource.setPassword(suitDataSource.getPassword());

        dataSource.setInitialSize(1);
        dataSource.setMinIdle(1);
        dataSource.setMaxActive(100);
        dataSource.setMaxWait(60000);
        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
        // 每十分钟验证一下连接
        dataSource.setTimeBetweenEvictionRunsMillis(600000);
        // 如果连接空闲超过5分钟/1小时就断开
        dataSource.setMinEvictableIdleTimeMillis(60000 * 5);//1 * 60000 * 60
        dataSource.setValidationQuery("select 1 from dual");
        dataSource.setTestWhileIdle(true);
        // 从池中取得链接时做健康检查，该做法十分保守
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);

        dataSource.setConnectionErrorRetryAttempts(1); //重试次数置为0
        dataSource.setBreakAfterAcquireFailure(true); // 这个配置可以跳出循环
//        StatFilter statFilter = new StatFilter();
//        // 运行ilde链接测试线程，剔除不可用的链接
//        dataSource.setMaxWait(-1);
        return dataSource;
    }

    /**
     * 通过账套获取DruidDataSource
     * @param currentAccountSuit
     * @return
     */
    public static DruidDataSource getDruidDataSource(String currentAccountSuit) {
        return (DruidDataSource) dataSources.get(currentAccountSuit);
    }


}
