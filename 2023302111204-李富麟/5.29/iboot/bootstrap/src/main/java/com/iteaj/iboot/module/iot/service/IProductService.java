package com.iteaj.iboot.module.iot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.IBaseService;
import com.iteaj.framework.result.*;
import com.iteaj.framework.spi.iot.consts.TransportProtocol;
import com.iteaj.iboot.module.iot.consts.DeviceType;
import com.iteaj.iboot.module.iot.consts.FuncStatus;
import com.iteaj.iboot.module.iot.consts.NameValueDto;
import com.iteaj.iboot.module.iot.dto.*;
import com.iteaj.iboot.module.iot.entity.Product;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 设备型号 服务类
 * </p>
 *
 * @author iteaj
 * @since 2022-07-22
 */
public interface IProductService extends IBaseService<Product> {

    PageResult<IPage<ProductDto>> pageOfDetail(Page page, Product entity);

    /**
     * 移除物模型相关的所有配置
     * @see com.iteaj.iboot.module.iot.entity.ModelApi
     * @see com.iteaj.iboot.module.iot.entity.ModelAttr
     * @see com.iteaj.iboot.module.iot.entity.ModelApiConfig
     * @param productId
     */
    void removeModel(Long productId);


    /**
     * 协议模型转产品模型
     * @param protocolId 协议id
     * @return
     */
    ProtocolToProductModel protocolToProductModel(Long protocolId);

    /***
     * 根据产品代码获取产品
     * @param code
     * @return
     */
    DetailResult<Product> getByCode(String code);

    DetailResult<ProductDto> detailById(Long id);

    /**
     * 是否已被设备使用
     * @param idList
     * @return
     */
    BooleanResult hasBind(Collection<? extends Serializable> idList);

    ListResult<ProductDto> listOfDetail(Product entity);

    DetailResult<ProductDto> joinDetailById(Long productId);

    DetailResult<ProductDto> debugById(Long productId);

    /**
     * 获取点位协议的产品列表
     * @return
     */
    ListResult<Product> listByPoint();

    ListResult<ProductDto> listJoinDetailByIds(List<Long> productIds);

    boolean hasBindEnabledEventSource(Long id);

    /**
     * 切换产品状态
     * @param id
     * @param status
     */
    void switchStatus(Long id, FuncStatus status);

    /**
     * 获取指定产品下面的状态控制api
     * @param productId
     * @return
     */
    DetailResult<ProductDto> getCtrlStatusModelApi(Long productId);

    /**
     * 获取指定产品下面的开关状态物模型接口
     * @param productIds
     * @return
     */
    List<ProductDto> listCtrlStatusModelAttrs(List<Long> productIds);

    /**
     * 获取指定协议下面的模型接口
     * @param protocolCode 协议码必填
     * @param productCode 产品码可以为空
     * @return
     */
    ListResult<ProductDto> listByProtocolCode(String protocolCode, String productCode);

    /**
     * 统计产品状态信息
     * @return
     */
    FuncStatusProfileDto countStatusProfile();

    /**
     * 统计各个产品下的设备数量
     * @return
     */
    List<NameValueDto> countOfDevice();

    /**
     * 统计各个设备类型下面的设备数量
     * @return
     */
    List<DeviceTypeCountDto> countOfDeviceType();

    /**
     * 获取指定协议码下的产品列表
     * @param deviceType
     * @param protocolCodes
     * @param protocol 传输协议
     * @return
     */
    ListResult<Product> listOfProtocolCodes(DeviceType deviceType, String[] protocolCodes, TransportProtocol protocol);
}
