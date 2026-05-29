package com.iteaj.framework.spi.iot.protocol;

import com.iteaj.framework.exception.FrameworkException;

public class ProtocolSupplierException extends FrameworkException {

    public ProtocolSupplierException(String message) {
        super(message);
    }

    public ProtocolSupplierException(String message, Throwable cause) {
        super(message, cause);
    }

}
