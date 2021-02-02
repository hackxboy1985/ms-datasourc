package com.mints.demo.task;

import com.mints.demo.repository.UserFirstRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InitFirstTask implements CommandLineRunner{

    @Autowired
    private UserFirstRepository userFirstRepository;

    @Override
    public void run(String... args) throws Exception {
//        User user = new User();
//        user.setName("test");
//        user.setAge(25);
//        userFirstRepository.save(user);


//        System.out.println(userFirstRepository.getAllByAge(25));

    }
}
