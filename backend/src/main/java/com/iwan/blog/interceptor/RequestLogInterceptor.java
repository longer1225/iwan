package com.iwan.blog.interceptor;

import com.iwan.blog.util.LogUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 请求日志拦截器
 * 自动记录所有HTTP请求的日志，方便调试和追踪
 */
@Component
public class RequestLogInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLogInterceptor.class);

    private static final String START_TIME_ATTR = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTR, startTime);
        
        // 记录请求日志
        LogUtils.logRequestStart(logger, request);
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, 
                          ModelAndView modelAndView) throws Exception {
        // 可在此处添加更多日志逻辑
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, 
                               Exception ex) throws Exception {
        // 计算请求耗时
        long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        long duration = System.currentTimeMillis() - startTime;
        
        // 记录请求结束日志
        LogUtils.logRequestEnd(logger, request, duration);
        
        // 如果有异常，记录异常日志
        if (ex != null) {
            LogUtils.logErrorWithStack(logger, "请求处理异常", ex.getMessage(), ex);
        }
    }
}
