package com.genersoft.iot.vmp.gb28181.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iteaj.framework.Entity;
import io.swagger.v3.oas.annotations.media.Schema;

@TableName("wvp_device_channel")
@Schema(description = "通道信息")
public class DeviceChannel implements Entity<Long> {


	/**
	 * 数据库自增ID
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 通道国标编号
	 */
	private String channelId;

	/**
	 * 设备国标编号
	 */
	private String deviceId;

	/**
	 * 通道名
	 */
	private String name;

	/**
	 * 生产厂商
	 */
	private String manufacture;

	/**
	 * 型号
	 */
	private String model;

	/**
	 * 设备归属
	 */
	private String owner;

	/**
	 * 行政区域
	 */
	private String civilCode;

	/**
	 * 警区
	 */
	private String block;

	/**
	 * 安装地址
	 */
	private String address;

	/**
	 * 是否有子设备 1有, 0没有
	 */
	private Integer parental;

	/**
	 * 父级id
	 */
	private String parentId;

	/**
	 * 信令安全模式  缺省为0; 0:不采用; 2: S/MIME签名方式; 3: S/ MIME加密签名同时采用方式; 4:数字摘要方式
	 */
	private Integer safetyWay;

	/**
	 * 注册方式 缺省为1;1:符合IETFRFC3261标准的认证注册模 式; 2:基于口令的双向认证注册模式; 3:基于数字证书的双向认证注册模式
	 */
	private Integer registerWay;

	/**
	 * 证书序列号
	 */
	private String certNum;

	/**
	 * 证书有效标识 缺省为0;证书有效标识:0:无效1: 有效
	 */
	private Integer certifiable;

	/**
	 * 证书无效原因码
	 */
	private Integer errCode;

	/**
	 * 证书终止有效期
	 */
	private String endTime;

	/**
	 * 保密属性 缺省为0; 0:不涉密, 1:涉密
	 */
	private String secrecy;

	/**
	 * IP地址
	 */
	private String ipAddress;

	/**
	 * 端口号
	 */
	private Integer port;

	/**
	 * 密码
	 */
	private String password;

	/**
	 * 云台类型
	 */
	private Integer ptzType;

	private Integer customPtzType;

	/**
	 * 云台类型描述字符串
	 */
	@TableField(exist = false)
	private String ptzTypeText;

	/**
	 * 创建时间
	 */
	private String createTime;

	/**
	 * 更新时间
	 */
	private String updateTime;

	/**
	 * 在线/离线
	 * 1在线,0离线
	 * 默认在线
	 * 信令:
	 * <Status>ON</Status>
	 * <Status>OFF</Status>
	 * 遇到过NVR下的IPC下发信令可以推流， 但是 Status 响应 OFF
	 */
	private Boolean status;

	/**
	 * 经度
	 */
	private Double longitude;

	/**
	 * 纬度
	 */
	private Double latitude;

	/**
	 * GCJ02坐标系经度
	 */
	private Double longitudeGcj02;

	/**
	 * 纬度 GCJ02
	 */
	private Double latitudeGcj02;

	/**
	 * 经度 WGS84
	 */
	private Double longitudeWgs84;

	/**
	 * 纬度 WGS84
	 */
	private Double latitudeWgs84;

	/**
	 * 子设备数
	 */
	private Integer subCount;

	/**
	 * 流唯一编号，存在表示正在直播
	 */
	private String  streamId;

	/**
	 *  是否含有音频
	 */
	private Boolean hasAudio;

	/**
	 * 标记通道的类型，0->国标通道 1->直播流通道 2->业务分组/虚拟组织/行政区划
	 */
	@TableField(exist = false)
	private Integer channelType;

	/**
	 * 业务分组
	 */
	private String businessGroupId;

	/**
	 * GPS的更新时间
	 */
	private String gpsTime;

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

	public void setPtzType(int ptzType) {
		this.ptzType = ptzType;
		switch (ptzType) {
			case 0:
				this.ptzTypeText = "未知";
				break;
			case 1:
				this.ptzTypeText = "球机";
				break;
			case 2:
				this.ptzTypeText = "半球";
				break;
			case 3:
				this.ptzTypeText = "固定枪机";
				break;
			case 4:
				this.ptzTypeText = "遥控枪机";
				break;
		}
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

	public String getManufacture() {
		return manufacture;
	}

	public void setManufacture(String manufacture) {
		this.manufacture = manufacture;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getCivilCode() {
		return civilCode;
	}

	public void setCivilCode(String civilCode) {
		this.civilCode = civilCode;
	}

	public String getBlock() {
		return block;
	}

	public void setBlock(String block) {
		this.block = block;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Integer getParental() {
		return parental;
	}

	public void setParental(Integer parental) {
		this.parental = parental;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public Integer getSafetyWay() {
		return safetyWay;
	}

	public void setSafetyWay(Integer safetyWay) {
		this.safetyWay = safetyWay;
	}

	public Integer getRegisterWay() {
		return registerWay;
	}

	public void setRegisterWay(Integer registerWay) {
		this.registerWay = registerWay;
	}

	public String getCertNum() {
		return certNum;
	}

	public void setCertNum(String certNum) {
		this.certNum = certNum;
	}

	public Integer getCertifiable() {
		return certifiable;
	}

	public void setCertifiable(Integer certifiable) {
		this.certifiable = certifiable;
	}

	public Integer getErrCode() {
		return errCode;
	}

	public void setErrCode(Integer errCode) {
		this.errCode = errCode;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getSecrecy() {
		return secrecy;
	}

	public void setSecrecy(String secrecy) {
		this.secrecy = secrecy;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getPtzType() {
		return ptzType;
	}

	public void setPtzType(Integer ptzType) {
		this.ptzType = ptzType;
	}

	public Integer getCustomPtzType() {
		return customPtzType;
	}

	public void setCustomPtzType(Integer customPtzType) {
		this.customPtzType = customPtzType;
	}

	public String getPtzTypeText() {
		return ptzTypeText;
	}

	public void setPtzTypeText(String ptzTypeText) {
		this.ptzTypeText = ptzTypeText;
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

	public Boolean isStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
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

	public Integer getSubCount() {
		return subCount;
	}

	public void setSubCount(Integer subCount) {
		this.subCount = subCount;
	}

	public String getStreamId() {
		return streamId;
	}

	public void setStreamId(String streamId) {
		this.streamId = streamId;
	}

	public Boolean isHasAudio() {
		return hasAudio;
	}

	public void setHasAudio(Boolean hasAudio) {
		this.hasAudio = hasAudio;
	}

	public Integer getChannelType() {
		return channelType;
	}

	public void setChannelType(Integer channelType) {
		this.channelType = channelType;
	}

	public String getBusinessGroupId() {
		return businessGroupId;
	}

	public void setBusinessGroupId(String businessGroupId) {
		this.businessGroupId = businessGroupId;
	}

	public String getGpsTime() {
		return gpsTime;
	}

	public void setGpsTime(String gpsTime) {
		this.gpsTime = gpsTime;
	}
}
