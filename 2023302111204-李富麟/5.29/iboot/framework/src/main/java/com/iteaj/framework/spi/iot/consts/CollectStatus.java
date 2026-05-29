package com.iteaj.framework.spi.iot.consts;

import com.iteaj.iot.consts.ExecStatus;

public enum CollectStatus {

    Success("成功"),
    Timeout("超时"),
    Offline("设备离线"),
    Fail("失败"),
    NonDownValue("无下行参数值"),
    ;

    public String reason;

    CollectStatus(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public static CollectStatus from(ExecStatus status) {
        if(status == ExecStatus.success) {
            return Success;
        } else if(status == ExecStatus.timeout) {
            return Timeout;
        } else if(status == ExecStatus.offline) {
            return Offline;
        }

        return Fail;
    }
}
