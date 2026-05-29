package com.iteaj.framework.spi.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.iteaj.framework.json.StringToMapDeserializer;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;

@Data
public class SendModel {

    /**
     * 接收人(手机号、邮箱、openid)
     */
    private List<String> accept;

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
     * @see #templateVars
     */
    private String templateId;

    /**
     * 模板变量
     * @see #templateId
     */
    @JsonDeserialize(using = StringToMapDeserializer.class)
    private LinkedHashMap<String, String> templateVars;

}
