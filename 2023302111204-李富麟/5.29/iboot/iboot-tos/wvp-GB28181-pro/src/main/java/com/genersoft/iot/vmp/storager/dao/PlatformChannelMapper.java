package com.genersoft.iot.vmp.storager.dao;

import com.genersoft.iot.vmp.gb28181.bean.Device;
import com.genersoft.iot.vmp.gb28181.bean.DeviceChannel;
import com.genersoft.iot.vmp.gb28181.bean.ParentPlatform;
import com.genersoft.iot.vmp.gb28181.bean.PlatformCatalog;
import com.genersoft.iot.vmp.vmanager.gb28181.platform.bean.ChannelReduce;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlatformChannelMapper {

    /**
     * 查询列表里已经关联的
     */
    List<Integer> findChannelRelatedPlatform(@Param("platformId") String platformId, @Param("channelReduces") List<ChannelReduce> channelReduces);

    int addChannels(@Param("platformId") String platformId, @Param("channelReducesToAdd") List<ChannelReduce> channelReducesToAdd);

    int delChannelForGB(@Param("platformId") String platformId, @Param("channelReducesToDel") List<ChannelReduce> channelReducesToDel);

    int delChannelForDeviceId(String deviceId);

    int cleanChannelForGB(String platformId);

    List<DeviceChannel> queryChannelInParentPlatform(@Param("platformId") String platformId, @Param("channelId") String channelId);

    List<DeviceChannel> queryAllChannelInCatalog(@Param("platformId") String platformId, @Param("catalogId") String catalogId);

    List<PlatformCatalog> queryChannelInParentPlatformAndCatalog(@Param("platformId") String platformId, @Param("catalogId") String catalogId);

    List<Device> queryVideoDeviceByPlatformIdAndChannelId(@Param("platformId") String platformId, @Param("channelId") String channelId);

    int delByCatalogId(@Param("platformId") String platformId, @Param("id") String id);

    int delByCatalogIdAndChannelIdAndPlatformId(PlatformCatalog platformCatalog);

    List<ParentPlatform> queryPlatFormListForGBWithGBId(@Param("channelId") String channelId, @Param("platforms") List<String> platforms);

    void delByPlatformId(String serverGBId);

    int delChannelForGBByCatalogId(@Param("platformId") String platformId, @Param("catalogId") String catalogId);

    List<Device> queryDeviceInfoByPlatformIdAndChannelId(@Param("platformId") String platformId, @Param("channelId") String channelId);

    List<String> queryParentPlatformByChannelId(String channelId);
}
