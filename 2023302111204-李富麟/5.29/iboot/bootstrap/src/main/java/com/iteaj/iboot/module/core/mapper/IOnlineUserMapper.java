package com.iteaj.iboot.module.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iteaj.iboot.module.core.dto.OnlineCountDto;
import com.iteaj.iboot.module.core.entity.OnlineUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * create time: 2020/6/20
 *
 * @author iteaj
 * @since 1.0
 */
@Mapper
public interface IOnlineUserMapper extends BaseMapper<OnlineUser> {

    /**
     * 统计当天访问人数
     * @return
     */
    OnlineCountDto countCurrentOnline();

}
