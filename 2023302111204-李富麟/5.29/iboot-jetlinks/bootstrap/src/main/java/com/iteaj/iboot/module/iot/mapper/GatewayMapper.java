package com.iteaj.iboot.module.iot.mapper;

import com.iteaj.framework.IVOption;
import com.iteaj.framework.spi.iot.consts.ConnectionType;
import com.iteaj.framework.spi.iot.consts.GatewayType;
import com.iteaj.framework.spi.iot.consts.TransportProtocol;
import com.iteaj.iboot.module.iot.dto.GatewayDto;
import com.iteaj.iboot.module.iot.entity.Gateway;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 协议网关 Mapper 接口
 * </p>
 *
 * @author iteaj
 * @since 2023-09-12
 */
public interface GatewayMapper extends BaseMapper<Gateway> {

    Boolean isBindProduct(Long gatewayId);

    GatewayDto detailById(Long id);

    GatewayDto getByProtocolId(Long protocolId);

    List<IVOption> protocols(GatewayType type, TransportProtocol protocol, ConnectionType connectionType);

    Gateway getByPort(Integer port);
}
