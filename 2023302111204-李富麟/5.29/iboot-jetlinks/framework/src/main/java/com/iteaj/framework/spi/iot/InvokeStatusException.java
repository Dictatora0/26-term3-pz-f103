package com.iteaj.framework.spi.iot;

import com.iteaj.framework.spi.iot.consts.CollectStatus;

public class InvokeStatusException extends ProtocolInvokeException{

    private CollectStatus status;

    public InvokeStatusException(String message, CollectStatus status) {
        super(message);
        this.status = status;
    }

    public InvokeStatusException(String message, Throwable cause, CollectStatus status) {
        super(message, cause);
        this.status = status;
    }

    public InvokeStatusException(String message, String apiCode, String protocolCode, CollectStatus status) {
        super(message, apiCode, protocolCode);
        this.status = status;
    }

    public CollectStatus getStatus() {
        return status;
    }

    public void setStatus(CollectStatus status) {
        this.status = status;
    }
}
