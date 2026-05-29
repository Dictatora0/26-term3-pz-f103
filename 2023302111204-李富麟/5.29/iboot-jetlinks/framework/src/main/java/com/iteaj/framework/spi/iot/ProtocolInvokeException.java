package com.iteaj.framework.spi.iot;

import com.iteaj.framework.spi.iot.protocol.ProtocolSupplierException;

public class ProtocolInvokeException extends ProtocolSupplierException {

    /**
     * 接口代码
     */
    private String apiCode;

    /**
     * 协议码
     */
    private String protocolCode;

    public ProtocolInvokeException(String message) {
        super(message);
    }

    public ProtocolInvokeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtocolInvokeException(String message, String apiCode, String protocolCode) {
        super(message);
        this.apiCode = apiCode;
        this.protocolCode = protocolCode;
    }

    public ProtocolInvokeException(String message, Throwable cause, String apiCode, String protocolCode) {
        super(message, cause);
        this.apiCode = apiCode;
        this.protocolCode = protocolCode;
    }

    public String getApiCode() {
        return apiCode;
    }

    public String getProtocolCode() {
        return protocolCode;
    }
}
