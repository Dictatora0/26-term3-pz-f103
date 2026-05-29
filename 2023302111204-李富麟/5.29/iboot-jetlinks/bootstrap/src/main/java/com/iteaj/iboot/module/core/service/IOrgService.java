package com.iteaj.iboot.module.core.service;

import com.iteaj.framework.IBaseService;
import com.iteaj.framework.result.ListResult;
import com.iteaj.iboot.module.core.entity.Org;

/**
 * create time: 2019/11/26
 *
 * @author iteaj
 * @since 1.0
 */
public interface IOrgService extends IBaseService<Org> {

    ListResult<Org> selectTrees(Org org);
}
