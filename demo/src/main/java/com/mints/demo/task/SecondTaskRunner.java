package com.mints.demo.task;


import com.mints.demo.FuturesExecutorUtil;
import org.mints.masterslave.datasource.SuitRoutingDataSourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;


@Component
public class SecondTaskRunner implements CommandLineRunner{


    @Autowired
    ISecondTask iSecondTask;

    @Override
    public void run(String... args) throws Exception {
        SuitRoutingDataSourceContext.setDataSourceProductKey("test");
//        iSecondTask.read();
        iSecondTask.write();
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
