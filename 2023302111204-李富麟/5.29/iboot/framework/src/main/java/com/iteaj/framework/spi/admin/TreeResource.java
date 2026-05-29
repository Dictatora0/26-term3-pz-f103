package com.iteaj.framework.spi.admin;

import java.util.Collection;

public interface TreeResource extends Resource {

    /**
     * 子资源列表
     * @return
     */
    Collection getChildren();

    void setChildren(Collection children);
}
