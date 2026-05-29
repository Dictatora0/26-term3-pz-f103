package com.genersoft.iot.vmp.gb28181.bean;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iteaj.framework.Entity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.beans.Transient;

/**
 * 国标设备/平台
 * @author lin
 */
@TableName("wvp_device")
@Schema(description = "国标设备/平台")
public class Device implements Entity<Long> {

	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 设备国标编号
	 */
	private String deviceId;

	/**
	 * 设备名
	 */
	private String name;

	/**
	 * 生产厂商
	 */
	private String manufacturer;

	/**
	 * 型号
	 */
	private String model;

	/**
	 * 固件版本
	 */
	private String firmware;

	/**
	 * 传输协议
	 * UDP/TCP
	 */
	private String transport;

	/**
	 * 数据流传输模式
	 * UDP:udp传输
	 * TCP-ACTIVE：tcp主动模式
	 * TCP-PASSIVE：tcp被动模式
	 */
	private String streamMode;

	/**
	 * wan地址_ip
	 */
	private String  ip;

	/**
	 * wan地址_port
	 */
	private Integer port;

	/**
	 * wan地址
	 */
	private String  hostAddress;

	/**
	 * 是否在线，true为在线，false为离线
	 */
	private Boolean onLine;


	/**
	 * 注册时间
	 */
	private String registerTime;


	/**
	 * 心跳时间
	 */
	private String keepaliveTime;


	/**
	 * 心跳间隔
	 */
	private Integer keepaliveIntervalTime;

	/**
	 * 通道个数
	 */
	@TableField(exist = false)
	private Integer channelCount;

	/**
	 * 注册有效期
	 */
	private Integer expires;

	/**
	 * 创建时间
	 */
	private String createTime;

	/**
	 * 更新时间
	 */
	private String updateTime;

	/**
	 * 设备使用的媒体id, 默认为null
	 */
	private String mediaServerId;

	/**
	 * 字符集, 支持 UTF-8 与 GB2312
	 */
	private String charset ;

	/**
	 * 目录订阅周期，0为不订阅
	 */
	private int subscribeCycleForCatalog;

	/**
	 * 移动设备位置订阅周期，0为不订阅
	 */
	private int subscribeCycleForMobilePosition;

	/**
	 * 移动设备位置信息上报时间间隔,单位:秒,默认值5
	 */
	private int mobilePositionSubmissionInterval = 5;

	/**
	 * 报警订阅周期，0为不订阅
	 */
	private int subscribeCycleForAlarm;

	/**
	 * 是否开启ssrc校验，默认关闭，开启可以防止串流
	 */
	private Boolean ssrcCheck = false;

	/**
	 * 地理坐标系， 目前支持 WGS84,GCJ02
	 */
	private String geoCoordSys;

	/**
	 * 密码
	 */
	private String password;

	/**
	 * 收流IP
	 */
	private String sdpIp;

	/**
	 * SIP交互IP（设备访问平台的IP）
	 */
	private String localIp;

	/**
	 * 是否作为消息通道
	 */
	private Boolean asMessageChannel;

	/**
	 * 设备注册的事务信息
	 */
	@TableField(exist = false)
	private SipTransactionInfo sipTransactionInfo;

	/*======================设备主子码流逻辑START=========================*/
	/**
	 * 开启主子码流切换的开关（false-不开启，true-开启）
	 */
	private Boolean switchPrimarySubStream;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getFirmware() {
		return firmware;
	}

	public void setFirmware(String firmware) {
		this.firmware = firmware;
	}

	public String getTransport() {
		return transport;
	}

	public void setTransport(String transport) {
		this.transport = transport;
	}

	public String getStreamMode() {
		return streamMode;
	}

	@JsonIgnore
	public Integer getStreamModeForParam() {
		if (streamMode == null) {
			return 0;
		}
		if (streamMode.equalsIgnoreCase("UDP")) {
			return 0;
		}else if (streamMode.equalsIgnoreCase("TCP-PASSIVE")) {
			return 1;
		}else if (streamMode.equalsIgnoreCase("TCP-ACTIVE")) {
			return 2;
		}
		return 0;
	}

