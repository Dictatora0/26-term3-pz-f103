package com.iteaj.framework.exception;

/**
 * create time: 2020/3/9
 *  业务异常, 但操作不回滚
 *  no rollBack
 * @author iteaj
 * @since 1.0
 */
public class ServiceNRBException extends ServiceException {

    public ServiceNRBException(String message) {
        super(message);
    }

    public ServiceNRBException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceNRBException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
