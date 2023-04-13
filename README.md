# ms-datasource-spring-boot-starter

## 简介
> 主从数据源模式只需要配置主写数据源、从读数据源
* 主从功能目的是为了实现读写分离，把主数据源查询压力转移到从数据源，实现主数据库负责写，丛负责读
* 分离不可能完全实现，见下面的`事务隔离性`，但能极大减轻主库压力

## 版本重大特性
>兼顾事务，避免幻读，在一个完整的线程中，无论前面是读还是写（非事务），当此时出现写事务时，后面所有的操作都以主写数据源为主，避免写后再读时，走了从库，而此时主库数据还未来得及将将写入数据复制到丛库，出现幻读
* 方法切面拦截：自动拦截Repository及所有子类的方法，根据方法名决定是读还是写；
* 注解切面拦截：自动拦截TargetDataSource注解切面，设置数据源;
* 事务读写拦截：拦截事务，在进入事务前doBegin根据事务的读写策略决定走从库还是写库；
* 兼容驼峰命名：对驼峰命名进行兼容;

>事务隔离性
* 使用本starer能够将事务进行隔离开，每个事务内部只能选择1次数据源.
* 如果在一个事务内部包含其它查询方法，数据源仍将不会改变。例：在某Service方法上加事务，该方法中调用了事务或非事务方法，都只会因该方法的事务而选择主数据源。除非申明该事务为只读。
* 通过doBegin与doResume/doCleanupAfterCompletion保证多个串行事务之间可以进行数据源切换.
* 只要是事务，每次执行时都会先选择数据源，结束时释放数据源(释放至连接池)

>事务默认执行主库(MASTER)/也可走从库(SLAVER)
* 定义一个默认事务，如果未指定readonly，则默认走主库
* 如果使用@Transactional(readOnly = true)定义只读，则走从库


>doBegin时获取数据源，进而获取链接，然后设置事务状态，所以方法切面要早于事务的doBegin方法

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
* 如果要添加事务，应该指定MASTER，使其使用主库进行读写，否则将发生分布式事务问题!!!!!!!



> 讲解几种事例

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
    //因为事务结束前，是不会释放connect，所以在read时直接将数据源设置为slave，read执行完后数据源不会切换，如果slave没有写权限，则会失败
    //如果想加事务，则可以指定MASTER: 添加@TargetDataSource(dataSourceKey = MASTER) 即可放入master
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
    enabled: true
    # 日志开关
    log.enabled: true
    # 驼峰命名
    implicit-naming: true
    # 策略设置
    strategy: NORMAL_RW # NORMAL_RW 为普通读写策略 TX_WRITE_FIRST 为写事务优先策略
    # 实体类的扫描路径
    domain-packages: com..*.*
    # 主数据源
    master:
      driver-class-name: net.sf.log4jdbc.sql.jdbcapi.DriverSpy
      url: jdbc:log4jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true
      username: root
      password: 
      # 初始化连接大小
      initial-size: 1
      # 最小空闲连接数
      min-idle: 1
      max-active: 20
      max-wait: 30000
      # 可关闭的空闲连接间隔时间
      time-between-eviction-runs-millis: 60000
      # 配置连接在池中的最小生存时间
      min-evictable-idle-time-millis: 300000
      validation-query: select '1' from dual
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false

    # 从数据源
    slave:
      driver-class-name: net.sf.log4jdbc.sql.jdbcapi.DriverSpy
      url: jdbc:log4jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true
      username: test_reader
      password: test_reader
      # 初始化连接大小
      initial-size: 1
      # 最小空闲连接数
      min-idle: 1
      max-active: 20
      max-wait: 30000
      # 可关闭的空闲连接间隔时间
      time-between-eviction-runs-millis: 60000
      # 配置连接在池中的最小生存时间
      min-evictable-idle-time-millis: 300000
      validation-query: select '1' from dual
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false

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

## 账套
> 多账套实现
* 增加多账套，使一套代码可以允许多个不同的马甲包访问服务，走不同的数据库，使各个马甲包数据隔离
* 结合上面的主从功能，则可以实现多账户的分区、读写分离


#### 版本1.0.0 

日期：2021/02/02\
版本号：1.0.0\
更新说明：初始版本
