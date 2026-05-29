package com.iteaj.framework.spi.gbs;

import cn.hutool.core.util.StrUtil;

import java.util.List;

public class GbsRealtimeTree {

    /**
     * 设备编号
     */
    private String deviceId;

    /**
     * 通道编号
     */
    private String channelId;

    /**
     * 在线状态
     */
    private Boolean status;

    /**
     * 通道或者设备名称
     */
    private String name;

    /**
     * device 设备
     * channel 通道
     */
    private String type;

    /**
     * 子设备数
     */
    private Integer childrenNum;

    /**
     * 子记录
     */
    private List<GbsRealtimeTree> children;

    public GbsRealtimeTree(String deviceId, String channelId, String name, String type, Boolean status, Integer childrenNum) {
        this.deviceId = deviceId;
        this.channelId = channelId;
        this.name = name;
        this.type = type;
        this.status = status;
        this.childrenNum = childrenNum;
    }

    public static GbsRealtimeTree buildDevice(String deviceId, String name, Boolean status, Integer childrenNum) {
        return new GbsRealtimeTree(deviceId, "", name, "device", status, childrenNum);
    }

    public static GbsRealtimeTree buildChannel(String deviceId, String channelId, String name, Boolean status) {
        return new GbsRealtimeTree(deviceId, channelId, name, "channel", status, 0);
    }

    public String getId() {
        return (StrUtil.isBlank(this.getDeviceId()) ? "" : this.getDeviceId()) + (StrUtil.isBlank(this.getChannelId()) ? "" : "_" + this.getChannelId());
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Integer getChildrenNum() {
        return childrenNum;
    }

    public void setChildrenNum(Integer childrenNum) {
        this.childrenNum = childrenNum;
    }

    public List<GbsRealtimeTree> getChildren() {
        return children;
    }

    public void setChildren(List<GbsRealtimeTree> children) {
        this.children = children;
    }
}
