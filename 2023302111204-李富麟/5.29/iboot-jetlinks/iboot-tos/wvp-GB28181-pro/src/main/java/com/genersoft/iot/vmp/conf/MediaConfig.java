package com.genersoft.iot.vmp.conf;

import com.genersoft.iot.vmp.media.zlm.dto.MediaServerItem;
import com.genersoft.iot.vmp.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.util.ObjectUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

@Order(0)
@ConfigurationProperties(prefix = "media")
public class MediaConfig{

    private final static Logger logger = LoggerFactory.getLogger(MediaConfig.class);

    // 修改必须配置，不再支持自动获取
    private String id;

    /**
     * 流媒体ip
     */
    private String ip;

    /**
     * 流媒体回调ip
     */
    private String hookIp;

    /**
     * 回调地址前缀
     */
    private String hookPrefix = "gbs/zlm/hook";

    @Value("${sip.ip}")
    private String sipIp;

    @Value("${sip.domain}")
    private String sipDomain;

    private String sdpIp;

    private String streamIp;

    private Integer httpPort;

    private Integer httpSSlPort = 0;

    private Integer rtmpPort = 0;

    private Integer rtmpSSlPort = 0;

    @Value("${media.rtp-proxy-port:0}")
    private Integer rtpProxyPort = 0;

    @Value("${media.rtsp-port:0}")
    private Integer rtspPort = 0;

    @Value("${media.rtsp-ssl-port:0}")
    private Integer rtspSSLPort = 0;

    /**
     * 自动配置
     */
    private boolean autoConfig = true;

    /**
     * zlm服务器的 secret
     */
    private String secret;

    @Value("${media.rtp.enable}")
    private boolean rtpEnable;

    @Value("${media.rtp.port-range}")
    private String rtpPortRange;

    @Value("${media.rtp.send-port-range}")
    private String rtpSendPortRange;

    @Value("${media.record-assist-port:0}")
    private Integer recordAssistPort = 0;

    public String getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public String getHookIp() {
        if (ObjectUtils.isEmpty(hookIp)){
            return sipIp.split(",")[0];
        }else {
            return hookIp;
        }

    }

    public String getSipIp() {
        if (sipIp == null) {
            return this.ip;
        }else {
            return sipIp;
        }
    }

    public int getHttpPort() {
        return httpPort;
    }

    public int getHttpSSlPort() {
        return httpSSlPort;
    }

    public int getRtmpPort() {
        return rtmpPort;
    }

    public int getRtmpSSlPort() {
        return rtmpSSlPort;
    }

    public int getRtpProxyPort() {
        if (rtpProxyPort == null) {
            return 0;
        }else {
            return rtpProxyPort;
        }

    }

    public int getRtspPort() {
        return rtspPort;
    }

    public int getRtspSSLPort() {
        return rtspSSLPort;
    }

    public boolean isAutoConfig() {
        return autoConfig;
    }

    public String getSecret() {
        return secret;
    }

    public boolean isRtpEnable() {
        return rtpEnable;
    }

    public String getRtpPortRange() {
        return rtpPortRange;
    }

    public int getRecordAssistPort() {
        return recordAssistPort;
    }

    public String getSdpIp() {
        if (ObjectUtils.isEmpty(sdpIp)){
            return ip;
        }else {
            if (isValidIPAddress(sdpIp)) {
                return sdpIp;
            }else {
                // 按照域名解析
                String hostAddress = null;
                try {
                    hostAddress = InetAddress.getByName(sdpIp).getHostAddress();
                } catch (UnknownHostException e) {
                    logger.error("[获取SDP IP]: 域名解析失败");
                }
                return hostAddress;
            }
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setHookIp(String hookIp) {
        this.hookIp = hookIp;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getStreamIp() {
        if (ObjectUtils.isEmpty(streamIp)){
            return ip;
        }else {
            return streamIp;
        }
    }

    public String getSipDomain() {
        return sipDomain;
    }

    public MediaServerItem getMediaSerItem(){
        MediaServerItem mediaServerItem = new MediaServerItem();
        mediaServerItem.setId(id);
        mediaServerItem.setIp(ip);
        mediaServerItem.setDefaultServer(true);
        mediaServerItem.setHookIp(getHookIp());
        mediaServerItem.setSdpIp(getSdpIp());
        mediaServerItem.setStreamIp(getStreamIp());
        mediaServerItem.setHttpPort(httpPort);
        mediaServerItem.setHttpSSlPort(httpSSlPort);
        mediaServerItem.setRtmpPort(rtmpPort);
        mediaServerItem.setRtmpSSlPort(rtmpSSlPort);
        mediaServerItem.setRtpProxyPort(getRtpProxyPort());
        mediaServerItem.setRtspPort(rtspPort);
        mediaServerItem.setRtspSSLPort(rtspSSLPort);
        mediaServerItem.setAutoConfig(autoConfig);
        mediaServerItem.setSecret(secret);
        mediaServerItem.setRtpEnable(rtpEnable);
        mediaServerItem.setRtpPortRange(rtpPortRange);
        mediaServerItem.setSendRtpPortRange(rtpSendPortRange);
        mediaServerItem.setRecordAssistPort(recordAssistPort);
        mediaServerItem.setHookAliveInterval(30.00f);

        mediaServerItem.setCreateTime(DateUtil.getNow());
        mediaServerItem.setUpdateTime(DateUtil.getNow());

        return mediaServerItem;
    }

    public String getRtpSendPortRange() {
        return rtpSendPortRange;
    }

    public void setRtpSendPortRange(String rtpSendPortRange) {
        this.rtpSendPortRange = rtpSendPortRange;
    }

    private boolean isValidIPAddress(String ipAddress) {
        if ((ipAddress != null) && (!ipAddress.isEmpty())) {
            return Pattern.matches("^([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$", ipAddress);
        }
        return false;
    }

    public void setSipIp(String sipIp) {
        this.sipIp = sipIp;
    }

    public void setSipDomain(String sipDomain) {
        this.sipDomain = sipDomain;
    }

    public void setSdpIp(String sdpIp) {
        this.sdpIp = sdpIp;
    }

    public void setStreamIp(String streamIp) {
        this.streamIp = streamIp;
    }

    public void setHttpSSlPort(Integer httpSSlPort) {
        this.httpSSlPort = httpSSlPort;
    }

    public void setRtmpPort(Integer rtmpPort) {
        this.rtmpPort = rtmpPort;
    }

    public void setRtmpSSlPort(Integer rtmpSSlPort) {
        this.rtmpSSlPort = rtmpSSlPort;
    }

    public void setRtpProxyPort(Integer rtpProxyPort) {
        this.rtpProxyPort = rtpProxyPort;
    }

    public void setRtspPort(Integer rtspPort) {
        this.rtspPort = rtspPort;
    }

    public void setRtspSSLPort(Integer rtspSSLPort) {
        this.rtspSSLPort = rtspSSLPort;
    }

    public void setAutoConfig(boolean autoConfig) {
        this.autoConfig = autoConfig;
    }

    public void setRtpEnable(boolean rtpEnable) {
        this.rtpEnable = rtpEnable;
    }

    public void setRtpPortRange(String rtpPortRange) {
        this.rtpPortRange = rtpPortRange;
    }

    public void setRecordAssistPort(Integer recordAssistPort) {
        this.recordAssistPort = recordAssistPort;
    }

    public String getHookPrefix() {
        return hookPrefix;
    }

    public void setHookPrefix(String hookPrefix) {
        this.hookPrefix = hookPrefix;
    }
}
