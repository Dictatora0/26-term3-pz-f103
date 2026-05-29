package com.iteaj.framework.result;

/**
 * create time: 2019/11/29
 *  空结果集
 * @author iteaj
 * @since 1.0
 */
public class VoidResult extends OptionalResult<Void, VoidResult> {
    protected VoidResult() {
        super(null);
    }
}
