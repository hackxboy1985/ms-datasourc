# ms-datasource-spring-boot-starter

## 简介
> 主从数据源模式只需要配置主写数据源、从读数据源

## 版本重大特性
>兼顾事务，避免幻读，在一个完整的线程中，无论前面是读还是写（非事务），当此时出现写事务时，后面所有的操作都以主写数据源为主，避免写后再读时，走了从库，而此时主库数据还未来得及复制到丛库，出现幻读
* 方法切面拦截：自动拦截Repository及所有子类的方法，根据方法名决定是读还是写；
* 注解切面拦截：自动拦截TargetDataSource注解切面，设置数据源;
* 事务读写拦截:拦截事务，在进入事务时根据事务的读写策略决定走从库还是写库；


## 主从数据源策略

> 普通读写分离策略: NORMAL_RW
* 优先级:方法切面<事务读写拦截<注解设置
* 根据方法名来区分数据源
* 根据事务是读还是写来区分数据源
* 根据注解指定数据源

> 写事务优先策略: TX_WRITE_FIRST
* 优先级:方法切面<注解设置<事务读写拦截
* 采用此策略时：当存在写事务时，后续无论是读还是写，都遵循该写事务的数据源，避免写事务后出现从库读，而此时写库可能未同步到从库，出现幻读现象


## 多数据源事务的分布式事务问题

> 外层事务中内部的方法不支持跨数据源请求，即不支持分布式事务
* 每个数据源对应一个独立的事务管理器，简单的业务场景这样用也就没有问题，但是一般的业务场景总有一个事务覆盖两个数据源的操作，这个时候单指定哪个事务管理器都不行
* 因此在同一个线程中，如果存在覆盖两个数据源的操作，禁止在这些操作前加事务注解或手动事务

  例:
```java



class ServiceA{
      public void read(){
         //读
      }
      public void write(){
           //写
        }
}


class B{
    
    ServiceA sa;
    
    //此处不能使用注解，因为read为读数据源，write为写数据源，此处加事务注解，会引起分布式事务问题
    @Transactional
    public void logic(){
      sa.read();
      sa.write();
    }
}


```


### 使用主从数据源的配置

```yaml

### 允许bean覆盖,因datasource使用的是自定义的
spring:
  main:
    allow-bean-definition-overriding: true
### 数据源配置
ms-datasource:
    # 配置开关
    multi-source-enabled: true
    # 日志开关
    log:
      enabled: true
    # 策略设置
    strategy: NORMAL_RW # NORMAL_RW 为普通读写策略 TX_WRITE_FIRST 为写事务优先策略
    # 实体类的扫描路径
    domain-packages: com..*.*
    # 主数据源
    master:
      jdbcUrl: 
      username: 
      password: 
      # 连接池的配置
      pool:
        useConnPool: true
        maxPoolSize: 10
        connectionTimeout: 60
        maxLifetime: 60
    # 从数据源
    slave:
      jdbcUrl: 
      username: 
      password: 
      # 连接池的配置
      pool:
        useConnPool: true
        maxPoolSize: 10
        connectionTimeout: 60
        maxLifetime: 60
```

### 项目中使用

```java
public class XxxController{

    @TargetDataSource(dataSourceKey = MASTER)
    public void api(){
    
    }

}
```

或者

```java
public class XxxServiceImpl{
    
    @TargetDataSource(dataSourceKey = SLAVE)
    public void api(){
    
    }

}
```

### 版本更新说明

#### 版本1.0.0 

日期：2021/02/02\
版本号：1.0.0\
更新说明：初始版本
