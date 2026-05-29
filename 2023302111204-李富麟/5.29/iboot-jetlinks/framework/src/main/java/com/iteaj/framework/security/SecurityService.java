package com.iteaj.framework.security;

import com.iteaj.framework.Entity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SecurityService {

    /**
     * 当前用户是否登录
     * @return 是否登录
     */
    default boolean isLogin() {
        return getUser().isPresent();
    }

    /**
     * 获取当前登录用户
     * @return 用户对象
     */
    Optional<Entity> getUser();

    /**
     * 获取当前登录用户的角色列表
     * @return
     */
    Optional<List<String>> getRoles();

    /**
     * 是否有角色
     * @param role
     * @return
     */
    boolean hasRole(String role);

    /**
     * 是否有角色
     * @param roles
     * @return
     */
    boolean hasRole(Logical logical, String... roles);

    /**
     * 是否有权限
     * @param permission
     * @return
     */
    boolean hasPermission(String permission);

    /**
     * 是否有权限
     * @param permissions
     * @return
     */
    boolean hasPermission(Logical logical, String... permissions);

    /**
     * 返回当前请求
     * @return
     */
    default HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return requestAttributes.getRequest();
    }

    default HttpServletResponse getResponse() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return requestAttributes.getResponse();
    }

    /**
     * 返回session里面所有的属性
     * @return
     */
    Collection<String> getSessionKeys();

    /**
     * 从session里面获取对应的属性
     * @param key
     * @param <T>
     * @return
     */
    <T> Optional<T> getSessionAttr(String key);

    /**
     * 将名为key的一个属性放到session
     * @param key
     * @param value
     * @return
     */
    SecurityService setSessionAttr(String key, Object value);

    /**
     * @param key
     * @return 移除的对象
     */
    Object removeSessionAttr(String key);


    /**
     * 获取当前登入的用户id
     * @return 登录用户id
     */
    default Optional<Serializable> getLoginId() {
        return getUser().map(item -> item.getId());
    }

    /**
     * 注销指定的会话
     * @param sessionId
     */
    void logout(Serializable sessionId);

    /**
     * 注销
     * @param request
     * @param response
     */
    void logout(HttpServletRequest request, HttpServletResponse response) throws SecurityException;

    /**
     * 登录
     * @param token
     * @param request
     * @param response
     */
    void login(SecurityToken token, HttpServletRequest request, HttpServletResponse response) throws SecurityException;

    /**
     * 校验是否当前登录用户
     * @param sessionId
     * @return
     */
    boolean isCurrentUser(Object sessionId);
}
