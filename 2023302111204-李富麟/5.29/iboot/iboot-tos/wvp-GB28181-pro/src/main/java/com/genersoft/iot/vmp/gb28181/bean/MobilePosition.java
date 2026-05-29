package com.genersoft.iot.vmp.gb28181.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iteaj.framework.Entity;

/**
 * @description: 移动位置bean
 * @author: lawrencehj
 * @date: 2021年1月23日
 */
@TableName("wvp_device_mobile_position")
public class MobilePosition implements Entity<Long> {

    private Long id;

    /**
     * 设备Id
     */
    private String deviceId;

    /**
     * 通道Id
     */
    private String channelId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 通知时间
     */
    private String time;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 海拔高度
     */
    private Double altitude;

    /**
     * 速度
     */
    private Double speed;

    /**
     * 方向
     */
    private Double direction;

    /**
     * 位置信息上报来源（Mobile Position、GPS Alarm）
     */
    private String reportSource;

    /**
     * 国内坐标系：经度坐标
     */
    private Double longitudeGcj02;

    /**
     * 国内坐标系：纬度坐标
     */
    private Double latitudeGcj02;

    /**
     * 国内坐标系：经度坐标
     */
    private Double longitudeWgs84;

    /**
     * 国内坐标系：纬度坐标
     */
    private Double latitudeWgs84;

    /**
     * 创建时间
     */
    private String createTime;

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

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getDirection() {
        return direction;
    }

    public void setDirection(Double direction) {
        this.direction = direction;
    }

    public String getReportSource() {
        return reportSource;
    }

    public void setReportSource(String reportSource) {
        this.reportSource = reportSource;
    }

    public Double getLongitudeGcj02() {
        return longitudeGcj02;
    }

    public void setLongitudeGcj02(Double longitudeGcj02) {
        this.longitudeGcj02 = longitudeGcj02;
    }

    public Double getLatitudeGcj02() {
        return latitudeGcj02;
    }

    public void setLatitudeGcj02(Double latitudeGcj02) {
        this.latitudeGcj02 = latitudeGcj02;
    }

    public Double getLongitudeWgs84() {
        return longitudeWgs84;
    }

    public void setLongitudeWgs84(Double longitudeWgs84) {
        this.longitudeWgs84 = longitudeWgs84;
    }

    public Double getLatitudeWgs84() {
        return latitudeWgs84;
    }

    public void setLatitudeWgs84(Double latitudeWgs84) {
        this.latitudeWgs84 = latitudeWgs84;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }
}
