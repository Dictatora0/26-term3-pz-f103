package com.iteaj.framework.result;

public class NumberResult<T extends Number> extends OptionalResult<T, NumberResult<T>>{

    public NumberResult(T value) {
        super(value);
    }

    public NumberResult(T data, String message, long code) {
        super(data, message, code);
    }

    @Override
    public T getData() {
        return super.getData();
    }
}
