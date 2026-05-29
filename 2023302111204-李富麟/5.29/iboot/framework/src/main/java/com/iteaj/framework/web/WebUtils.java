package com.iteaj.framework.web;

import com.iteaj.framework.consts.CoreConst;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.BrowserType;
import eu.bitwalker.useragentutils.UserAgent;

import javax.servlet.http.HttpServletRequest;

/**
 * create time: 2021/4/2
 *
 * @author iteaj
 * @since 1.0
 */
public class WebUtils {

    public static boolean isAjax(HttpServletRequest request) {
        final String ajaxHeader = request.getHeader("x-requested-with");
        return ajaxHeader != null && ajaxHeader.equals("XMLHttpRequest");
    }

    public static boolean isApp(HttpServletRequest request) {
        final Object agent = request.getAttribute(CoreConst.WEB_USER_AGENT);
        if(agent instanceof UserAgent) {
            final Browser browser = ((UserAgent) agent).getBrowser();

            return browser.getBrowserType() == BrowserType.APP;
        }

        return false;
    }

    public static String getRequestUriContext(HttpServletRequest request){
        final String requestURI = request.getRequestURI();
        return requestURI.substring(request.getContextPath().length());
    }

    public static String getContextRequestUrl(HttpServletRequest request) {
        final StringBuffer requestURL = request.getRequestURL();
        return requestURL.substring(0, requestURL.length() - request
                .getRequestURI().length()) + request.getContextPath();
    }


    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-Ip");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
