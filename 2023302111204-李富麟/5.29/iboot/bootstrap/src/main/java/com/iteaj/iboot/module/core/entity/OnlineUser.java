package com.iteaj.iboot.module.core.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.iteaj.framework.BaseEntity;
import com.iteaj.framework.spi.OnlineSession;
import com.iteaj.framework.spi.admin.event.OnlineStatus;
import com.iteaj.iboot.module.core.enums.ClientType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * <p>
 * 在线用户
 * </p>
 *
 * @author iteaj
 * @since 2020-06-20
 */
@Data
@TableName("sys_online_user")
@Accessors(chain = true)
public class OnlineUser extends BaseEntity implements OnlineSession {

    private String account;

    /**
     * 会话编号
     */
    private String sessionId;

    /**
     * 登录时使用的浏览器
     */
    private String browse;

    /**
     * 超时时长, 从访问到离开的时间， 单位分钟
     */
    private Long expireTime;

    /**
     * 访问的本地位置
     */
    private String location;

    /**
     * 访问ip
     */
    private String accessIp;

    /**
     * 登录时间
     */
    private Date loginTime;

    /**
     * 使用的操作系统
     */
    private String os;

    /**
     * 应用类型
     */
    private ClientType type;

    /**
     * 用户状态(Online. 在线, Offline. 离线)
     */
    private OnlineStatus status;

    /**
     * 用户昵称
     */
    @TableField(condition = SqlCondition.LIKE)
    private String userNick;

    /**创建时间*/
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**更新时间*/
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public OnlineUser() { }

    public OnlineUser(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String getAccount() {
        return this.account;
    }

    @Override
    public String getAction() {
        return null;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public long getTimeout() {
        return getExpireTime();
    }

    @Override
    public Date getStartTimestamp() {
        return getCreateTime();
    }

    @Override
    public Date getLastAccessTime() {
        return getUpdateTime();
    }

}
