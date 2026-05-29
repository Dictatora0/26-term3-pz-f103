package com.iteaj.iboot.module.iot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.result.*;
import com.iteaj.framework.spi.iot.DeviceProtocolSupplier;
import com.iteaj.iboot.module.iot.dto.ProductDto;
import com.iteaj.iboot.module.iot.dto.ProtocolDto;
import com.iteaj.iboot.module.iot.dto.ProtocolToProductModel;
import com.iteaj.iboot.module.iot.entity.Protocol;
import com.iteaj.framework.IBaseService;
import com.iteaj.iboot.module.iot.entity.ProtocolApi;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 报文协议 服务类
 * </p>
 *
 * @author iteaj
 * @since 2023-09-12
 */
public interface IProtocolService extends IBaseService<Protocol> {

    DetailResult<ProtocolDto> getByDetail(Long id);

    PageResult<IPage<ProtocolDto>> pageDetail(Page<Protocol> page, ProtocolDto entity);

    /**
     * 加载协议api
     * @param code 协议代码
     * @return
     */
    ProtocolDto loadProtocol(String code);

    ProtocolDto loadProtocol(DeviceProtocolSupplier supplier);

    /**
     * 是否有绑定网关
     * @param idList
     * @return
     */
    Boolean isBindGateway(Collection<? extends Serializable> idList);

    /**
     * 通过协议代码获取协议
     * @param code
     * @return
     */
    DetailResult<Protocol> getByCode(String code);
}
