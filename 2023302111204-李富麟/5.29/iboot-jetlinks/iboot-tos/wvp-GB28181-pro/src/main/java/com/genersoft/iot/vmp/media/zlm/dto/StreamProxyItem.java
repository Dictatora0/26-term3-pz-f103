package com.genersoft.iot.vmp.media.zlm.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.genersoft.iot.vmp.gb28181.bean.GbStream;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author lin
 */
@TableName(value = "wvp_stream_proxy", excludeProperty = {"gbStreamId", "platformId"
        , "catalogId", "gbId", "longitude", "latitude", "streamType"})
@Schema(description = "拉流代理的信息")
public class StreamProxyItem extends GbStream {

    @TableId
    private Long id;
    @Schema(description = "类型")
    private String type;
    @Schema(description = "拉流地址")
    private String url;
    @Schema(description = "拉流地址")
    private String srcUrl;
    @Schema(description = "目标地址")
    private String dstUrl;
    @Schema(description = "超时时间")
    private int timeoutMs;
    @Schema(description = "ffmpeg模板KEY")
    private String ffmpegCmdKey;
    @Schema(description = "rtsp拉流时，拉流方式，0：tcp，1：udp，2：组播")
    private String rtpType;
    @Schema(description = "是否启用")
    private Boolean enable;
    @Schema(description = "是否启用音频")
    private Boolean enableAudio;
    @Schema(description = "是否启用MP4")
    private Boolean enableMp4;
    @Schema(description = "是否 无人观看时删除")
    private Boolean enableRemoveNoneReader;

    @Schema(description = "是否 无人观看时自动停用")
    private Boolean enableDisableNoneReader;

    @Schema(description = "拉流代理时zlm返回的key，用于停止拉流代理")
    private String streamKey;
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private String updateTime;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSrcUrl() {
        return srcUrl;
    }

    public void setSrcUrl(String src_url) {
        this.srcUrl = src_url;
    }

    public String getDstUrl() {
        return dstUrl;
    }

    public void setDstUrl(String dst_url) {
        this.dstUrl = dst_url;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeout_ms) {
        this.timeoutMs = timeout_ms;
    }

    public String getFfmpegCmdKey() {
        return ffmpegCmdKey;
    }

    public void setFfmpegCmdKey(String ffmpeg_cmd_key) {
        this.ffmpegCmdKey = ffmpeg_cmd_key;
    }

    public String getRtpType() {
        return rtpType;
    }

    public void setRtpType(String rtp_type) {
        this.rtpType = rtp_type;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnableMp4() {
        return enableMp4;
    }

    public void setEnableMp4(boolean enable_mp4) {
        this.enableMp4 = enable_mp4;
    }

    public boolean isEnableRemoveNoneReader() {
        return enableRemoveNoneReader;
    }

    public void setEnableRemoveNoneReader(boolean enable_remove_none_reader) {
        this.enableRemoveNoneReader = enable_remove_none_reader;
    }

    public boolean isEnableDisableNoneReader() {
        return enableDisableNoneReader;
    }

    public void setEnableDisableNoneReader(boolean enable_disable_none_reader) {
        this.enableDisableNoneReader = enable_disable_none_reader;
    }

    public boolean isEnableAudio() {
        return enableAudio;
    }

    public void setEnableAudio(boolean enable_audio) {
        this.enableAudio = enable_audio;
    }

    public String getStreamKey() {
        return streamKey;
    }

    public void setStreamKey(String streamKey) {
        this.streamKey = streamKey;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Boolean getEnableAudio() {
        return enableAudio;
    }

    public void setEnableAudio(Boolean enableAudio) {
        this.enableAudio = enableAudio;
    }

    public Boolean getEnableMp4() {
        return enableMp4;
    }

    public void setEnableMp4(Boolean enableMp4) {
        this.enableMp4 = enableMp4;
    }

    public Boolean getEnableRemoveNoneReader() {
        return enableRemoveNoneReader;
    }

    public void setEnableRemoveNoneReader(Boolean enableRemoveNoneReader) {
        this.enableRemoveNoneReader = enableRemoveNoneReader;
    }

    public Boolean getEnableDisableNoneReader() {
        return enableDisableNoneReader;
    }

    public void setEnableDisableNoneReader(Boolean enableDisableNoneReader) {
        this.enableDisableNoneReader = enableDisableNoneReader;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
