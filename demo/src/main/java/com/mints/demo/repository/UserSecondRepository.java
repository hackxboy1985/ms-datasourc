package com.mints.demo.repository;

import com.mints.demo.domain.first.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;


public interface UserSecondRepository extends BaseRepository<User, Long> {


    @Transactional
    @Query(value = "select * from f_user limit 1 ",nativeQuery = true)
    User getByOneTransactionMaster();

    @Transactional(readOnly = true)
    @Query(value = "select * from f_user limit 1 ",nativeQuery = true)
    User getByOneTransactionSlaver();

    @Query(value = "select * from f_user limit 1 ",nativeQuery = true)
    User getByOneNoTransaction();

}
