package com.iteaj.framework.spi.auth.handle;

import com.iteaj.framework.exception.FrameworkException;
import com.iteaj.framework.consts.CoreConst;
import com.iteaj.framework.spi.auth.*;
import com.iteaj.framework.autoconfigure.FrameworkProperties;
import com.iteaj.framework.web.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * create time: 2021/3/24
 *  默认的移动端处理器
 * @author iteaj
 * @since 1.0
 */
public abstract class DefaultAuthHandler implements WebAuthHandler, InitializingBean {

    private List<WebAuthAction> actions;
    private FrameworkProperties config;
    private Map<String, WebAuthAction> actionMap;
    protected static Logger logger = LoggerFactory.getLogger(WebAuthHandler.class);

    public DefaultAuthHandler(List<WebAuthAction> actions) {
        this.actions = actions;
        try {
            this.actionMap = actions.stream().collect(Collectors
                    .toMap(item -> item.getName(), item -> item));
        } catch (Exception e) {
            throw new FrameworkException("actions必填或者actions名称重复", e);
        }
    }

    @Override
    public WebAuthAction getAction(String name) {
        return actionMap.get(name);
    }

    @Override
    public final WebAuthAction matcher(HttpServletRequest request) {
        final Object action = request.getAttribute(CoreConst.HANDLE_AUTH_ACTION);
        if(action != null) {
            return (WebAuthAction) action;
        }

        String requestURI = WebUtils.getRequestUriContext(request);
        for (WebAuthAction item: actions) {
            if(item.pathMatcher(requestURI, request)) {
                request.setAttribute(CoreConst.HANDLE_AUTH_ACTION, item);
                return item;
            }
        }

        return null;
    }

    @Override
    public List<WebAuthAction> getActions() {
        return this.actions;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    public FrameworkProperties getConfig() {
        return config;
    }

    @Autowired
    public DefaultAuthHandler setConfig(FrameworkProperties config) {
        this.config = config;
        return this;
    }
}
