package com.iteaj.iboot.module.iot.service;

import com.iteaj.framework.result.DetailResult;
import com.iteaj.iboot.module.iot.entity.Serial;
import com.iteaj.framework.IBaseService;

/**
 * <p>
 * 串口设备 服务类
 * </p>
 *
 * @author iteaj
 * @since 2023-04-12
 */
public interface ISerialService extends IBaseService<Serial> {

    DetailResult<Serial> getByCom(String com);
}
