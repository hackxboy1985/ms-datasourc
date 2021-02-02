package org.mints.masterslave.strategy;

import org.mints.masterslave.logger.MsLogger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 访问结束时清理
 */
public class AccessInterceptor extends HandlerInterceptorAdapter {

//    private MsLogger logger = MsLogger.getLogger(AccessInterceptor.class);
    DsStrategy dsStrategy;

    public AccessInterceptor(DsStrategy dsStrategy){
        this.dsStrategy = dsStrategy;
    }

//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        return true;
//    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        if ("/".equalsIgnoreCase(request.getRequestURI()) == false)
//            logger.info(" 请求路径：{}, tCost: {}ms", request.getRequestURI(), System.currentTimeMillis() - threadLocal.get());
        dsStrategy.clear(true, null);
    }
}
