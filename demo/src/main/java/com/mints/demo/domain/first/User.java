package com.mints.demo.domain.first;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "f_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer age;

//    @Column(name = "testAge")
//    private Integer test_age;

}
