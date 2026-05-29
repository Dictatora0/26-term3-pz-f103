package com.iteaj.framework.Proxy;

import java.lang.reflect.InvocationHandler;

/**
 * create time: 2021/3/28
 *
 * @author iteaj
 * @since 1.0
 */
public interface TargetInvocationHandler<T> extends InvocationHandler {

    T getTarget();
}
