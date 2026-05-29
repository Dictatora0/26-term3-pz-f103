package com.genersoft.iot.vmp.storager.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.genersoft.iot.vmp.gb28181.bean.Device;
import com.genersoft.iot.vmp.gb28181.bean.DeviceChannel;
import com.genersoft.iot.vmp.gb28181.bean.DeviceChannelInPlatform;
import com.genersoft.iot.vmp.vmanager.gb28181.platform.bean.ChannelReduce;
import com.genersoft.iot.vmp.web.gb28181.dto.DeviceChannelExtend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用于存储设备通道信息
 */
@Mapper
public interface DeviceChannelMapper extends BaseMapper<DeviceChannel> {

    List<DeviceChannel> queryChannels(@Param("deviceId") String deviceId, @Param("parentChannelId") String parentChannelId
            , @Param("query") String query, @Param("hasSubChannel") Boolean hasSubChannel, @Param("online") Boolean online
            , @Param("channelIds") List<String> channelIds);

    Page<DeviceChannel> queryChannelsByPage(Page page, @Param("deviceId") String deviceId, @Param("parentChannelId") String parentChannelId
            , @Param("query") String query, @Param("hasSubChannel") Boolean hasSubChannel, @Param("online") Boolean online);

    List<DeviceChannelExtend> queryChannelsWithDeviceInfo(@Param("deviceId") String deviceId, @Param("parentChannelId") String parentChannelId
            , @Param("query") String query, @Param("hasSubChannel") Boolean hasSubChannel, @Param("online") Boolean online
            , @Param("channelIds") List<String> channelIds);

    List<DeviceChannelExtend> queryChannelsByDeviceIdWithStartAndLimit(@Param("deviceId") String deviceId, @Param("channelIds") List<String> channelIds
            , @Param("parentChannelId") String parentChannelId, @Param("query") String query, @Param("hasSubChannel") Boolean hasSubChannel
            , @Param("online") Boolean online, @Param("start") int start, @Param("limit") int limit);

    int cleanChannelsByDeviceId(@Param("deviceId") String deviceId);

    void stopPlay(@Param("deviceId") String deviceId, @Param("channelId") String channelId);

    void startPlay(@Param("deviceId") String deviceId, @Param("channelId") String channelId, @Param("streamId") String streamId);

    List<ChannelReduce> queryChannelListInAll(@Param("query") String query, @Param("online") Boolean online
            , @Param("hasSubChannel") Boolean hasSubChannel, @Param("platformId") String platformId, @Param("catalogId") String catalogId);

    Page<ChannelReduce> queryChannelListByPage(Page page, @Param("query") String query, @Param("online") Boolean online
            , @Param("hasSubChannel") Boolean hasSubChannel, @Param("platformId") String platformId, @Param("catalogId") String catalogId);

    List<DeviceChannelInPlatform> queryChannelByPlatformId(String platformId);

    int batchAdd(@Param("addChannels") List<DeviceChannel> addChannels);

    int batchUpdate(@Param("updateChannels") List<DeviceChannel> updateChannels);

    int updateChannelSubCount(@Param("deviceId") String deviceId, @Param("channelId") String channelId);

    List<DeviceChannel> getAllChannelInPlay();

    List<DeviceChannel> queryChannelWithCatalog(String serverGBId);

    /**
     * 是否有通道
     * @param deviceId
     * @return
     */
    boolean isChannel(String deviceId);

    List<DeviceChannel> getChannelsWithoutTransform(String deviceId);

    List<Device> getDeviceByChannelId(String channelId);

    int batchDel(@Param("deleteChannelList") List<DeviceChannel> deleteChannelList);

    int batchOnline(@Param("channels") List<DeviceChannel> channels);

    int batchOffline(@Param("channels") List<DeviceChannel> channels);

    int getOnlineCount();

    // 设备主子码流逻辑END
    List<DeviceChannel> getSubChannelsByDeviceId(@Param("deviceId") String deviceId, @Param("parentId") String parentId, @Param("onlyCatalog") boolean onlyCatalog);

}
