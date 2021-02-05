package org.mints.masterslave.datasource;

import org.mints.masterslave.logger.MsLogger;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;


public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    private final static MsLogger logger = MsLogger.getLogger(DynamicRoutingDataSource.class);

    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceKey dataSourceKey = DynamicDataSourceContextHolder.get();

//        logger.info("当前事务:{} name:{}", TransactionSynchronizationManager.isActualTransactionActive(), TransactionSynchronizationManager.getCurrentTransactionName());
        logger.info("[ms-ds]当前数据源:{}", dataSourceKey != null ? dataSourceKey.name() : "默认");
//        String ds = dataSourceKey + "";
//        if("SLAVE".equals(ds)) {
//            logger.info("当前数据源：从数据库");
//        }else if(StringUtils.isEmpty(ds)){
//            logger.info("当前数据源：默认主库");
//        }else {
//            logger.info("当前数据源：主数据库");
//        }
        return dataSourceKey;
    }

    public DataSource getAcuallyDataSource() {
        Object lookupKey = determineCurrentLookupKey();
        if (null == lookupKey) {
            return this;
        }
        DataSource determineTargetDataSource = this.determineTargetDataSource();
        return determineTargetDataSource == null ? this : determineTargetDataSource;
    }
}
