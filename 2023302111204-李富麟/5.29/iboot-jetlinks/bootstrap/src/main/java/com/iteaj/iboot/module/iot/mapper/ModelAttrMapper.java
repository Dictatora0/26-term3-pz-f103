package com.iteaj.iboot.module.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iteaj.iboot.module.iot.entity.ModelAttr;
import com.iteaj.iboot.module.iot.entity.ModelAttrDict;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 物模型属性 Mapper 接口
 * </p>
 *
 * @author iteaj
 * @since 2023-09-12
 */
public interface ModelAttrMapper extends BaseMapper<ModelAttr> {

    /**
     * 获取物模型属性详情(包含属性字典)
     * @param id
     * @return
     */
    ModelAttr detailById(Long id);

    /**
     * 属性是否被使用
     * @param idList
     * @return
     */
    boolean hasBinded(Collection<? extends Serializable> idList);

    List<ModelAttrDict> listDetailDict(ModelAttrDict entity);

    /**
     * 切换状态属性
     * @param productId
     * @param id
     * @param status
     * @return
     */
    int switchCtrlStatus(Long productId, Long id, Boolean status);

    /**
     * 关联删除模型属性, 字典
     * @param id
     * @return
     */
    int deleteByJoin(Serializable id);

    /**
     * 关联的产品是否启用
     * @param id
     * @return
     */
    boolean isEnabledOfProduct(Serializable id);
}
