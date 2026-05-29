package com.iteaj.framework.spi.excel;

import java.io.Serializable;

public interface IExcelService {

    /**
     * 字典值转成字典名称
     * @param dictType
     * @param dictValue
     * @return
     */
    String toDictName(String dictType, Serializable dictValue);

    /**
     * 字典名称转成字典值
     * @param dictType
     * @param dictName
     * @return
     */
    Serializable toDictValue(String dictType, String dictName);

    /**
     * 机构id转成机构名称
     * @param id
     * @return
     */
    String toOrgName(Serializable id);

    /**
     * 机构名称转成机构id
     * @param name
     * @return
     */
    Serializable toOrgValue(String name);
}
