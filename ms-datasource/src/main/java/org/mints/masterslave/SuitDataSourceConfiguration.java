package org.mints.masterslave;


import com.alibaba.druid.pool.DruidDataSource;
import org.mints.masterslave.datasource.SuitRoutingDataSource;
import org.mints.masterslave.datasource.SuitRoutingDataSourceContext;
//import org.mints.masterslave.filter.ProductFilter;
import org.mints.masterslave.suit.SuitAcquireImplement;
import org.mints.masterslave.suit.SuitAcquireInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * 根据spring.datasource.type配置决定是否启动当前多账套数据源
 */
@Configuration
//@ConditionalOnProperty(value = {"spring.datasource.type"}, havingValue = "org.mints.masterslave.datasource.SuitRoutingDataSource", matchIfMissing = false)
@ConditionalOnProperty(value = {"ms-datasource.suit-enabled"}, matchIfMissing = false)
public class SuitDataSourceConfiguration {
    private static final Logger log = LoggerFactory.getLogger(SuitDataSourceConfiguration.class);

    //@Bean
    @Bean(name = "dataSource")
    //@Primary
    SuitRoutingDataSource routingDataSource() {
        log.info("[MsDynamic][SuitDataSourceConfiguration] 创建RoutingDataSource");
        SuitRoutingDataSource rds = new SuitRoutingDataSource();
        return rds;
    }

    @Bean
    JdbcTemplate jdbcTemplate() {
        log.info("[MsDynamic][SuitDataSourceConfiguration] 创建jdbcTemplate");
        DruidDataSource dataSource = SuitRoutingDataSource.getDruidDataSource(SuitRoutingDataSourceContext.getMainKey());
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(dataSource);
        return jdbcTemplate;
    }

    @Bean
    SuitAcquireInterface suitAcquireImplement(SuitRoutingDataSource routingDataSource, JdbcTemplate jdbcTemplate) {
        log.info("[MsDynamic][SuitDataSourceConfiguration] 创建SuitAcquireInterface");
        SuitAcquireInterface suitAcquireImplement = new SuitAcquireImplement(jdbcTemplate);
        routingDataSource.setSuitAcquireInterface(suitAcquireImplement);
        return suitAcquireImplement;
    }

//    @Bean
//    ProductFilter timeFilter() {
//        log.info("[MsDynamic][SuitDataSourceConfiguration] 创建TimeFilter");
//        return new ProductFilter();
//    }


//    @PostConstruct
//    void init() {
//    }
}
