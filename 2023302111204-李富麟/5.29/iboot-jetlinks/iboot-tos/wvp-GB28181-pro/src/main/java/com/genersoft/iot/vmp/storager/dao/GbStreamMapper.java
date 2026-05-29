package com.genersoft.iot.vmp.storager.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.genersoft.iot.vmp.gb28181.bean.DeviceChannel;
import com.genersoft.iot.vmp.gb28181.bean.GbStream;
import com.genersoft.iot.vmp.media.zlm.dto.StreamProxyItem;
import com.genersoft.iot.vmp.media.zlm.dto.StreamPushItem;
import com.genersoft.iot.vmp.service.bean.GPSMsgInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GbStreamMapper extends BaseMapper<GbStream> {

    List<GbStream> selectAll(GbStream entity);

    Page<GbStream> selectAllByPage(Page page, GbStream entity);

    GbStream queryStreamInPlatform(@Param("platformId") String platformId, @Param("gbId") String gbId);

    List<DeviceChannel> queryGbStreamListInPlatform(String platformId, @Param("usPushingAsStatus") boolean usPushingAsStatus);

    void batchDel(List<StreamProxyItem> streamProxyItemList);

    void batchDelForGbStream(List<GbStream> gbStreams);

    void batchAdd(@Param("subList") List<StreamPushItem> subList);

    int updateStreamGPS(List<GPSMsgInfo> gpsMsgInfos);

    List<GbStream> selectAllForAppAndStream(List<StreamPushItem> streamPushItems);

    default void updateMediaServer(String app, String stream, String mediaServerId) {
        this.update(null, Wrappers.<GbStream>lambdaUpdate()
                .set(GbStream::getMediaServerId, mediaServerId)
                .eq(GbStream::getApp, app)
                .eq(GbStream::getStream, stream));
    }

    int updateGbIdOrName(List<StreamPushItem> streamPushItemForUpdate);

    Boolean selectStatusForProxy(@Param("app") String app, @Param("stream") String stream);

    Boolean selectStatusForPush(@Param("app") String app, @Param("stream") String stream);

}
