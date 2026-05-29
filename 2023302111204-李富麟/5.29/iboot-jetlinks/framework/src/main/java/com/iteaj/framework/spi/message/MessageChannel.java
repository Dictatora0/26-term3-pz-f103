package com.iteaj.framework.spi.message;

import cn.hutool.json.JSONObject;

public interface MessageChannel {

    /**
     * 通道标识
     * @return
     */
    String getId();

    /**
     * 通道名称
     * @return
     */
    String getChannel();

    /**
     * 额外配置
     * @return
     */
    JSONObject getExtra();

    /**
     * 返回配置参数
     * @see #getExtra() 所需要的字段参数
     * @return
     */
    String getExtraParams();
}
