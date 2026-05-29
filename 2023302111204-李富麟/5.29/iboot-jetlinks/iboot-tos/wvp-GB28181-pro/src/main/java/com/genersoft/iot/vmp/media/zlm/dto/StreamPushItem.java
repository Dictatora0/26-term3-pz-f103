package com.genersoft.iot.vmp.media.zlm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.genersoft.iot.vmp.gb28181.bean.GbStream;
import com.genersoft.iot.vmp.media.zlm.dto.hook.OnStreamChangedHookParam;
import com.genersoft.iot.vmp.utils.DateUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@TableName(value = "wvp_stream_push", excludeProperty = {"gbStreamId", "platformId"
        , "catalogId", "gbId", "name", "longitude", "latitude", "streamType"})
@Schema(description = "推流信息")
public class StreamPushItem extends GbStream implements Comparable<StreamPushItem>{

    /**
     * id
     */
    @TableId
    @Schema(description = "id")
    private Long id;

    /**
     * 观看总人数，包括hls/rtsp/rtmp/http-flv/ws-flv
     */
    @Schema(description = "观看总人数")
    private String totalReaderCount;

    /**
     * 协议 包括hls/rtsp/rtmp/http-flv/ws-flv
     */
    @TableField(exist = false)
    @Schema(description = "协议 包括hls/rtsp/rtmp/http-flv/ws-flv")
    private List<MediaSchema> schemas;

    /**
     * 产生源类型，
     * unknown = 0,
     * rtmp_push=1,
     * rtsp_push=2,
     * rtp_push=3,
     * pull=4,
     * ffmpeg_pull=5,
     * mp4_vod=6,
     * device_chn=7
     */
    @Schema(description = "产生源类型")
    private Integer originType;

    /**
     * 客户端和服务器网络信息，可能为null类型
     */
    @TableField(exist = false)
    @Schema(description = "客户端和服务器网络信息，可能为null类型")
    private OnStreamChangedHookParam.OriginSock originSock;

    /**
     * 产生源类型的字符串描述
     */
    @Schema(description = "产生源类型的字符串描述")
    private String originTypeStr;

    /**
     * 产生源的url
     */
    @TableField(exist = false)
    @Schema(description = "产生源的url")
    private String originUrl;

    /**
     * 存活时间，单位秒
     */
    @Schema(description = "存活时间，单位秒")
    private Long aliveSecond;

    /**
     * 音视频轨道
     */
    @TableField(exist = false)
    @Schema(description = "音视频轨道")
    private List<OnStreamChangedHookParam.MediaTrack> tracks;

    /**
     * 音视频轨道
     */
    @TableField(exist = false)
    @Schema(description = "音视频轨道")
    private String vhost;

    /**
     * 使用的服务ID
     */
    @TableField(exist = false)
    @Schema(description = "使用的服务ID")
    private String serverId;

    /**
     * 推流时间
     */
    @Schema(description = "推流时间")
    private String pushTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private String updateTime;

    /**
     * 是否正在推流
     */
    @Schema(description = "是否正在推流")
    private Boolean pushIng;

    /**
     * 是否自己平台的推流
     */
    @Schema(description = "是否自己平台的推流")
    private Boolean self;

    public String getVhost() {
        return vhost;
    }

    public void setVhost(String vhost) {
        this.vhost = vhost;
    }

    @Override
    public int compareTo(@NotNull StreamPushItem streamPushItem) {
        return Long.valueOf(DateUtil.yyyy_MM_dd_HH_mm_ssToTimestamp(this.getCreateTime())
                - DateUtil.yyyy_MM_dd_HH_mm_ssToTimestamp(streamPushItem.getCreateTime())).intValue();
    }

    public static class MediaSchema {
        private String schema;
        private Long bytesSpeed;

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }

        public Long getBytesSpeed() {
            return bytesSpeed;
        }

        public void setBytesSpeed(Long bytesSpeed) {
            this.bytesSpeed = bytesSpeed;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTotalReaderCount() {
        return totalReaderCount;
    }

    public void setTotalReaderCount(String totalReaderCount) {
        this.totalReaderCount = totalReaderCount;
    }

    public List<MediaSchema> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<MediaSchema> schemas) {
        this.schemas = schemas;
    }

    public int getOriginType() {
        return originType;
    }

    public void setOriginType(int originType) {
        this.originType = originType;
    }

    public OnStreamChangedHookParam.OriginSock getOriginSock() {
        return originSock;
    }

    public void setOriginSock(OnStreamChangedHookParam.OriginSock originSock) {
        this.originSock = originSock;
    }


    public String getOriginTypeStr() {
        return originTypeStr;
    }

    public void setOriginTypeStr(String originTypeStr) {
        this.originTypeStr = originTypeStr;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public Long getAliveSecond() {
        return aliveSecond;
    }

    public void setAliveSecond(Long aliveSecond) {
        this.aliveSecond = aliveSecond;
    }

    public List<OnStreamChangedHookParam.MediaTrack> getTracks() {
        return tracks;
    }

    public void setTracks(List<OnStreamChangedHookParam.MediaTrack> tracks) {
        this.tracks = tracks;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getPushTime() {
        return pushTime;
    }

    public void setPushTime(String pushTime) {
        this.pushTime = pushTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isPushIng() {
        return pushIng;
    }

    public void setPushIng(boolean pushIng) {
        this.pushIng = pushIng;
    }

    public boolean isSelf() {
        return self;
    }

    public void setSelf(boolean self) {
        this.self = self;
    }

}

