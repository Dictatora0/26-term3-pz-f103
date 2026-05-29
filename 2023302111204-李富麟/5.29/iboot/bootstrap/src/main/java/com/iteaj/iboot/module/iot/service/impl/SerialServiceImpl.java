package com.iteaj.iboot.module.iot.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.iboot.module.iot.entity.Serial;
import com.iteaj.iboot.module.iot.mapper.SerialMapper;
import com.iteaj.iboot.module.iot.service.ISerialService;
import com.iteaj.framework.BaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 串口设备 服务实现类
 * </p>
 *
 * @author iteaj
 * @since 2023-04-12
 */
@Service
public class SerialServiceImpl extends BaseServiceImpl<SerialMapper, Serial> implements ISerialService {

    @Override
    public DetailResult<Serial> getByCom(String com) {
        return getOne(Wrappers.<Serial>lambdaQuery().eq(Serial::getCom, com));
    }
}
