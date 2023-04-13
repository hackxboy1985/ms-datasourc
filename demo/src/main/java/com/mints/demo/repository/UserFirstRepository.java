package com.mints.demo.repository;

import com.mints.demo.domain.first.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserFirstRepository extends BaseRepository<User, Long> {
    @Transactional
    List<User> getAllByAge(int age);

    @Query(value = "select * from f_user where age = ?1 ",nativeQuery = true)
    List<User> getAllByAgeNoTransaction(int age);

    @Query(value = "select * from f_user limit 1 ",nativeQuery = true)
    User getByOne();
}
