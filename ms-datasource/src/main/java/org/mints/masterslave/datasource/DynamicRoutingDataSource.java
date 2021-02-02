package org.mints.masterslave.datasource;

import org.mints.masterslave.logger.MsLogger;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;


public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    private final  static MsLogger logger= MsLogger.getLogger(DynamicRoutingDataSource.class);

    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceKey dataSourceKey = DynamicDataSourceContextHolder.get();
        logger.info("当前数据源：{}",dataSourceKey != null ? dataSourceKey.name() : "null");
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

}
