package org.mints.masterslave;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.mints.masterslave.datasource.DataSourceKey;
import org.mints.masterslave.datasource.DynamicRoutingDataSource;
import org.mints.masterslave.strategy.DsStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库配置
 */
@Configuration
public class DataSourceConfiguration {
//    private static final MsLogger LOG = MsLogger.getLogger(DataSourceConfiguration.class);

    @ConditionalOnProperty(value = {"ms-datasource.suit-enabled"}, havingValue = "true", matchIfMissing = false)
    @Bean(name = "dataSourceMain")
    @Primary
    @ConfigurationProperties(prefix = "ms-datasource.main")
    public DataSource dataSourceMain() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     *  第一个数据连接，默认优先级最高，账套模式关闭才启用
     * @return
     */
    @ConditionalOnProperty(value = {"ms-datasource.suit-enabled"}, havingValue = "false", matchIfMissing = false)
    @Bean(name = "dataSourceMaster")
    @Primary
    @ConfigurationProperties(prefix = "ms-datasource.master")
    public DataSource dataSourceMaster() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 第二个数据源，账套模式关闭才启用
     * @return
     */
    @ConditionalOnProperty(value = {"ms-datasource.suit-enabled"}, havingValue = "false", matchIfMissing = false)
    @Bean(name = "dataSourceSlave")
    @ConfigurationProperties(prefix = "ms-datasource.slave")
    public DataSource dataSourceSlave() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 数据源集，账套模式关闭才启用
     * @return
     */
    @ConditionalOnProperty(value = {"ms-datasource.suit-enabled"}, havingValue = "false", matchIfMissing = false)
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


    @ConditionalOnProperty(value = {"ms-datasource.repository-aspect-enabled"}, havingValue = "true", matchIfMissing = true)
    @Bean
    DynamicDataSourceAspect dynamicDataSourceAspect(DsStrategy dsStrategy){
        return new DynamicDataSourceAspect(dsStrategy);
    }

//    @Bean
//    public JdbcTemplate jdbcTemplate(){
//        return new SpringJdbcTemplate(dynamicDataSource());
//    }
}
