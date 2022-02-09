package org.mints.masterslave.filter;



import org.mints.masterslave.SuitDataSourceConfiguration;
import org.mints.masterslave.datasource.SuitRoutingDataSourceContext;
import org.mints.masterslave.entity.PkgDataSource;
import org.mints.masterslave.logger.MsLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.InvalidParameterException;

/**
 * @description
 * @author: bn
 * @date: 2018/9/26 0026 13:36
 */
public class ProductFilter implements Filter {

//    private static final MsLogger log = MsLogger.getLogger(ProductFilter.class);
    private static final Logger logger = LoggerFactory.getLogger(SuitDataSourceConfiguration.class);

    private JdbcTemplate jdbcTemplate;

    public ProductFilter(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }


    @Value("${ms-datasource.product-default-mode-http-header-pd-field:pkgName}")
    private String pdHttpHeaderField;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("[ms-ds][ProductFilter] filter初始化:{}",filterConfig);
    }

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

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        //long start = System.currentTimeMillis();
        //log.info("filter start");
        HttpServletRequest hr = (HttpServletRequest)request;

        String pkg = ((HttpServletRequest) request).getHeader(pdHttpHeaderField);
//        if (pkg == null) {
//            pkg = "main";
//        }

        logger.debug("[ms-ds][ProductFilter] {} get pkg: {}", hr.getRequestURI(),pkg);
        try {
            PkgDataSource suitProduct = getSuitProduct(pkg);
            SuitRoutingDataSourceContext.setDataSourceProductKey(suitProduct.getDs());
            filterChain.doFilter(request, response);
            SuitRoutingDataSourceContext.clearThreadLocalAllKey();
        }catch (RuntimeException e){
            throw new InvalidParameterException("未正确配置产品");
        }

        //log.info("filter end, time=" + (System.currentTimeMillis() - start));
    }

    @Override
    public void destroy() {
        logger.info("[ms-ds][ProductFilter]filter销毁");
    }
}
