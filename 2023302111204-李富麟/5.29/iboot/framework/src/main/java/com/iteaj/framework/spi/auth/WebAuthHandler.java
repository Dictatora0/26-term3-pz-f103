package com.iteaj.framework.spi.auth;

import com.iteaj.framework.autoconfigure.FrameworkProperties;
import com.iteaj.framework.consts.CoreConst;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.iteaj.framework.consts.CoreConst.EXEC_EXCEPTION_STATUS;

/**
 * create time: 2021/3/24
 *  web请求授权处理器 支持pc和移动端
 *  1. 支持多端统一使用session
 *  2. 支持不同端配置不同的session过期时间
 *  3. 支持多种方式授权(账号密码, oauth2[gitee, wechat, alipay])
 *  4. 支持session集群
 * @author iteaj
 * @since 1.0
 */
public interface WebAuthHandler {

    FrameworkProperties getConfig();

    /**
     * 通过Action名称获取一个Action
     * 在跳转到授权时会存储当前对应的Action名称到当前session
     * 在授权回调时, 通过session里面的此参数获取对应的Action对象
     * @param name
     * @return
     */
    WebAuthAction getAction(String name);

    /**
     * 获取当前系统所有注入到spring容器的对象<br>
     *     1. 一个系统可以有多个动作, 比如处理用账号密码登录的动作 比如用Oauth2授权动作
     * @return
     */
    List<WebAuthAction> getActions();

    /**
     * 返回匹配的动作
     * @param request
     * @return
     */
    WebAuthAction matcher(HttpServletRequest request);

    /**
     * web登录时的处理
     * @param request
     * @param response
     */
     default void login(HttpServletRequest request, HttpServletResponse response) throws SecurityException {
         final WebAuthAction action = matcher(request);
         if(action != null) {
             Exception throwable = null;
             final AuthContext context = action.getContext(request, response);
             try {
                 action.login(context, request, response);
             } catch (Exception e) {
                 throwable = e;
             } finally {
                 action.loginCall(throwable, request, response);
             }
         }
     }

    /**
     * 注销
     * @param request
     * @param response
     */
     default void logout(HttpServletRequest request, HttpServletResponse response) {
         final WebAuthAction matcher = matcher(request);
         if(matcher != null) {
             matcher.logout(request, response);
         }
     }

     default AuthContext getAuthContext(HttpServletRequest request) {
        return (AuthContext) request.getAttribute(CoreConst.SESSION_AUTH_CONTEXT);
     }

     default boolean preAuthorize(HttpServletRequest request, HttpServletResponse response) {
        final WebAuthAction action = matcher(request);

        if(action == null) {
            return true; // 没有找到处理此次请求的动作, 拒绝放行
        } else {
            final AuthContext context = action.getContext(request, response);
            context.setAction(action.getName());
            request.setAttribute(CoreConst.SESSION_AUTH_CONTEXT, context);
        }

        return action.preAuthorize(request, response);
     }

    /**
     * 继续授权
     * @param request
     * @param response
     * @return 默认直接放行
     */
    default boolean postAuthorize(HttpServletRequest request, HttpServletResponse response) throws SecurityException {
        final WebAuthAction action = matcher(request);

        if(action == null) {
            request.setAttribute(EXEC_EXCEPTION_STATUS, "未认证");
            return true; // 没有找到处理此次请求的动作, 拒绝放行
        }

        final boolean authorize = action.postAuthorize(request, response);
        if(authorize) {
            request.setAttribute(EXEC_EXCEPTION_STATUS, "未认证");
        }
        return authorize;
    }
}
