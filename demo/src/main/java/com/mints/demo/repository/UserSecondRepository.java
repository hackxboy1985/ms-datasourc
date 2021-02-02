package com.mints.demo.repository;

import com.mints.demo.domain.first.User;
import org.springframework.data.jpa.repository.Query;

public interface UserSecondRepository extends BaseRepository<User, Long> {

    @Query(value = "select * from f_user limit 1 ",nativeQuery = true)
    User getByOne();

}
