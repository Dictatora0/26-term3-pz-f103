package com.iteaj.iboot.module.iot.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iteaj.framework.BaseEntity;
import com.iteaj.framework.mybatis.handler.FastjsonTypeHandler;
import com.iteaj.framework.spi.iot.consts.TransportProtocol;
import com.iteaj.iboot.module.iot.consts.DeviceType;
import com.iteaj.framework.spi.iot.consts.ConnectionType;
import com.iteaj.iboot.module.iot.consts.GatewayStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * <p>
 * 协议网关
 * </p>
 *
 * @author iteaj
 * @since 2023-09-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_gateway")
public class Gateway extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 网关名称
     */
    @NotBlank(message = "网关名称必填")
    @TableField(condition = SqlCondition.LIKE)
    private String name;

    /**
     * 设备类型(设备直连、边缘网关)
     */
    private DeviceType type;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 网关端口
     */
    private Integer port;

    /**
     * 用户名
     */
    private String username;

    /**
     * 是否启用ssl
     */
    private Boolean useSsl;

    /**
     * 密码
     */
    private String password;

    /**
     * 失败原因
     */
    private String reason;

    /**
     * 绑定协议
     */
    @NotNull(message = "协议必填")
    private Long protocolId;

    /**
     * 网关状态
     */
    private GatewayStatus status;

    /**
     * 网关配置
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject config;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 网关连接类型
     */
    private ConnectionType connectType;

    /**
     * 传输协议
     */
    @NotNull(message = "传输协议必填")
    private TransportProtocol protocolType;

}
