package org.mints.masterslave.suit;

import org.mints.masterslave.datasource.SuitRoutingDataSourceContext;
import org.mints.masterslave.entity.SuitDataSource;
import org.mints.masterslave.logger.MsLogger;
import org.mints.masterslave.utils.EncryptAESUtil;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import java.security.InvalidParameterException;
import java.util.List;


public class SuitAcquireImplement implements SuitAcquireInterface{
    private static final MsLogger log = MsLogger.getLogger(SuitAcquireImplement.class);

    private JdbcTemplate jdbcTemplate;

    public SuitAcquireImplement(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public SuitDataSource getSuitDataSource(String suitname) {
        if(StringUtils.isEmpty(suitname) || !suitname.contains(SuitRoutingDataSourceContext.SUIT_SEPERATE))
            throw new InvalidParameterException("账套不存在-"+suitname);
        String[] split = suitname.split(SuitRoutingDataSourceContext.SUIT_SEPERATE);
        String product = split[0];
        String dsindex = split[1];//split.length > 1 ? split[1]: DS_READ;
        String sql = "select name, dbindex, url, username, password from suit_datasource where name= ? and dbindex = ?";
        RowMapper<SuitDataSource> rowMapper = new BeanPropertyRowMapper<>(SuitDataSource.class);
        SuitDataSource suitDataSource = null;
        try {
            suitDataSource = jdbcTemplate.queryForObject(sql, rowMapper, product, dsindex);
            suitDataSource.setPassword(EncryptAESUtil.detryptFailReturnSrc(suitDataSource.getPassword()));
            adapterDbindex(suitDataSource);
        }catch (EmptyResultDataAccessException e){
            //log.error(e.getMessage(),e);
        }
        if (suitDataSource == null){
            log.info("[ms-ds][SuitAcquire]Query Ds {}-{} empty! please verity the datasource info in table: suit_datasource",product,dsindex);
            if (SuitRoutingDataSourceContext.MAIN_KEY.equalsIgnoreCase(product)) {
                throw new InvalidParameterException("未正确配置产品");
            } else {
                throw new InvalidParameterException(product + SuitRoutingDataSourceContext.SUIT_SEPERATE + dsindex + "-账套不存在,请检查该产品数据库配置");
            }
        }

        return suitDataSource;
    }

    @Override
    public List<SuitDataSource> getSuitProducts() {
        String sql = "select name,url from suit_datasource group by name ";
        List<SuitDataSource> suitDataSourceList = null;
        try {
            suitDataSourceList = jdbcTemplate.query(sql, new BeanPropertyRowMapper(SuitDataSource.class));
            for (SuitDataSource sds : suitDataSourceList){
                String url = sds.getUrl();
                if (url!=null && url.contains("?")) {
                    String[] split = url.split("\\?");
                    String prefix = split[0];
                    int lastIndexOf = prefix.lastIndexOf("/");
                    sds.setDb(prefix.substring(lastIndexOf+1,prefix.length()));
                }
                adapterDbindex(sds);
            }
            return suitDataSourceList;
        }catch (EmptyResultDataAccessException e){
        }
        return null;
    }

    /**
     * 兼容主从数据格式
     * @param sds
     */
    void adapterDbindex(SuitDataSource sds ){
        if("slaveDataSourceRead".equalsIgnoreCase(sds.getDbindex())){
            sds.setDbindex("slave");
        } else if("slaveDataSourceWrite".equalsIgnoreCase(sds.getDbindex())){
            sds.setDbindex("master");
        }
    }
}
