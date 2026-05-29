package com.iteaj.framework.spi.message;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SendTemplate {

    /**
     * 模板类型
     */
    private String type;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 发送标题
     */
    private String title;

    /**
     * 发送内容
     */
    private Object content;

    /**
     * 扩展字段
     */
    private Object extra;

    /**
     * 用于发送短信模板
     */
    private String templateId;

    /**
     * 接收人标识
     */
    private JSONArray accepts;
}
