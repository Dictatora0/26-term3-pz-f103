package com.iteaj.iboot.module.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iteaj.iboot.module.core.mapper.IAccessLogDao;
import com.iteaj.iboot.module.core.entity.AccessLog;
import com.iteaj.iboot.module.core.service.IAccessLogService;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.result.PageResult;
import org.springframework.stereotype.Service;

/**
 * create time: 2020/4/22
 *
 * @author iteaj
 * @since 1.0
 */
@Service
public class AccessLogServiceImpl extends BaseServiceImpl<IAccessLogDao, AccessLog> implements IAccessLogService {

    @Override
    public PageResult<IPage<AccessLog>> page(IPage<AccessLog> page, AccessLog condition) {
        Object[] section = condition.getSection();
        Object start = section != null && section.length>0 ? section[0] : null;
        Object end = section != null && section.length>1 ? section[1] : null;

        return new PageResult<>(getBaseMapper().selectPage(page,
                new QueryWrapper<>(condition)
                        .gt(start!=null, "create_time", start)
                        .lt(end!=null, "create_time", end)
                        .orderByDesc("create_time")
                        .orderByDesc(condition.getMillis()!=null, "millis")));
    }
}
