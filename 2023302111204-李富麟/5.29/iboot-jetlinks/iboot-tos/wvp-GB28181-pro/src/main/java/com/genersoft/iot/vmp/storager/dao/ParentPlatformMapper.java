package com.genersoft.iot.vmp.storager.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.genersoft.iot.vmp.gb28181.bean.ParentPlatform;
import com.genersoft.iot.vmp.storager.dao.dto.ChannelSourceInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用于存储上级平台
 */
@Mapper
public interface ParentPlatformMapper extends BaseMapper<ParentPlatform> {

    List<ParentPlatform> getParentPlatformList();

    Page<ParentPlatform> parentPlatformByPage(Page page, ParentPlatform entity);

    default ParentPlatform getParentPlatByServerGBId(String platformGbId) {
        return selectOne(Wrappers.<ParentPlatform>lambdaQuery().eq(ParentPlatform::getServerGBId, platformGbId));
    }

    default int updateParentPlatformStatus(@Param("platformGbID") String platformGbID, @Param("online") boolean online) {
        return this.update(null, Wrappers.<ParentPlatform>lambdaUpdate()
                .set(ParentPlatform::isStatus, online)
                .eq(ParentPlatform::getServerGBId, platformGbID));
    }

    List<ChannelSourceInfo> getChannelSource(@Param("platform_id") String platform_id, @Param("gbId") String gbId);
}
