package com.iteaj.iboot.module.iot.debug;

import lombok.Data;

@Data
public class DebugRequestModel<D extends DebugModel> {

    private D model;

    private String clientSn;

}
