package com.mints.demo.task;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class SecondTaskRunner implements CommandLineRunner{


    @Autowired
    ISecondTask iSecondTask;

    @Override
    public void run(String... args) throws Exception {

        iSecondTask.read();
//        iSecondTask.write();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                iSecondTask.readNoneTs();
//            }
//        }).run();
        iSecondTask.read();
        iSecondTask.readNoneTs();
    }

}
