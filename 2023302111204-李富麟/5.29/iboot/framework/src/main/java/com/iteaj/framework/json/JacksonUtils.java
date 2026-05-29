package com.iteaj.framework.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

public class JacksonUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();
    // 基于泛型, 存储类型信息
    private static ObjectMapper GENERIC_MAPPER = new ObjectMapper();


    static {
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.PUBLIC_ONLY);

        GENERIC_MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.PUBLIC_ONLY);
        // json包含类型信息, 支持直接反序列号对象
        GENERIC_MAPPER.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
    }

    public static ObjectMapper getMapper() {
        return objectMapper;
    }

    public static ObjectMapper getGenericMapper() {
        return GENERIC_MAPPER;
    }
}
