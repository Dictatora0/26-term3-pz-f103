package com.iteaj.framework.logger;

import com.iteaj.framework.security.SecurityUtil;
import com.iteaj.framework.spi.event.EventUtils;
import com.iteaj.framework.spi.event.PayloadEvent;
import com.iteaj.framework.web.WebUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoggerInterceptor implements HandlerInterceptor {

    private final static String ACCESS_LOGGER = "AccessLogger";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod) {
            Logger logger = ((HandlerMethod) handler).getMethod().getAnnotation(Logger.class);
            if(logger != null) {
                String methodName = ((HandlerMethod) handler).getMethod().getName();
                String beanName = ((HandlerMethod) handler).getMethod().getDeclaringClass().getSimpleName();
                AccessLogger accessLogger = new AccessLogger(request.getRequestURI(), logger.type(), logger.value())
                        .setIp(WebUtils.getIpAddress(request))
                        .setParams(request.getQueryString())
                        .setMethod(beanName + "." + methodName)
                        .setStartTime(System.currentTimeMillis());

                request.setAttribute(ACCESS_LOGGER, accessLogger);
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        Object attribute = request.getAttribute(ACCESS_LOGGER);
        if(attribute instanceof AccessLogger) {
            ((AccessLogger) attribute).setMillis(System.currentTimeMillis() - ((AccessLogger) attribute).getStartTime());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Object attribute = request.getAttribute(ACCESS_LOGGER);
        if(attribute instanceof AccessLogger) {
            if(ex != null) {
                ((AccessLogger) attribute)
                        .setStatus(false)
                        .setErrMsg(ex.getMessage());
            }

            try {
                EventUtils.publish(new PayloadEvent(SecurityUtil.getLoginUser(), attribute));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
