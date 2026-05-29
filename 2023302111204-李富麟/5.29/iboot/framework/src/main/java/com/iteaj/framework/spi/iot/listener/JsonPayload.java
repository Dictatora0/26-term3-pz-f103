package com.iteaj.framework.spi.iot.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;

/**
 * json格式的负载
 */
public class JsonPayload implements IotEventPayload{

    private JSON json;

    public JsonPayload() {
        this.json = new JSONObject();
    }

    public JsonPayload(Object value) {
        this.json = (JSON) JSON.toJSON(value);
    }

    public JSON getJson() {
        return json;
    }

    public <T> T get(String path) {
        JSONPath compile = JSONPath.compile(path);
        return (T) compile.eval(json);
    }

    public <T> T toBean(Class<T> clazz) {
        return this.json.toJavaObject(clazz);
    }
}
