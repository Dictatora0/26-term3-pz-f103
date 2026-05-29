package com.iteaj.framework;

import com.iteaj.framework.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * create time: 2018/7/7
 *
 * @author iteaj
 * @version 1.0
 * @since JDK1.7
 */
public abstract class AbstractController {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected String msgForSuccess() {
        return "操作成功";
    }

    protected String msgForFail() {
        return "操作异常";
    }

    /**
     * @see #msgForFail() 默认使用消息
     * @return
     */
    protected <E> Result<E> fail() {
        return fail(null, msgForFail());
    }

    /**
     * @see #msgForSuccess() () 默认使用消息
     * @return
     */
    protected <E> Result<E> success() {
        return success(null, msgForSuccess());
    }

    /**
     * @see #msgForFail() 默认使用消息
     * @return
     */
    protected <E> Result<E> fail(E data) {
        return fail(data, msgForFail());
    }

    /**
     * @see #msgForSuccess() () 默认使用消息
     * @return
     */
    protected <E> Result<E> success(E data) {
        return success(data, msgForSuccess());
    }

    protected <E> Result<E> fail(String message) {
        return fail(null, message);
    }

    protected <E> Result<E> success(String message) {
        return success(null, message);
    }

    /**
     * 响应失败信息到客户端 指定消息
     * @return
     */
    protected abstract <E> Result<E> fail(E data, String msg);

    /**
     * 响应成功信息到客户端 指定消息
     * @return
     */
    protected abstract <E> Result<E> success(E data, String msg);

}
