package com.mints.demo.task;

import org.mints.masterslave.MyTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class TestController {

    @Autowired
    ISecondTask iSecondTask;

    @GetMapping(value = "/test")
    public ResponseEntity test() throws Exception {
        iSecondTask.read();
        iSecondTask.write();
        iSecondTask.read();
        iSecondTask.readNoneTs();

        return new ResponseEntity("ok", HttpStatus.OK);
    }

    @GetMapping(value = "/test2")
    public ResponseEntity test2() throws Exception {
//        MyTransactionManager.testTrigger = 1;
        iSecondTask.read();
        try {
            iSecondTask.write();
        }catch (Exception e){}
//        MyTransactionManager.testTrigger = 0;

//        iSecondTask.readWriteRead();

        return new ResponseEntity("ok", HttpStatus.OK);
    }

}
