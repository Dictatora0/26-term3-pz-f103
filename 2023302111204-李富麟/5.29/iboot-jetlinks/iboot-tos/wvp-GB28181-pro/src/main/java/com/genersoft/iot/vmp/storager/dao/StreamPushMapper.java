package com.genersoft.iot.vmp.storager.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.genersoft.iot.vmp.gb28181.bean.GbStream;
import com.genersoft.iot.vmp.media.zlm.dto.StreamPushItem;
import com.genersoft.iot.vmp.service.bean.StreamPushItemFromRedis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StreamPushMapper extends BaseMapper<StreamPushItem> {

    int delAll(List<StreamPushItem> streamPushItems);

    int delAllForGbStream(List<GbStream> gbStreams);

    Page<StreamPushItem> selectAllForList(Page page, @Param("query") String query, @Param("pushing") Boolean pushing, @Param("mediaServerId") String mediaServerId);

    List<StreamPushItem> selectAll();

    StreamPushItem getByAppAndStream(@Param("app") String app, @Param("stream") String stream);

    int addAll(List<StreamPushItem> streamPushItems);

    void deleteWithoutGBId(String mediaServerId);

    List<StreamPushItem> selectAllByMediaServerIdWithOutGbID(String mediaServerId);

    int updatePushStatus(@Param("app") String app, @Param("stream") String stream, @Param("pushIng") boolean pushIng);

    void updateStatusByMediaServerId(@Param("mediaServerId") String mediaServerId, @Param("status") boolean status);

    List<GbStream> getOnlinePusherForGbInList(List<StreamPushItemFromRedis> offlineStreams);

    void offline(List<StreamPushItemFromRedis> offlineStreams);

    List<GbStream> getOfflinePusherForGbInList(List<StreamPushItemFromRedis> onlineStreams);

    void online(List<StreamPushItemFromRedis> onlineStreams);

    List<GbStream> getOnlinePusherForGb();

    void setAllStreamOffline();

    List<String> getAllAppAndStream();

    int getAllCount();

    int getAllOnline(Boolean usePushingAsStatus);
}
