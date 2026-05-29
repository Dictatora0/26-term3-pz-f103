package com.iteaj.iboot.plugin.shiro;

import com.iteaj.framework.autoconfigure.FrameworkProperties;
import com.iteaj.framework.consts.CoreConst;
import eu.bitwalker.useragentutils.BrowserType;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * create time: 2020/6/25
 *
 * @author iteaj
 * @since 1.0
 */
public class OnlineSessionManager extends DefaultWebSessionManager {

    private FrameworkProperties properties;

    public OnlineSessionManager(FrameworkProperties frameworkProperties) {
        this.properties = frameworkProperties;
        FrameworkProperties.Session session = properties.getWeb().getSession();

        // 修改cookie的名称
        this.getSessionIdCookie().setName(session.getTokenName());
    }

    /**
     * 1. 尝试从header获取access_token作为sessionId
     * 2. 如果没有则从cookie获取
     * @param request
     * @param response
     * @return
     */
    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        HttpServletRequest servletRequest = (HttpServletRequest)request;

        UserAgent userAgent = UserAgent.parseUserAgentString(servletRequest.getHeader("user-agent"));
        servletRequest.setAttribute(CoreConst.WEB_USER_AGENT, userAgent);

        FrameworkProperties.Web web = properties.getWeb();

        // 自动选择
        if(web.getSession().isAutomaticToken()) {
            // 尝试从header获取access_token作为sessionId
            if(web.getSession().isWriteHeader()) {
                String token = getTokenFromHeader(servletRequest, web);
                if (token != null) {
                    return token;
                }
            }

            try {
                // app 从头部获取token
                if(userAgent.getBrowser().getBrowserType() == BrowserType.APP) {
                    return getTokenFromHeader(servletRequest, web);
                }
            } catch (Exception e) { // 如果有异常不做任何处理
                e.printStackTrace();
            }
        } else if(web.getSession().isWriteHeader()) {
            // 尝试从header获取access_token作为sessionId
            String token = getTokenFromHeader(servletRequest, web);
            if (token != null) {
                return token;
            }
        }

        // 从cookie中获取
        return super.getSessionId(request, response);
    }

    @Nullable
    private String getTokenFromHeader(HttpServletRequest servletRequest, FrameworkProperties.Web web) {
        String tokenName = web.getSession().getTokenName();
        String token = servletRequest.getHeader(tokenName);
        if(StringUtils.hasText(token)) {
            return token;
        }

        return null;
    }

}
