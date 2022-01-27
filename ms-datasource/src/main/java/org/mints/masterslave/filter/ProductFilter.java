package org.mints.masterslave.filter;



import org.mints.masterslave.datasource.SuitRoutingDataSourceContext;
import org.mints.masterslave.logger.MsLogger;

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

    private static final MsLogger log = MsLogger.getLogger(ProductFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("[MsDynamic][ProductFilter] filter初始化:{}",filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        //long start = System.currentTimeMillis();
        //log.info("filter start");
        HttpServletRequest hr = (HttpServletRequest)request;

        String as = request.getParameter("as");
        if (as == null){
            as = hr.getHeader("as");
        }
        if (as == null) {
            as = "main";
        }

        log.debug("[MsDynamic][ProductFilter] {} get as: {}", hr.getRequestURI(),as);

        if (as == null) {
            throw new InvalidParameterException("as不能为空");
        } else {

            SuitRoutingDataSourceContext.setDataSourceProductKey(as);
            //RoutingDataSourceContext.setThreadLocalDataSourceKey(ds);
            filterChain.doFilter(request, response);
            SuitRoutingDataSourceContext.clearThreadLocalAllKey();
        }
        //log.info("filter end, time=" + (System.currentTimeMillis() - start));
    }

    @Override
    public void destroy() {
        log.info("[MsDynamic][ProductFilter]filter销毁");
    }
}
