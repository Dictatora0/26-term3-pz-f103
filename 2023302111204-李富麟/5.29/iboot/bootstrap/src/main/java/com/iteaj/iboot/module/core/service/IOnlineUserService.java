package com.iteaj.iboot.module.core.service;

import com.iteaj.framework.IBaseService;
import com.iteaj.iboot.module.core.dto.OnlineCountDto;
import com.iteaj.iboot.module.core.entity.OnlineUser;

/**
 * <p>
 * 在线用户 服务类
 * </p>
 *
 * @author iteaj
 * @since 2020-06-20
 */
public interface IOnlineUserService extends IBaseService<OnlineUser> {

    void updateBySessionId(OnlineUser onlineUser);

    /**
     * 统计当前在线人数和当天在线人数
     * @return
     */
    OnlineCountDto countCurrentOnline();

}
