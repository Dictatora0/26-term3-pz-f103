package com.iteaj.iboot.module.iot.collect.websocket;

import com.iteaj.framework.spi.iot.SignalOrFieldValue;
import com.iteaj.iboot.module.iot.cache.entity.RealtimeStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class PushData {

    private String uid;

    private String type;

    private Object value;

    protected PushData(String uid, String type, Object value) {
        this.uid = uid;
        this.type = type;
        this.value = value;
    }

    public static PushData build(String uid, String type, Object value) {
        return new PushData(uid, type, value);
    }

    public static PushData buildStatus(RealtimeStatus status) {
        return new PushData(status.getUid(), "status", status);
    }

    public static PushData buildModel(String uid, Map<String, SignalOrFieldValue> value) {
        return new PushData(uid, "model", value);
    }

    public static PushData buildSignal(String uid, Map<String, SignalOrFieldValue> value) {
        return new PushData(uid, "signal", value);
    }
}
