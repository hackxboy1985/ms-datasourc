package org.mints.masterslave;


import org.mints.masterslave.entity.PkgDataSource;
import org.mints.masterslave.utils.DsMemoryCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public class ProductUtils {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    DsMemoryCacheUtil<PkgDataSource> dsMemoryCacheUtil;

    public ProductUtils(JdbcTemplate jdbcTemplate,DsMemoryCacheUtil<PkgDataSource> dsMemoryCacheUtil){
        this.jdbcTemplate = jdbcTemplate;
        this.dsMemoryCacheUtil = dsMemoryCacheUtil;
    }

    /**
     * 获取指定包名的数据源关系
     * @param pkg
     * @return
     */
    public PkgDataSource getSuitProduct(String pkg) {
        if(StringUtils.isEmpty(pkg))
            throw new InvalidParameterException("参数pkg异常-"+pkg);

        Optional<PkgDataSource> ds = dsMemoryCacheUtil.findObject(pkg, new Callable<PkgDataSource>() {
            @Override
            public PkgDataSource call() throws Exception {
                String sql = "select pkg,ds,remark from pkg_datasource where pkg= ? ";
                RowMapper<PkgDataSource> rowMapper = new BeanPropertyRowMapper<>(PkgDataSource.class);
                PkgDataSource pkgDataSource = null;
                try {
                    pkgDataSource = jdbcTemplate.queryForObject(sql, rowMapper, pkg);
                }catch (EmptyResultDataAccessException e){
                    //log.error(e.getMessage(),e);
                }
                return pkgDataSource;
            }
        });

//        if (pkgDataSource == null){
//            logger.info("[ms-ds][ProductFilter]Query product {} empty! please verity the datasource info in table: pkg_datasource",pkg);
//            throw new InvalidParameterException(pkg+"-包配置不存在,请检查该产品数据源配置");
//        }
        if (ds.isPresent())
            return ds.get();
        throw new RuntimeException("查询不到"+pkg+"对应的数据源");
    }

    /**
     * 获取所有产品包的数据源关系
     * @return list
     */
    public List<PkgDataSource> getSuitProductList() {
        String sql = "select id,pkg,ds,remark from pkg_datasource";
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
