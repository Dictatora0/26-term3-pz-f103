package com.iteaj.iboot.module.iot.dto;

import com.iteaj.iboot.module.iot.entity.ModelApi;
import com.iteaj.iboot.module.iot.entity.ModelAttr;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 协议模型转产品模型
 */
@Data
@Accessors(chain = true)
public class ProtocolToProductModel {

    /**
     * 物模型属性列表
     */
    private List<ModelAttr> attrs = new ArrayList<>();

    /**
     * 协议指令接口
     */
    private List<ModelApi> funcApis = new ArrayList<>();

    /**
     * 模型事件接口
     */
    private List<ModelApi> eventApis = new ArrayList<>();

}
