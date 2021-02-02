package com.mints.demo.repository;

import com.mints.demo.domain.first.User;

import java.util.List;

public interface UserFirstRepository extends BaseRepository<User, Long> {
    List<User> getAllByAge(int age);
}
