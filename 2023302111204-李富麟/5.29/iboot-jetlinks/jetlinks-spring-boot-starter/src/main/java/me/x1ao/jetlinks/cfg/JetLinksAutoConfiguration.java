package me.x1ao.jetlinks.cfg;

import me.x1ao.jetlinks.service.JetLinksService;
import me.x1ao.jetlinks.service.JetLinksWebSocketClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnClass({JetLinksService.class, JetLinksWebSocketClient.class})
@EnableConfigurationProperties(JetLinksProperties.class)
public class JetLinksAutoConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public JetLinksService jetLinksService(JetLinksProperties properties, RestTemplate restTemplate, JetLinksWebSocketClient webSocketClient) {
        return new JetLinksService(properties, restTemplate, webSocketClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public JetLinksWebSocketClient jetLinksWebSocketClient(JetLinksProperties properties, ApplicationEventPublisher eventPublisher) throws Exception {
        JetLinksWebSocketClient client = new JetLinksWebSocketClient(properties, eventPublisher);
        if (properties.isAutoConnect()) {
            client.connect();
        }
        return client;
    }
}
