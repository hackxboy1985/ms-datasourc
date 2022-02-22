package org.mints.masterslave;


import com.alibaba.druid.pool.DruidDataSource;
import org.mints.masterslave.datasource.SuitRoutingDataSource;
import org.mints.masterslave.datasource.SuitRoutingDataSourceContext;
import org.mints.masterslave.entity.PkgDataSource;
import org.mints.masterslave.filter.ProductFilter;
import org.mints.masterslave.suit.SuitAcquireImplement;
import org.mints.masterslave.suit.SuitAcquireInterface;
import org.mints.masterslave.utils.DsMemoryCacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * 根据spring.datasource.type配置决定是否启动当前多账套数据源
 */
//@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration
//@ConditionalOnProperty(value = {"spring.datasource.type"}, havingValue = "org.mints.masterslave.datasource.SuitRoutingDataSource", matchIfMissing = false)
@ConditionalOnProperty(value = {"ms-datasource.suit-enabled"}, matchIfMissing = false)
public class SuitDataSourceConfiguration {
    private static final Logger log = LoggerFactory.getLogger(SuitDataSourceConfiguration.class);

    //@Bean
    @Bean(name = "dataSource")
    //@Primary
    SuitRoutingDataSource routingDataSource() {
        log.info("[ms-ds][SuitDataSourceConfiguration] 创建RoutingDataSource");
        SuitRoutingDataSource rds = new SuitRoutingDataSource();
        return rds;
    }

    @Bean
    JdbcTemplate jdbcTemplate() {
        log.info("[ms-ds][SuitDataSourceConfiguration] 创建jdbcTemplate");
        DruidDataSource dataSource = SuitRoutingDataSource.getDruidDataSource(SuitRoutingDataSourceContext.getMainKey());
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(dataSource);
        suitAcquireImplement(routingDataSource(),jdbcTemplate);
        return jdbcTemplate;
    }

    @Bean
    SuitAcquireInterface suitAcquireImplement(SuitRoutingDataSource routingDataSource, JdbcTemplate jdbcTemplate) {
        log.info("[ms-ds][SuitDataSourceConfiguration] 创建SuitAcquireInterface");
        SuitAcquireInterface suitAcquireImplement = new SuitAcquireImplement(jdbcTemplate);
        routingDataSource.setSuitAcquireInterface(suitAcquireImplement);
        return suitAcquireImplement;
    }

    @Bean
    DsMemoryCacheUtil getMemoryCache(){
        return new DsMemoryCacheUtil<>();
    }

//    @ConditionalOnProperty(value = {"ms-datasource.product-default-mode"}, havingValue = "true", matchIfMissing = false)
    @Bean
    ProductUtils productUtils(){
        return new ProductUtils(jdbcTemplate(),getMemoryCache());
    }

    @ConditionalOnProperty(value = {"ms-datasource.product-default-mode"}, havingValue = "true", matchIfMissing = false)
    @DependsOn({"dataSourceMain","dataSource"})
    @Bean
    ProductFilter pdFilter(JdbcTemplate jdbcTemplate,SuitAcquireInterface suitAcquireInterface /*放这只是为了提前初始化*/) {
        log.info("[ms-ds][SuitDataSourceConfiguration] 创建ProductFilter");
        return new ProductFilter(productUtils());
    }


}
