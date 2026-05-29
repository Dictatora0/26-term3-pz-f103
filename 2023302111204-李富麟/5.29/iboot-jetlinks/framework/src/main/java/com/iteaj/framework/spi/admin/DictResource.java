package com.iteaj.framework.spi.admin;

public interface DictResource extends Resource{

    /**
     * 返回字典名称
     * @return
     */
    @Override
    String getName();

    /**
     * 字典类型
     * @return
     */
    String getType();

}
