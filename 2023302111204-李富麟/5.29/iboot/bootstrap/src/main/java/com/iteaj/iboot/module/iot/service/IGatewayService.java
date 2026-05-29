package com.iteaj.iboot.module.iot.service;

import com.iteaj.framework.IBaseService;
import com.iteaj.framework.IVOption;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.spi.iot.consts.ConnectionType;
import com.iteaj.framework.spi.iot.consts.GatewayType;
import com.iteaj.framework.spi.iot.consts.TransportProtocol;
import com.iteaj.iboot.module.iot.consts.GatewayStatus;
import com.iteaj.iboot.module.iot.dto.GatewayDto;
import com.iteaj.iboot.module.iot.entity.Gateway;

/**
 * <p>
 * 协议网关 服务类
 * </p>
 *
 * @author iteaj
 * @since 2023-09-12
 */
public interface IGatewayService extends IBaseService<Gateway> {

    boolean isBindProduct(Long gatewayId);

    DetailResult<GatewayDto> detailById(Long id);

    /**
     * 通过协议获取网关
     * @param protocolId
     * @return
     */
    DetailResult<GatewayDto> getByProtocolId(Long protocolId);

    ListResult<IVOption> protocols(GatewayType type, TransportProtocol protocol, ConnectionType connectionType);

    /**
     * 切换网关状态
     * @param id 网关id
     * @param status 状态
     */
    void switchStatus(Long id, GatewayStatus status);

    /**
     * 切换网关状态
     * @param gatewayDto
     * @param status
     */
    void switchStatus(GatewayDto gatewayDto,  GatewayStatus status);
}
