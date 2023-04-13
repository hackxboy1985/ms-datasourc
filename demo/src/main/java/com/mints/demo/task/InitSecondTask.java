package com.mints.demo.task;

import com.mints.demo.domain.first.User;
import com.mints.demo.repository.UserFirstRepository;
import com.mints.demo.repository.UserSecondRepository;
import org.mints.masterslave.TargetDataSource;
import org.mints.masterslave.datasource.SuitRoutingDataSourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.mints.masterslave.datasource.DataSourceKey.MASTER;
import static org.mints.masterslave.datasource.DataSourceKey.SLAVE;


@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class InitSecondTask implements ISecondTask{

    @Autowired
    private UserSecondRepository userSecondRepository;
    @Autowired
    private UserFirstRepository userFirstRepository;

    Long id = null;

    public void read(){
        read3();//先测试3，因为里面开始执行的无事务查询,如果放在read2后，则为read2执行完后的数据源
        read1();
        read2();
    }

    public void read1(){
        System.out.println("----------read1 开始InitSecondTask 测试默认事务后切换数据源");
//        DynamicDataSourceContextHolder.setMaster();
//        DynamicDataSourceContextHolder.setSlave();

        //TODO: step1-findAll用是jpa框架方法，默认为事务
//        System.out.println("step0:"+userSecondRepository.findAll());
        //TODO: or
        //TODO: step2-getByOne方法添加@Transactional事务，调用完成后，事务结束时，会释放链接放入缓存
        System.out.println("step1 执行默认事务方法 getByOneTransactionMaster:"+
                userSecondRepository.getByOneTransactionMaster());

        System.out.println("step2:尝试切换数据源至test");
        SuitRoutingDataSourceContext.setDataSourceProductKey("test");
        System.out.println("step3:执行默认事务方法 getAllByAge: "+userFirstRepository.getAllByAge(0));

        System.out.println("step4: 不切换数据源，执行非事务方法getByOneNoTransaction: "+userSecondRepository.getByOneNoTransaction());
//        DynamicDataSourceContextHolder.clear();
        System.out.println("----------read1 结束InitSecondTask  测试默认事务后切换数据源");
    }


    public void read2(){
        System.out.println("----------read2 开始InitSecondTask 测试只读事务后切换数据源");
        SuitRoutingDataSourceContext.setDataSourceProductKey("zdd");

        //TODO: step1-findAll用是jpa框架方法，默认为事务
//        System.out.println("step0:"+userSecondRepository.findAll());
        //TODO: or
        //TODO: step2-getByOne方法添加@Transactional事务，调用完成后，事务结束时，会释放链接放入缓存
        System.out.println("step1 执行默认事务方法 getByOneTransactionSlaver:"+
                userSecondRepository.getByOneTransactionSlaver());

        System.out.println("step2:尝试切换数据源至test");
        SuitRoutingDataSourceContext.setDataSourceProductKey("test");
        System.out.println("step3:执行默认事务方法 getAllByAge: "+userFirstRepository.getAllByAge(0));

        System.out.println("step4: 不切换数据源，执行非事务方法getByOneNoTransaction: "+userSecondRepository.getByOneNoTransaction());
//        DynamicDataSourceContextHolder.clear();
        System.out.println("----------read2 结束InitSecondTask  测试只读事务后切换数据源");
    }

    public void read3(){
        System.out.println("----------read3 开始InitSecondTask 测试无事务后切换数据源");
        SuitRoutingDataSourceContext.setDataSourceProductKey("zdd");
        System.out.println("----------本次切换成功与否，取决于read3方法执行前的数据源是否是事务，是则能够切换，否则切换失败，或者链接未初始化，如未，则能够切换，否则切换失败");
        //TODO: step1-执行非事务方法执行非事务方法getByOneNoTransaction
        System.out.println("step1 执行默认事务方法 getByOneTransactionMaster:"+
                userSecondRepository.getByOneNoTransaction());

        System.out.println("step2:尝试切换数据源至test");
        SuitRoutingDataSourceContext.setDataSourceProductKey("test");
        System.out.println("step3:执行非事务方法 getAllByAgeNoTransaction: "+userFirstRepository.getAllByAgeNoTransaction(0));
        System.out.println("step4:通过日志，可以看到，以上切换test数据源是失败的!!!!!!!");

        System.out.println("step5: 不切换数据源，执行非事务方法getByOneNoTransaction: "+userSecondRepository.getByOneNoTransaction());
//        DynamicDataSourceContextHolder.clear();
        System.out.println("----------read3 结束InitSecondTask  测试只读事务后切换数据源");
    }


//    @Transactional(rollbackFor = Exception.class)
    public void write(){
        System.out.println("----------InitSecondTask-开始写");
//        DynamicDataSourceContextHolder.setMaster();
//        DynamicDataSourceContextHolder.setSlave();
        User user = new User();
        user.setName("test");
        user.setAge(25);
        //user.setTestAge(2);
        User save = userSecondRepository.saveAndFlush(user);
        System.out.println("结果:"+save);
        System.out.println("写入后查询 缓存");
        System.out.println("查询1结果:"+userSecondRepository.getByOneNoTransaction());
        System.out.println("查询2结果:"+userFirstRepository.findAll());
//        Optional<User> byId = userSecondRepository.findById(save.getId());
//        id = save.getId();
//        DynamicDataSourceContextHolder.clear();
//        System.out.println("写入后查询 结果"+byId.get());

        System.out.println("----------InitSecondTask 结束写");
    }

    public void readNoneTs(){
        System.out.println(Thread.currentThread().getName()+"----------开始读25");
        userSecondRepository.findById(25L);
//        userSecondRepository.getByOne();
        System.out.println(Thread.currentThread().getName()+"----------结束读25");
    }

    @TargetDataSource(dataSourceKey = MASTER)
    public void readWriteRead(){
        read();
        write();
        read();
    }
}
