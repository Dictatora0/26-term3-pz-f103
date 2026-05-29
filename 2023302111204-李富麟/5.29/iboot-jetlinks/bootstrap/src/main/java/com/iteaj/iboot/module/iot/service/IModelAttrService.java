package com.iteaj.iboot.module.iot.service;

import com.iteaj.framework.IBaseService;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.result.Result;
import com.iteaj.iboot.module.iot.dto.StatusSwitchDto;
import com.iteaj.iboot.module.iot.entity.ModelAttr;
import com.iteaj.iboot.module.iot.entity.ModelAttrDict;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 物模型属性 服务类
 * </p>
 *
 * @author iteaj
 * @since 2023-09-12
 */
public interface IModelAttrService extends IBaseService<ModelAttr> {

    /**
     * 获取物模型属性详情(包含属性字典)
     * @param id
     * @return
     */
    DetailResult<ModelAttr> detailById(Long id);

    Result<Boolean> saveOrUpdateModelAttrDict(ModelAttrDict entity);

    Result<Boolean> removeModelAttrDictByIds(List<Long> idList);

    ListResult<ModelAttrDict> listModelAttrDicts(ModelAttrDict entity);

    boolean hasBinded(Collection<? extends Serializable> idList);

    /**
     * 切换状态属性
     * @param id
     * @return
     */
    BooleanResult switchCtrlStatus(StatusSwitchDto<Boolean> dto);
}