	public void setStreamMode(String streamMode) {
		this.streamMode = streamMode;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getHostAddress() {
		return hostAddress;
	}

	public void setHostAddress(String hostAddress) {
		this.hostAddress = hostAddress;
	}

	public Boolean isOnLine() {
		return onLine;
	}

	public void setOnLine(Boolean onLine) {
		this.onLine = onLine;
	}

	public Integer getChannelCount() {
		return channelCount;
	}

	public void setChannelCount(Integer channelCount) {
		this.channelCount = channelCount;
	}

	public String getRegisterTime() {
		return registerTime;
	}

	public void setRegisterTime(String registerTime) {
		this.registerTime = registerTime;
	}

	public String getKeepaliveTime() {
		return keepaliveTime;
	}

	public void setKeepaliveTime(String keepaliveTime) {
		this.keepaliveTime = keepaliveTime;
	}

	public Integer getExpires() {
		return expires;
	}

	public void setExpires(Integer expires) {
		this.expires = expires;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getMediaServerId() {
		return mediaServerId;
	}

	public void setMediaServerId(String mediaServerId) {
		this.mediaServerId = mediaServerId;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public Integer getSubscribeCycleForCatalog() {
		return subscribeCycleForCatalog;
	}

	public void setSubscribeCycleForCatalog(Integer subscribeCycleForCatalog) {
		this.subscribeCycleForCatalog = subscribeCycleForCatalog;
	}

	public Integer getSubscribeCycleForMobilePosition() {
		return subscribeCycleForMobilePosition;
	}

	public void setSubscribeCycleForMobilePosition(Integer subscribeCycleForMobilePosition) {
		this.subscribeCycleForMobilePosition = subscribeCycleForMobilePosition;
	}

	public Integer getMobilePositionSubmissionInterval() {
		return mobilePositionSubmissionInterval;
	}

	public void setMobilePositionSubmissionInterval(Integer mobilePositionSubmissionInterval) {
		this.mobilePositionSubmissionInterval = mobilePositionSubmissionInterval;
	}

	public Integer getSubscribeCycleForAlarm() {
		return subscribeCycleForAlarm;
	}

	public void setSubscribeCycleForAlarm(Integer subscribeCycleForAlarm) {
		this.subscribeCycleForAlarm = subscribeCycleForAlarm;
	}

	public Boolean isSsrcCheck() {
		return ssrcCheck;
	}

	public void setSsrcCheck(Boolean ssrcCheck) {
		this.ssrcCheck = ssrcCheck;
	}

	public String getGeoCoordSys() {
		return geoCoordSys;
	}

	public void setGeoCoordSys(String geoCoordSys) {
		this.geoCoordSys = geoCoordSys;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSdpIp() {
		return sdpIp;
	}

	public void setSdpIp(String sdpIp) {
		this.sdpIp = sdpIp;
	}

	public String getLocalIp() {
		return localIp;
	}

	public void setLocalIp(String localIp) {
		this.localIp = localIp;
	}

	public Integer getKeepaliveIntervalTime() {
		return keepaliveIntervalTime;
	}

	public void setKeepaliveIntervalTime(int keepaliveIntervalTime) {
		this.keepaliveIntervalTime = keepaliveIntervalTime;
	}

	public Boolean isAsMessageChannel() {
		return asMessageChannel;
	}

	public void setAsMessageChannel(Boolean asMessageChannel) {
		this.asMessageChannel = asMessageChannel;
	}

	public SipTransactionInfo getSipTransactionInfo() {
		return sipTransactionInfo;
	}

	public void setSipTransactionInfo(SipTransactionInfo sipTransactionInfo) {
		this.sipTransactionInfo = sipTransactionInfo;
	}

	public Boolean isSwitchPrimarySubStream() {
		return switchPrimarySubStream;
	}

	public void setSwitchPrimarySubStream(Boolean switchPrimarySubStream) {
		this.switchPrimarySubStream = switchPrimarySubStream;
	}


	/*======================设备主子码流逻辑END=========================*/


}
