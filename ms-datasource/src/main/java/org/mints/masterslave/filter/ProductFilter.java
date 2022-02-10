package org.mints.masterslave.filter;



import org.mints.masterslave.ProductUtils;
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
    private static final Logger logger = LoggerFactory.getLogger(ProductFilter.class);

    private ProductUtils productUtils;

    public ProductFilter(ProductUtils productUtils){
        this.productUtils = productUtils;
    }


    @Value("${ms-datasource.product-default-mode-http-header-pd-field:pkgName}")
    private String pdHttpHeaderField;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("[ms-ds][ProductFilter] filter初始化:{}",filterConfig);
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
            PkgDataSource suitProduct = productUtils.getSuitProduct(pkg);
            SuitRoutingDataSourceContext.setDataSourceProductKey(suitProduct.getDs());
            filterChain.doFilter(request, response);
            SuitRoutingDataSourceContext.clearThreadLocalAllKey();
        }catch (RuntimeException e){
            logger.error(e.getMessage(),e);
            throw new InvalidParameterException("未正确配置产品");
        }

        //log.info("filter end, time=" + (System.currentTimeMillis() - start));
    }

    @Override
    public void destroy() {
        logger.info("[ms-ds][ProductFilter]filter销毁");
    }
}
