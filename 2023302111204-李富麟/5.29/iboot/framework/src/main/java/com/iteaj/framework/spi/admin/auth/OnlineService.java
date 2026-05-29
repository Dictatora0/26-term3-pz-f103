package com.iteaj.framework.spi.admin.auth;

import com.iteaj.framework.spi.OnlineSession;

import java.io.Serializable;

/**
 * create time: 2020/6/25
 *
 * @author iteaj
 * @since 1.0
 */
public interface OnlineService {

    /**
     * 获取会话
     * @param sessionId
     * @return
     */
    OnlineSession getSession(Serializable sessionId);

    /**
     * 移除会话
     * @param sessionId
     */
    void removeBySessionId(Serializable sessionId);

    /**
     * 更新会话
     * @param session
     */
    void update(OnlineSession session);
}
