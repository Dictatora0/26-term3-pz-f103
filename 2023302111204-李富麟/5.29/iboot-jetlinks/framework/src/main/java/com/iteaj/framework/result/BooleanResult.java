package com.iteaj.framework.result;

import com.iteaj.framework.exception.ServiceException;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * create time: 2019/7/26
 *
 * @author iteaj
 * @since 1.0
 */
public class BooleanResult extends OptionalResult<Boolean, BooleanResult> {

    public BooleanResult(Boolean value) {
        this(value, null);
    }

    public BooleanResult(Boolean value, String message) {
        super(value, message, Boolean.TRUE.equals(value) ? DCODE : HttpResult.DEFAULT_ERROR_CODE);
    }

    public BooleanResult(Boolean data, String message, long code) {
        super(data, message, code);
    }

    @Override
    public Boolean getData() {
        return super.getData() == null ? false : super.getData();
    }

    public BooleanResult ifPresentAndTrue(Consumer<Boolean> consumer) throws ServiceException {
        if(this.getData() != null && this.getData()) {
            consumer.accept(this.getData());
        }

        return this;
    }

    public BooleanResult ifPresentAndFalse(Consumer<BooleanResult> consumer) throws ServiceException {
        if(this.getData() != null && !this.getData()) {
            consumer.accept(this);
        }

        return this;
    }

    /**
     * 如果数据存在则抛出异常
     * @param msg
     */
    public BooleanResult ifPresentAndTrueThrow(String msg) throws ServiceException {
        if(this.getData() != null && this.getData()) {
            throw new ServiceException(msg);
        }

        return this;
    }

    public BooleanResult ifPresentAndFalseThrow(String msg) throws ServiceException {
        if(this.getData() != null && !this.getData()) {
            throw new ServiceException(msg);
        }

        return this;
    }

    public static BooleanResult buildFalse(String msg) {
        return new BooleanResult(false, msg);
    }

    public static BooleanResult buildTrue(String msg) {
        return new BooleanResult(true, msg);
    }
}
