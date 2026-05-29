package com.iteaj.iboot.module.iot.dto;

import com.iteaj.framework.spi.iot.consts.ConnectionType;
import com.iteaj.framework.spi.iot.consts.TransportProtocol;
import com.iteaj.iboot.module.iot.consts.DeviceType;
import com.iteaj.iboot.module.iot.consts.DeviceTypeAlias;
import com.iteaj.iboot.module.iot.entity.Device;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeviceDto extends Device {

    /**
     * 设备类型
     */
    private DeviceType deviceType;

    /**
     * 所属网关
     */
    private Long gatewayId;

    /**
     * 网关名称
     */
    private String gatewayName;

    /**
     * 连接类型
     */
    private ConnectionType connectType;

    /**
     * 产品代码
     */
    private String productCode;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 协议码
     */
    private String protocolCode;

    /**
     * 产品类型名称
     */
    private String productTypeName;

    /**
     * 所属分组
     */
    private String deviceGroupName;

    /**
     * 设备别名
     */
    private DeviceTypeAlias alias;

    /**
     * 协议码列表
     */
    private String[] protocolCodes;

    /**
     * 协议类型
     */
    private TransportProtocol protocol;

    /**
     * 设备类型名称
     * @return
     */
    public String getDeviceTypeName() {
        return this.deviceType != null ? this.deviceType.getDesc() : null;
    }

}
