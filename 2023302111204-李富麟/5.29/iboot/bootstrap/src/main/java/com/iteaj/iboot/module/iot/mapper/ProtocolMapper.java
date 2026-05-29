package com.iteaj.iboot.module.iot.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.result.PageResult;
import com.iteaj.iboot.module.iot.dto.ProtocolDto;
import com.iteaj.iboot.module.iot.entity.Protocol;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.io.Serializable;
import java.util.Collection;

/**
 * <p>
 * 报文协议 Mapper 接口
 * </p>
 *
 * @author iteaj
 * @since 2023-09-12
 */
public interface ProtocolMapper extends BaseMapper<Protocol> {

    IPage<ProtocolDto> pageDetail(Page page, ProtocolDto entity);

    ProtocolDto getByDetail(Long id);

    Boolean isBindGateway(Collection<? extends Serializable> idList);
}
