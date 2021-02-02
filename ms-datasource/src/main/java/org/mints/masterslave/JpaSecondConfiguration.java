//package com.cetc.config;
//
//import com.cetc.config.strategy.DsStrategy;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
//import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.org.mints.masterslave.Configuration;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import javax.persistence.EntityManager;
//import javax.sql.DataSource;
//
///**
// * 第二个数据源，jpa的相关配置
// */
//@org.mints.masterslave.Configuration
////@EntityScan(basePackages = "com.cetc.domain.second")
////1、实体扫描
////2、实体管理ref
////3、事务管理
////@EnableJpaRepositories(
//////        basePackages = "com.cetc.repository",
////        entityManagerFactoryRef = "secondEntityManagerFactoryBean",
////        transactionManagerRef = "secondTransactionManager")
//@EnableTransactionManagement
//public class JpaSecondConfiguration {
//
//    //jpa其他参数配置
//    @Autowired
//    private JpaProperties jpaProperties;
//
//    //实体管理工厂builder
//    @Autowired
//    private EntityManagerFactoryBuilder factoryBuilder;
//
//    @Autowired
//    @Qualifier("dynamicDataSource")
//    private DataSource dynamicDataSource;
//
//    /**
//     * 配置第二个实体管理工厂的bean
//     * @return
//     */
////    @Bean(name = "secondEntityManagerFactoryBean")
//    @Bean(name = "entityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() {
//        return factoryBuilder.dataSource(dynamicDataSource)
////                .properties(jpaProperties.getHibernateProperties(new HibernateSettings()))
//                .properties(jpaProperties.getProperties())
////                .packages("com.cetc.domain.second")
//                .packages("com.*.domain..*")
//                .persistenceUnit("secondPersistenceUnit")
//                .build();
//    }
//
//    /**
//     * EntityManager不过解释，用过jpa的应该都了解
//     * @return
//     */
//    @Bean(name = "secondEntityManager")
//    public EntityManager entityManager() {
//        return entityManagerFactoryBean().getObject().createEntityManager();
//    }
//
//    /**
//     * jpa事务管理
//     * @return
//     */
////    @Bean(name = "secondTransactionManager")
//    @Bean(name = "transactionManager")
//    public JpaTransactionManager transactionManager(DsStrategy dsStrategy) {
//        JpaTransactionManager jpaTransactionManager = new MyTransactionManager(dsStrategy);
//        jpaTransactionManager.setEntityManagerFactory(entityManagerFactoryBean().getObject());
//        return jpaTransactionManager;
//    }
//}
