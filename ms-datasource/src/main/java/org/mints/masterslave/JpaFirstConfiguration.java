package org.mints.masterslave;

import org.mints.masterslave.strategy.DsStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

/**
 * 第一个数据源，jpa的相关配置
 */
@Configuration
//1、实体扫描
//2、实体管理ref
//3、事务管理
//@EnableJpaRepositories(
//        basePackages = "com.cetc.repository",
//        entityManagerFactoryRef = "firstEntityManagerFactoryBean",
//        transactionManagerRef = "firstTransactionManager")
@EnableTransactionManagement
public class JpaFirstConfiguration {

    //jpa其他参数配置
    @Autowired
    private JpaProperties jpaProperties;

    //实体管理工厂builder
    @Autowired
    private EntityManagerFactoryBuilder factoryBuilder;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dynamicDataSource;

    @Value("${ms-datasource.domain-packages:com..*.*}")
    public String domainPackages;//com.ydtc.**

    /**
     * 配置第一个实体管理工厂的bean
     * @return
     */
//    @Bean(name = "firstEntityManagerFactoryBean")
    @Bean(name = "entityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() {
        return factoryBuilder.dataSource(dynamicDataSource)
//        return factoryBuilder.dataSource(dataSource1)
                //这一行的目的是加入jpa的其他配置参数比如（ddl-auto: update等）
                //当然这个参数配置可以在事务配置的时候也可以
//                .properties(jpaProperties.getHibernateProperties(new HibernateSettings()))
                .properties(jpaProperties.getProperties())
//                .packages("com.cetc.domain.first")
//                .packages("com..*.*")
                .packages(domainPackages)
                .persistenceUnit("msdatasource-persistence-unit")//first
                .build();
    }

    /**
     * EntityManager不过解释，用过jpa的应该都了解
     * @return
     */
    @Bean(name = "firstEntityManager")
    @Primary
    public EntityManager entityManager() {
        return entityManagerFactoryBean().getObject().createEntityManager();
    }




    /**
     * jpa事务管理
     * @return
     */
//    @Bean(name = "firstTransactionManager")
    @Bean(name = "transactionManager")
    @Primary
    public JpaTransactionManager transactionManager(DsStrategy dsStrategy) {
        JpaTransactionManager jpaTransactionManager = new MyTransactionManager(dsStrategy);
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactoryBean().getObject());
        return jpaTransactionManager;
    }
}
