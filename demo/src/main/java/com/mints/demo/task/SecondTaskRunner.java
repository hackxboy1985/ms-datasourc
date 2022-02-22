package com.mints.demo.task;


import com.mints.demo.FuturesExecutorUtil;
import org.mints.masterslave.ProductUtils;
import org.mints.masterslave.datasource.SuitRoutingDataSourceContext;
import org.mints.masterslave.entity.PkgDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.Callable;


@Component
public class SecondTaskRunner implements CommandLineRunner{




    @Autowired
    ISecondTask iSecondTask;

    @Autowired
    ProductUtils productUtils;

    @PostConstruct
    void init(){
        //TODO: 测试PostConstruct下异常
        //iSecondTask.read();
    }

    @Override
    public void run(String... args) throws Exception {

        List<PkgDataSource> suitProductList = productUtils.getSuitProductList();
        for (PkgDataSource pkgDataSource : suitProductList){
            SuitRoutingDataSourceContext.setDataSourceProductKey(pkgDataSource.getDs());
            iSecondTask.read();
//            iSecondTask.write();
        }

//        SuitRoutingDataSourceContext.setDataSourceProductKey("test");
//        iSecondTask.read();
//        iSecondTask.write();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                iSecondTask.readNoneTs();
//            }
//        }).run();
//        iSecondTask.read();
//        iSecondTask.readNoneTs();

        FuturesExecutorUtil.schedule(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                iSecondTask.read();

                iSecondTask.write();

                iSecondTask.readNoneTs();
                return null;
            }
        });
    }

}
