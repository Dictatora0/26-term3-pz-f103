package com.iteaj.framework.spi.iot.protocol;

import com.iteaj.framework.spi.iot.ProtocolInvokeException;

public class NotIotClientException extends ProtocolInvokeException {

    public NotIotClientException(String message) {
        super(message);
    }

    public NotIotClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
