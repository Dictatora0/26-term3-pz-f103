package com.iteaj.framework;

import com.iteaj.framework.result.HttpResult;

/**
 * create time: 2019/11/14
 *  基于http状态码的响应
 * @author iteaj
 * @since 1.0
 */
public class BaseController extends AbstractController {

    @Override
    protected <E> HttpResult<E> fail(E data, String msg) {
        return HttpResult.Fail(data, msg);
    }

    @Override
    protected <E> HttpResult<E> success(E data, String msg) {
        return HttpResult.Success(data, msg);
    }

    /**
     * 返回状态码接口
     * @param msg 消息
     * @param code http状态码
     * @return
     */
    protected <E> HttpResult<E> statusCode(E data, String msg, int code) {
        return HttpResult.StatusCode(data, msg, code);
    }
}
