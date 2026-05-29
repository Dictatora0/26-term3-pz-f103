package com.iteaj.framework.result;

import java.io.Serializable;

/**
 * create time: 2018/7/19
 * @author iteaj
 * @version 1.0
 * @since JDK1.7
 */
public interface Result<E> extends Serializable {

    /**
     * 错误码
     * @return
     */
    long getCode();

    Object setCode(long code);

    /**
     * 错误描述
     * @return
     */
    String getMessage();

    Object setMessage(String message);

    /**
     * 响应实体
     * @return
     */
    E getData();

    /**
     * 成功
     * @return HttpResult
     */
    static <T> HttpResult<T> success() {
        return HttpResult.Success();
    }

    /**
     * 成功
     * @param msg
     * @return HttpResult
     */
    static <T> HttpResult<T> success(String msg) {
        return HttpResult.Success(msg);
    }

    /**
     * 成功
     * @param msg
     * @param data
     * @return HttpResult
     */
    static <T> HttpResult<T> success(T data, String msg) {
        return HttpResult.Success(data, msg);
    }

    static <T> HttpResult<T> fail(String msg) {
        return HttpResult.Fail(msg);
    }

    static <T> HttpResult<T> fail(T data, String msg) {
        return HttpResult.Fail(data, msg);
    }
}
