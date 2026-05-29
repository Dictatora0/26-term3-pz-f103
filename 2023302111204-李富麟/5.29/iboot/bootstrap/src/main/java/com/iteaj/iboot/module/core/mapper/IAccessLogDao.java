package com.iteaj.iboot.module.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iteaj.iboot.module.core.entity.AccessLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * create time: 2020/4/22
 *
 * @author iteaj
 * @since 1.0
 */
@Mapper
public interface IAccessLogDao extends BaseMapper<AccessLog> {

}
