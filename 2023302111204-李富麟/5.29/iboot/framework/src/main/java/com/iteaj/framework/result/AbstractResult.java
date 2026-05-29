package com.iteaj.framework.result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * create time: 2019/7/28
 *
 * @author iteaj
 * @since 1.0
 */
public abstract class AbstractResult<E> implements Result<E> {

    private E data;
    private long code;
    private String message;

    protected static Logger logger = LoggerFactory.getLogger(Result.class);

    public AbstractResult(String message, long code) {
        this.code = code;
        this.message = message;
    }

    public AbstractResult(E data, String message, long code) {
        this.data = data;
        this.code = code;
        this.message = message;
    }

    @Override
    public long getCode() {
        return this.code;
    }

    @Override
    public AbstractResult<E> setCode(long code) {
        this.code = code;
        return this;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public AbstractResult<E> setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public E getData() {
        return this.data;
    }

    public AbstractResult<E> setData(E data) {
        this.data = data;
        return this;
    }
}
