package com.iteaj.framework.result;

import com.iteaj.framework.exception.ServiceException;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * create time: 2019/7/26
 *
 * @author iteaj
 * @since 1.0
 */
public class ListResult<E> extends OptionalResult<List<E>, ListResult<E>> {

    public ListResult(List<E> value) {
        super(value);
    }

    public ListResult(List<E> data, String message, long code) {
        super(data, message, code);
    }

    public int size() {
        return getData() != null ? getData().size() : 0;
    }

    @Override
    public Optional<List<E>> of() {
        try {
            return super.of();
        } catch (Exception e) {
            throw new ServiceException("获取不到记录");
        }
    }

    /**
     * not null 并且 not empty
     * @param consumer
     * @return
     */
    @Override
    public ListResult<E> ifPresent(Consumer<List<E>> consumer) {
        if(!isEmpty()) {
            consumer.accept(this.getData());
        }

        return this;
    }

    /**
     * 如果数据不存在
     * @param consumer
     * @return
     */
    @Override
    public ListResult<E> ifNotPresent(Consumer<List<E>> consumer) {
        if(this.getData() == null || this.getData().isEmpty()) {
            consumer.accept(this.getData());
        }

        return this;
    }

    @Override
    public ListResult<E> ifPresentThrow(String msg) throws ServiceException {
        if(!isEmpty()) {
            throw new ServiceException(msg);
        }

        return this;
    }

    @Override
    public ListResult<E> ifNotPresentThrow(String msg) throws ServiceException {
        if(this.getData() == null || this.getData().isEmpty()) {
            throw new ServiceException(msg);
        }

        return this;
    }

    public Iterator iterable() {
        return getData() == null ? Collections.emptyIterator() : getData().iterator();
    }

    public boolean isEmpty() {
        return getData() == null ? true : getData().isEmpty();
    }

    public ListResult<E> forEach(Consumer<E> consumer) {
        this.stream().forEach(consumer); return this;
    }

    public Stream<E> stream() {
        return getData() == null ? Stream.empty() : getData().stream();
    }
}
