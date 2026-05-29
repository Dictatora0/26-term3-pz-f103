package com.iteaj.iboot.module.iot.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iteaj.framework.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.*;
import java.util.stream.Collectors;

import com.iteaj.framework.IVOption;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.mybatis.handler.JacksonTypeHandler;
import com.iteaj.framework.spi.iot.consts.TriggerMode;
import com.iteaj.framework.spi.iot.consts.FuncType;
import com.iteaj.framework.spi.iot.protocol.ProtocolApiType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * <p>
 * 物模型接口
 * </p>
 *
 * @author iteaj
 * @since 2023-09-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_model_api")
public class ModelApi extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 接口类型
     */
    @NotNull(message = "接口类型必填")
    private ProtocolApiType type;

    /**
     * 协议指令
     */
    private String direct;

    /**
     * 功能代码
     */
    @NotBlank(message = "接口代码必填")
    @TableField(condition = SqlCondition.LIKE)
    private String code;

    /**
     * 显示到调试
     */
    private Boolean debug;

    /**
     * 是否为协议接口
     */
    private Boolean protocol;

    /**
     * 所属产品
     */
    @NotNull(message = "所属产品必填")
    private Long productId;

    /**
     * 接口名称
     */
    @TableField(condition = SqlCondition.LIKE)
    private String name;

    /**
     * 接口状态
     */
    private String status;

    /**
     * 接口说明
     */
    private String remark;

    /**
     * 作为状态位(用于点位控制协议)
     */
    private Boolean asStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 功能类型
     */
    private FuncType funcType;

    @TableField(exist = false)
    private String productCode;

    /**
     * 事件触发方式
     */
    private TriggerMode triggerMode;

    /**
     * 指令参数
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ObjectNode directParams;

    /**
     * 上行接口配置
     */
    @TableField(exist = false)
    private List<ModelApiConfig> upConfig;

    /**
     * 下行接口配置
     */
    @TableField(exist = false)
    private List<ModelApiConfig> downConfig;

    /**
     * 解析接口字典配置
     * @return
     */
    public List<IVOption> resolveDownApiConfig(List<ModelAttr> attrs) {
        if(!CollectionUtils.isEmpty(downConfig)) {
            List<ModelApiConfig> configs = downConfig.stream()
                    .filter(item -> !CollectionUtils.isEmpty(item.getDicts()))
                    .collect(Collectors.toList());

            if(!CollectionUtils.isEmpty(configs)) {
                if(CollectionUtils.isEmpty(attrs)) {
                    throw new ServiceException("参数错误[物模型属性为空]");
                }

                Map<Long, ModelAttr> attrMap = attrs.stream().collect(Collectors.toMap(ModelAttr::getId, item -> item));
                List<List<ModelAttrDict>> collect = configs.stream()
                        .map(item -> item.getDicts())
                        .collect(Collectors.toList());

                // 计算笛卡尔积
                List<IVOption> options = cartesianProduct(collect, attrMap).stream().map(item -> {
                    StringBuilder sb = new StringBuilder();
                    StringBuilder valueSb = new StringBuilder();
                    item.forEach(dict -> {
                        ModelAttr modelAttr = attrMap.get(dict.getModelAttrId());
                        if(modelAttr != null) {
                            sb.append(modelAttr.getName()).append("->").append(dict.getDictName()).append(", ");
                            valueSb.append(modelAttr.getField()).append("::").append(dict.getDictValue()).append(",");
                        }
                    });

                    sb.deleteCharAt(sb.length() - 2);
                    valueSb.deleteCharAt(valueSb.length() - 1);

                    return new IVOption(sb.toString(), valueSb.toString());
                }).collect(Collectors.toList());

                return options;
            }
        }

        return Collections.EMPTY_LIST;
    }

    private List<List<ModelAttrDict>> cartesianProduct(List<List<ModelAttrDict>> lists, Map<Long, ModelAttr> attrMap) {
        List<List<ModelAttrDict>> result = new ArrayList<>();
        cartesianProductHelper(lists, 0, new ArrayList<>(), result, attrMap);
        return result;
    }

    private void cartesianProductHelper(List<List<ModelAttrDict>> lists, int index, List<ModelAttrDict> current, List<List<ModelAttrDict>> result, Map<Long, ModelAttr> attrMap) {
        if (index == lists.size()) {
            result.add(new ArrayList<>(current));
            return;
        }

        List<ModelAttrDict> list = lists.get(index);
        for (ModelAttrDict value : list) {
            ModelAttr modelAttr = attrMap.get(value.getModelAttrId());
            if(modelAttr != null) {
                current.add(value);
            }

            cartesianProductHelper(lists, index + 1, current, result, attrMap);
            current.remove(current.size() - 1);
        }
    }
}
