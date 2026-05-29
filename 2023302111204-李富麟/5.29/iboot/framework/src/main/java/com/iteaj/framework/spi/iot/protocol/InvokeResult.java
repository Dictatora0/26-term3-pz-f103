package com.iteaj.framework.spi.iot.protocol;

import com.iteaj.iot.Message;
import com.iteaj.iot.Protocol;
import com.iteaj.iot.consts.ExecStatus;

import java.beans.Transient;

/**
 * 接口执行结果
 */
public class InvokeResult {

    /**
     * 返回值
     */
    private Object value;

    /**
     * 失败原因
     */
    private String reason;

    /**
     * 执行状态
     */
    private ExecStatus status;

    /**
     * 执行协议
     */
    private Protocol protocol;

    private static Message EMPTY_MESSAGE = new EmptyMessage();

    public InvokeResult() { }

    public InvokeResult(ExecStatus status) {
        this(status.desc, status);
    }

    public InvokeResult(String reason) {
       this(reason, ExecStatus.fail);
    }

    public InvokeResult(String reason, ExecStatus status) {
        this.reason = reason;
        this.status = status;
    }

    public InvokeResult(Object value) {
        this(value, "采集成功", ExecStatus.success);
    }

    public InvokeResult(Object value, String reason, ExecStatus status) {
        this.value = value;
        this.reason = reason;
        this.status = status;
    }

    public static InvokeResult success(Protocol protocol) {
        return new InvokeResult(ExecStatus.success).setProtocol(protocol);
    }

    public static InvokeResult success(Object value, Protocol protocol) {
        return new InvokeResult(ExecStatus.success).setValue(value).setProtocol(protocol);
    }

    public static InvokeResult status(ExecStatus status, Protocol protocol) {
        return new InvokeResult(status).setProtocol(protocol);
    }

    public static InvokeResult status(ExecStatus status, Object value, Protocol protocol) {
        return new InvokeResult(status).setValue(value).setProtocol(protocol);
    }

    public static InvokeResult fail(String reason, Protocol protocol) {
        return new InvokeResult(reason).setProtocol(protocol);
    }

    @Transient
    public Message getRequestMessage() {
        return protocol != null ? (protocol.requestMessage() != null ? protocol.requestMessage() : EMPTY_MESSAGE) : EMPTY_MESSAGE;
    }

    @Transient
    public Message getResponseMessage() {
        return protocol != null ? (protocol.responseMessage() != null ? protocol.responseMessage() : EMPTY_MESSAGE) : EMPTY_MESSAGE;
    }

    public Object getValue() {
        return value;
    }

    public InvokeResult setValue(Object value) {
        this.value = value; return this;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ExecStatus getStatus() {
        return status;
    }

    public void setStatus(ExecStatus status) {
        this.status = status;
    }

    @Transient
    public Protocol getProtocol() {
        return protocol;
    }

    public InvokeResult setProtocol(Protocol protocol) {
        this.protocol = protocol; return this;
    }

    public static class EmptyMessage implements Message{

        @Override
        public int length() {
            return 0;
        }

        @Override
        public byte[] getMessage() {
            return new byte[0];
        }

        @Override
        public MessageHead getHead() {
            return null;
        }

        @Override
        public MessageBody getBody() {
            return null;
        }

        @Override
        public String toString() {
            return "";
        }
    };
}
