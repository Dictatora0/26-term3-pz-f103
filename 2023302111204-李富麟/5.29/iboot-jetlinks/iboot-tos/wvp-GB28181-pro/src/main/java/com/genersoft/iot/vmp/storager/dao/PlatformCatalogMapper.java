package com.genersoft.iot.vmp.storager.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.genersoft.iot.vmp.gb28181.bean.DeviceChannel;
import com.genersoft.iot.vmp.gb28181.bean.PlatformCatalog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlatformCatalogMapper extends BaseMapper<PlatformCatalog> {

    int del(@Param("platformId") String platformId, @Param("id") String id);

    int delByPlatformId(@Param("platformId") String platformId);

    List<PlatformCatalog> selectByParentId(@Param("platformId") String platformId, @Param("parentId") String parentId);

    List<PlatformCatalog> selectByPlatForm(@Param("platformId") String platformId);

    PlatformCatalog selectDefaultByPlatFormId(@Param("platformId") String platformId);

    List<DeviceChannel> queryCatalogInPlatform(@Param("platformId") String platformId);

    PlatformCatalog selectByPlatFormAndCatalogId(@Param("platformId") String platformId, @Param("id") String id);

    int deleteAll(String platformId, List<String> ids);

    List<String> queryCatalogFromParent(@Param("id") String id, @Param("platformId") String platformId);
}
