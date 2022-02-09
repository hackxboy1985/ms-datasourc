package org.mints.masterslave.entity;

import lombok.Data;

import java.util.Date;

/**
 * @description pkg数据源实体模型
 * @author:
 * @date: 2018/9/26 0026 12:15
 */
@Data
public class PkgDataSource {

    private Long id;

    private String pkg;

    private String ds;

    private Date createDate;

    private Date updateDate;

}
