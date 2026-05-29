package com.iteaj.framework.result;

import com.iteaj.framework.exception.ServiceException;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * create time: 2019/7/26
 *  业务层操作结果
 * @author iteaj
 * @since 1.0
 */
public abstract class OptionalResult<E, _this extends AbstractResult<E>> extends AbstractResult<E> {

    public static long DCODE = HttpResult.DEFAULT_SUCCESS_CODE;
    public static String DMSG = HttpResult.DEFAULT_SUCCESS_MSG;

    protected OptionalResult(E value) {
        this(value, DMSG, DCODE);
    }

    public OptionalResult(E data, String message, long code) {
        super(data, message, code);
    }

    public Optional<E> of() {
        return Optional.of(getData());
    }

    /**
     * 如果数据存在
     * @param consumer
     * @return
     */
    public _this ifPresent(Consumer<E> consumer) {
        if(this.getData() != null) {
            consumer.accept(this.getData());
        }

        return (_this) this;
    }

    /**
     * 如果数据不存在
     * @param consumer
     * @return
     */
    public _this ifNotPresent(Consumer<E> consumer) {
        if(this.getData() == null) {
            consumer.accept(this.getData());
        }

        return (_this) this;
    }

    /**
     * 如果数据存在
     * @param consumer
     * @return
     */
    public _this ifPresent(BiConsumer<E, _this> consumer) {
        if(this.getData() != null) {
            consumer.accept(this.getData(), (_this) this);
        }

        return (_this) this;
    }

    public Optional<E> ofNullable() {
        return Optional.ofNullable(this.getData());
    }

    /**
     * 如果数据存在则抛出异常
     * @param msg
     */
    public _this ifPresentThrow(String msg) throws ServiceException {
        if(this.getData() != null) {
            throw new ServiceException(msg);
        }

        return (_this) this;
    }

    /**
     * 如果数据不存在则抛出异常
     * @param msg
     */
    public _this ifNotPresentThrow(String msg) throws ServiceException {
        if(this.getData() == null) {
            throw new ServiceException(msg);
        }

        return (_this) this;
    }

    public _this ok() {
        return this.ok(DMSG);
    }

    public _this ok(String msg) {
        this.setCode(DCODE);
        this.setMessage(msg);
        return (_this) this;
    }

    public _this fail() {
        return this.fail(HttpResult.DEFAULT_ERROR_CODE, HttpResult.DEFAULT_ERROR_MSG);
    }

    public _this fail(String msg) {
        return this.fail(HttpResult.DEFAULT_ERROR_CODE, msg);
    }

    public _this fail(long code, String msg) {
        this.setCode(code);
        this.setMessage(msg);
        return (_this) this;
    }

    @Override
    public _this setCode(long code) {
        return (_this) super.setCode(code);
    }

    @Override
    public _this setMessage(String message) {
        return (_this) super.setMessage(message);
    }

    @Override
    public _this setData(E data) {
        return (_this) super.setData(data);
    }
}
