package com.iteaj.framework.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iteaj.framework.Entity;

import java.beans.Transient;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * create time: 2019/7/26
 *
 * @author iteaj
 * @since 1.0
 */
public class PageResult<T extends IPage> extends OptionalResult<T, PageResult<T>> {

    public PageResult(T value) {
        super(value);
    }

    public PageResult(T data, String message, long code) {
        super(data, message, code);
    }

    @Transient
    public long getSize() {
        return getData().getSize();
    }

    @Transient
    public long getCurrent() {
        return getData().getCurrent();
    }

    @Transient
    public long getTotal() {
        return getData().getTotal();
    }

    @Override
    public PageResult<T> ifPresent(BiConsumer<T, PageResult<T>> consumer) {
        if(this.getData() != null && this.getData().getRecords() != null) {
            consumer.accept(this.getData(), this);
        }

        return this;
    }

    /**
     * 返回记录数列表的stream
     * @return
     */
    public <E extends Entity> Stream<E> stream() {
        return ofNullable().map(eiPage -> eiPage
                .getRecords()).orElse(Collections.EMPTY_LIST).stream();
    }
}
