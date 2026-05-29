package com.iteaj.framework.result;

/**
 * create time: 2019/10/19
 *
 * @author iteaj
 * @since 1.0
 */
public class HttpResult<E> extends AbstractResult<E> {

    public static int DEFAULT_ERROR_CODE = 500;
    public static int DEFAULT_SUCCESS_CODE = 200;
    public static String DEFAULT_ERROR_MSG = "操作失败";
    public static String DEFAULT_SUCCESS_MSG = "操作成功";

    protected HttpResult(String message, int code) {
        super(message, code);
    }

    public HttpResult(E data, String message, int code) {
        super(data, message, code);
    }

    public static HttpResult Success() {
        return Success(null, DEFAULT_SUCCESS_MSG);
    }

    public static HttpResult Success(Object data) {
        return Success(data, DEFAULT_SUCCESS_MSG);
    }

    /**
     * 默认使用状态码：200
     * @param message
     * @return
     */
    public static HttpResult Success(Object data, String message) {
        return HttpResult.StatusCode(data, message, DEFAULT_SUCCESS_CODE);
    }

    public static HttpResult Fail() {
        return Fail(null, DEFAULT_ERROR_MSG);
    }

    public static HttpResult Fail(String message) {
        return Fail(null, message);
    }

    /**
     * 默认使用状态码：500
     * @param message
     * @return
     */
    public static HttpResult Fail(Object data, String message) {
        return HttpResult.StatusCode(data, message, DEFAULT_ERROR_CODE);
    }

    public static HttpResult StatusCode(String message, int code) {
        return StatusCode(null, message, code);
    }

    /**
     *
     * @param message
     * @param code http状态码
     * @return
     */
    public static HttpResult StatusCode(Object data, String message, int code) {
        return new HttpResult(data, message, code);
    }
}
