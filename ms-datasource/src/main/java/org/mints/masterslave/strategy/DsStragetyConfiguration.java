package org.mints.masterslave.strategy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DsStragetyConfiguration {
//    private static final Logger LOG = LoggerFactory.getLogger(DsStragetyConfiguration.class);

    @Bean
    public DsStrategy dsStrategy(@Value("${ms-datasource.strategy:NORMAL_RW}") String strategy){
        return DsStrategyFactory.create(strategy);
    }

    @Bean
    AccessInterceptor accessInterceptor(DsStrategy dsStrategy){
        return new AccessInterceptor(dsStrategy);
    }

    @Bean
    WebMvcConfigurer webMvcConfigurer(AccessInterceptor accessInterceptor){
        return new WebMvcConfigurer(){
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(accessInterceptor).
                        addPathPatterns("/**");
            }
        };
    }

}
