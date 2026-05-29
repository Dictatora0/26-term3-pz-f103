package com.iteaj.framework.exception;

/**
 * create time: 2019/10/20
 *  业务接口异常
 * @author iteaj
 * @since 1.0
 */
public class ServiceException extends FrameworkException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
