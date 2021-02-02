package com.mints.demo.task;

import com.mints.demo.domain.first.User;
import com.mints.demo.repository.UserFirstRepository;
import com.mints.demo.repository.UserSecondRepository;
import org.mints.masterslave.TargetDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.mints.masterslave.datasource.DataSourceKey.MASTER;
import static org.mints.masterslave.datasource.DataSourceKey.SLAVE;


@Component
public class InitSecondTask implements ISecondTask{

    @Autowired
    private UserSecondRepository userSecondRepository;
    @Autowired
    private UserFirstRepository userFirstRepository;

    Long id = null;

    @TargetDataSource(dataSourceKey = MASTER)
    public void read(){
        System.out.println("----------开始读");
//        DynamicDataSourceContextHolder.setMaster();
//        DynamicDataSourceContextHolder.setSlave();
        System.out.println("结果:"+userSecondRepository.findAll());
//        System.out.println(userFirstRepository.getAllByAge(25));
//        DynamicDataSourceContextHolder.clear();
        System.out.println("----------结束读");
    }

    @TargetDataSource(dataSourceKey = SLAVE)
    public void write(){
        System.out.println("----------开始写");
//        DynamicDataSourceContextHolder.setMaster();
//        DynamicDataSourceContextHolder.setSlave();
        User user = new User();
        user.setName("test");
        user.setAge(25);
        User save = userSecondRepository.saveAndFlush(user);
        System.out.println("结果:"+save);
//        System.out.println("写入后查询 缓存");
//        userSecondRepository.findById(save.getId());
//        id = save.getId();
//        DynamicDataSourceContextHolder.clear();

        System.out.println("----------结束写");
    }

    public void readNoneTs(){
        System.out.println(Thread.currentThread().getName()+"----------开始读");
        userSecondRepository.findById(114L);
//        userSecondRepository.getByOne();
        System.out.println(Thread.currentThread().getName()+"----------结束读");
    }

    @TargetDataSource(dataSourceKey = MASTER)
    public void readWriteRead(){
        read();
        write();
        read();
    }
}
