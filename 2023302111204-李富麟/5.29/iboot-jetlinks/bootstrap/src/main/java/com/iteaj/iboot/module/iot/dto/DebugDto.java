package com.iteaj.iboot.module.iot.dto;

import com.iteaj.framework.IVOption;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class DebugDto {

    /**
     * 设备信息
     */
    private DeviceDto device;

    /**
     * 产品信息
     */
    private ProductDto product;

    /**
     * 接口调试参数
     */
    private Map<String, List<IVOption>> apiCodeDebugMap;

    public void addApiDebugParam(String apiCode, List<IVOption> params) {
        if(apiCodeDebugMap == null) {
            apiCodeDebugMap = new HashMap<>();
        }

        apiCodeDebugMap.put(apiCode, params);
    }
}
