package com.genersoft.iot.vmp.storager.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.genersoft.iot.vmp.gb28181.bean.GbStream;
import com.genersoft.iot.vmp.gb28181.bean.ParentPlatform;
import com.genersoft.iot.vmp.gb28181.bean.PlatformCatalog;
import com.genersoft.iot.vmp.gb28181.bean.PlatformGbStream;
import com.genersoft.iot.vmp.media.zlm.dto.StreamProxyItem;
import com.genersoft.iot.vmp.media.zlm.dto.StreamPushItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlatformGbStreamMapper extends BaseMapper<PlatformGbStream> {

    int batchAdd(List<StreamPushItem> streamPushItems);

    int delByAppAndStream(@Param("app") String app, @Param("stream") String stream);

    int delByPlatformId(String platformId);

    List<ParentPlatform> selectByAppAndStream(@Param("app") String app, @Param("stream") String stream);

    StreamProxyItem selectOne(@Param("app") String app, @Param("stream") String stream, @Param("platformId") String platformId);

    List<GbStream> queryChannelInParentPlatformAndCatalog(@Param("platformId") String platformId, @Param("catalogId") String catalogId);

    List<PlatformCatalog> queryChannelInParentPlatformAndCatalogForCatalog(@Param("platformId") String platformId, @Param("catalogId") String catalogId);

    List<ParentPlatform> queryPlatFormListForGBWithGBId(@Param("app") String app, @Param("stream") String stream, @Param("platforms") List<String> platforms);

    void delByGbStreams(List<GbStream> gbStreams);

    void delByAppAndStreamsByPlatformId(@Param("gbStreams") List<GbStream> gbStreams, @Param("platformId") String platformId);

    int delByPlatformAndCatalogId(@Param("platformId") String platformId, @Param("catalogId") String catalogId);
}
