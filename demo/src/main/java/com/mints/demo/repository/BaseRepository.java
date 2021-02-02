package com.mints.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
//public interface BaseRepository<T, I> extends PagingAndSortingRepository<T, I>, JpaSpecificationExecutor<T> {
public interface BaseRepository<T, I> extends JpaRepository<T, I>, JpaSpecificationExecutor<T> {

}
