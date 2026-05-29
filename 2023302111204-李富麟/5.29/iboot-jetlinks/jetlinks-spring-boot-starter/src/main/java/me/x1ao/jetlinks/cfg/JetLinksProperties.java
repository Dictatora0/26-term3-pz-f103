package me.x1ao.jetlinks.cfg;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jetlinks")
public class JetLinksProperties {
    private String host;
    private String port;
    private String token;
    private String productId;
    private String deviceId;

    private boolean autoConnect = true;

}
