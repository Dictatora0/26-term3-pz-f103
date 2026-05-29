package com.iteaj.iboot.module.core.service;

import com.iteaj.framework.IBaseService;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.result.ListResult;
import com.iteaj.iboot.module.core.entity.DictData;

/**
 * jdk：1.8
 *
 * @author iteaj
 * create time：2019/6/24
 */
public interface IDictDataService extends IBaseService<DictData> {

    ListResult<DictData> selectByType(String type);

    BooleanResult removeByType(String type);
}
