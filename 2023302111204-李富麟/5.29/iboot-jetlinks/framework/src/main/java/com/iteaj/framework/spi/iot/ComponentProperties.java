package com.iteaj.framework.spi.iot;

public interface ComponentProperties {

    /**
     * 主机
     * @return
     */
    String getHost();

    /**
     * 端口
     * @return
     */
    Integer getPort();



    /**
     * 读写空闲
     * @return
     */
    Long getAllIdle();

    /**
     * 读空闲
     * @return
     */
    Long getReadIdle();

    /**
     * 写空闲
     * @return
     */
    Long getWriteIdle();
}
