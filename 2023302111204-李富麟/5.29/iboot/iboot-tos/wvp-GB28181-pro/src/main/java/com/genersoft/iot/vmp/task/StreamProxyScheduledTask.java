package com.genersoft.iot.vmp.task;

import com.alibaba.fastjson2.JSONObject;
import com.genersoft.iot.vmp.media.zlm.ZLMRESTfulUtils;
import com.genersoft.iot.vmp.media.zlm.dto.MediaServerItem;
import com.genersoft.iot.vmp.media.zlm.dto.StreamProxyItem;
import com.genersoft.iot.vmp.service.IMediaServerService;
import com.genersoft.iot.vmp.service.IStreamProxyService;
import com.genersoft.iot.vmp.storager.IVideoManagerStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

//@Component
public class StreamProxyScheduledTask {

    @Autowired
    private ZLMRESTfulUtils zlmresTfulUtils;

    @Autowired
    private IMediaServerService mediaServerService;

    @Autowired
    private IStreamProxyService streamProxyService;

    @Autowired
    private IVideoManagerStorage videoManagerStorage;

    @Scheduled(cron = "* 0/10 * * * ?")
    public void asyncCheckStreamProxyStatus() {

        List<MediaServerItem> all = mediaServerService.getAllOnline();

        if (CollectionUtils.isEmpty(all)){
            return;
        }

        Map<String, MediaServerItem> serverItemMap = all.stream().collect(Collectors.toMap(MediaServerItem::getId, Function.identity(), (m1, m2) -> m1));

        List<StreamProxyItem> list = videoManagerStorage.getStreamProxyListForEnable(true);

        if (CollectionUtils.isEmpty(list)){
            return;
        }

        for (StreamProxyItem streamProxyItem : list) {

            MediaServerItem mediaServerItem = serverItemMap.get(streamProxyItem.getMediaServerId());

            // TODO 支持其他 schema
            JSONObject mediaInfo = zlmresTfulUtils.isMediaOnline(mediaServerItem, streamProxyItem.getApp(), streamProxyItem.getStream(), "rtsp");

            if (mediaInfo == null){
                streamProxyItem.setStatus(false);
            } else {
                if (mediaInfo.getInteger("code") == 0 && mediaInfo.getBoolean("online")) {
                    streamProxyItem.setStatus(true);
                } else {
                    streamProxyItem.setStatus(false);
                }
            }

            streamProxyService.updateStreamProxy(streamProxyItem);
        }
    }
}
