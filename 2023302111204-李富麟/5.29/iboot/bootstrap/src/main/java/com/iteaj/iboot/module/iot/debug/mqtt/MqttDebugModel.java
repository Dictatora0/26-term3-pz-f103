package com.iteaj.iboot.module.iot.debug.mqtt;

import com.iteaj.iboot.module.iot.debug.DebugModel;
import lombok.Data;

@Data
public class MqttDebugModel extends DebugModel {

    /**
     * ip地址
     */
    private String ip;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 订阅的topic
     */
    private String topic;

    /**
     * 型号
     */
    private String model;

    /**
     * subscription、publish
     */
    private String cmd;

    /**
     * 要发布的值
     */
    private String value;

    /**
     * 类型 int、float、double ...
     */
    private String type;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
