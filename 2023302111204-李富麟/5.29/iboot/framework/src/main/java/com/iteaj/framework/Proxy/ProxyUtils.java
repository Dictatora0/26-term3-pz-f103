package com.iteaj.framework.Proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * create time: 2021/3/28
 *
 * @author iteaj
 * @since 1.0
 */
public class ProxyUtils {

    public static Object getTarget(Object proxy) {
        if(Proxy.isProxyClass(proxy.getClass())) {
            final InvocationHandler handler = Proxy.getInvocationHandler(proxy);
            if(handler instanceof TargetInvocationHandler) {
                return ((TargetInvocationHandler<?>) handler).getTarget();
            }
        }

        return null;
    }
}
