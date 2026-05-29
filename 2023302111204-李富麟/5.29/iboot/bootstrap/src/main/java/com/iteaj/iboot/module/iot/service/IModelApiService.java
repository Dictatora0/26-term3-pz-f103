package com.iteaj.iboot.module.iot.service;

import com.iteaj.framework.result.*;
import com.iteaj.framework.spi.iot.consts.CtrlMode;
import com.iteaj.iboot.module.iot.entity.ModelApi;
import com.iteaj.framework.IBaseService;

/**
 * <p>
 * 物模型接口 服务类
 * </p>
 *
 * @author iteaj
 * @since 2023-09-12
 */
public interface IModelApiService extends IBaseService<ModelApi> {

    /**
     * 获取指定产品下面的接口列表详情
     * @param productId
     * @return
     */
    ListResult<ModelApi> detailsOfProductId(Long productId);

    /**
     * 通过指定产品和接口代码获取记录
     * @param productId
     * @param code
     * @return
     */
    DetailResult<ModelApi> getByProductIdAndCode(Long productId, String code);

    DetailResult<ModelApi> getByCode(String code);

    Result<ModelApi> detailById(Long id);

    BooleanResult updateAsStatus(Long productId, String code);

    CtrlMode getProtocolCtrlModelByProductId(Long productId);

    /**
     * 获取已启用产品下的状态协议
     * @return
     */
    ListResult<ModelApi> listAsStatusModelApi();

    /**
     * 获取指定产品下面的状态接口
     * @param productId
     * @return
     */
    DetailResult<ModelApi> getAsStatusModelApi(Long productId);

    DetailResult<ModelApi> detailByCode(String modelApiCode);
}
