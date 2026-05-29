package com.iteaj.framework.spi.admin.event;

import com.iteaj.framework.Entity;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * create time: 2020/6/20
 *
 * @author iteaj
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class OnlinePayload {

    private Entity user;
    private String accessIp;
    private long expireTime;
    private Date accessTime;
    private OnlineStatus type;
    private String sessionId;
    private UserAgent userAgent;
    public OnlinePayload(OnlineStatus type, String sessionId) {
        this.type = type;
        this.sessionId = sessionId;
    }
}
