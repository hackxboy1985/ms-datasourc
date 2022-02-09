package org.mints.masterslave;


import org.mints.masterslave.entity.PkgDataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import java.security.InvalidParameterException;
import java.util.List;

public class ProductUtils {

    private JdbcTemplate jdbcTemplate;

    public ProductUtils(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 获取指定包名的数据源关系
     * @param pkg
     * @return
     */
    public PkgDataSource getSuitProduct(String pkg) {
        if(StringUtils.isEmpty(pkg))
            throw new InvalidParameterException("参数pkg异常-"+pkg);
        String sql = "select ds from pkg_datasource where pkg= ? ";
        RowMapper<PkgDataSource> rowMapper = new BeanPropertyRowMapper<>(PkgDataSource.class);
        PkgDataSource pkgDataSource = null;
        try {
            pkgDataSource = jdbcTemplate.queryForObject(sql, rowMapper, pkg);
        }catch (EmptyResultDataAccessException e){
            //log.error(e.getMessage(),e);
        }

//        if (pkgDataSource == null){
//            logger.info("[ms-ds][ProductFilter]Query product {} empty! please verity the datasource info in table: pkg_datasource",pkg);
//            throw new InvalidParameterException(pkg+"-包配置不存在,请检查该产品数据源配置");
//        }
        return pkgDataSource;
    }

    /**
     * 获取所有产品包的数据源关系
     * @return list
     */
    public List<PkgDataSource> getSuitProductList() {
        String sql = "select id,pkg,ds from pkg_datasource";
        RowMapper<PkgDataSource> rowMapper = new BeanPropertyRowMapper<>(PkgDataSource.class);
        List<PkgDataSource> pkgDataSource = null;
        try {
            pkgDataSource = jdbcTemplate.query(sql, rowMapper);
        }catch (EmptyResultDataAccessException e){
            //log.error(e.getMessage(),e);
        }

        return pkgDataSource;
    }
}
