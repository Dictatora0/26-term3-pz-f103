package com.iteaj.framework.result;

import com.iteaj.framework.Entity;
import com.iteaj.framework.exception.ServiceException;

import java.util.Optional;

/**
 * create time: 2019/7/26
 *
 * @author iteaj
 * @since 1.0
 */
public class DetailResult<E extends Entity> extends OptionalResult<E, DetailResult<E>> {

    public DetailResult(E value) {
        super(value);
    }

    public DetailResult(E data, String message, long code) {
        super(data, message, code);
    }

    @Override
    public Optional<E> of() {
        try {
            return super.of();
        } catch (Exception e) {
            throw new ServiceException("获取不到记录");
        }
    }
}
