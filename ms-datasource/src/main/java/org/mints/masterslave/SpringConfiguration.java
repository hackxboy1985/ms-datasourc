package org.mints.masterslave;

import org.mints.masterslave.logger.MsLogger;
import org.mints.masterslave.strategy.DsStragetyConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

@Configuration
@Import({DataSourceConfiguration.class, DsStragetyConfiguration.class, JpaFirstConfiguration.class})
@ConditionalOnProperty(value = {"ms-datasource.multi-source-enabled"}, matchIfMissing = false)
public class SpringConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(SpringConfiguration.class);


    @Value("${ms-datasource.log.enabled:false}")
    Boolean enabled;

    @Value("${ms-datasource.strategy:NORMAL_RW}")
    String strategy;

    @Value("${ms-datasource.domain-packages:com..*.*}")
    String domainpackages;

    @PostConstruct
    void init(){

        LOG.info("[ms-ds]主从数据源策略模式:{}",strategy);
        LOG.info("[ms-ds]主从数据源package:{}",domainpackages);
        LOG.info("[ms-ds]主从数据源日志开关:{}",enabled);
        MsLogger.setEnabled(enabled);
    }
}
