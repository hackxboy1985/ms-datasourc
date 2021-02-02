package org.mints.masterslave;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.mints.masterslave.datasource.DataSourceKey;
import org.mints.masterslave.datasource.DynamicRoutingDataSource;
import org.mints.masterslave.strategy.DsStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库配置
 */
@Configuration
public class DataSourceConfiguration {
//    private static final MsLogger LOG = MsLogger.getLogger(DataSourceConfiguration.class);

    /**
     *  第一个数据连接，默认优先级最高
     * @return
     */
    @Bean(name = "dataSourceMaster")
    @Primary
    @ConfigurationProperties(prefix = "ms-datasource.master")
    public DataSource dataSourceMaster() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 第二个数据源
     * @return
     */
    @Bean(name = "dataSourceSlave")
    @ConfigurationProperties(prefix = "ms-datasource.slave")
    public DataSource dataSourceSlave() {
        return DruidDataSourceBuilder.create().build();
    }


    @Bean(name = "dataSource")
    public DataSource dynamicDataSource() {
        DynamicRoutingDataSource dataSource = new DynamicRoutingDataSource();
        dataSource.setDefaultTargetDataSource(dataSourceMaster());
        Map<Object, Object> dataSourceMap = new HashMap<>(4);
        dataSourceMap.put(DataSourceKey.MASTER, dataSourceMaster());
        dataSourceMap.put(DataSourceKey.SLAVE, dataSourceSlave());
        dataSource.setTargetDataSources(dataSourceMap);
        return dataSource;
    }


    @Bean
    DynamicDataSourceAspect dynamicDataSourceAspect(DsStrategy dsStrategy){
        return new DynamicDataSourceAspect(dsStrategy);
    }

}
