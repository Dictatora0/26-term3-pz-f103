package me.x1ao.jetlinks.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.x1ao.jetlinks.cfg.JetLinksProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JetLinksService {

    private final JetLinksProperties jetLinksProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl;
    private final JetLinksWebSocketClient webSocketClient;

    public JetLinksService(JetLinksProperties properties, RestTemplate restTemplate, JetLinksWebSocketClient webSocketClient) {
        this.jetLinksProperties = properties;
        this.restTemplate = restTemplate;
        this.baseUrl = "http://" + properties.getHost() + ":" + properties.getPort();
        this.webSocketClient = webSocketClient;
    }

    public void destroyDeviceOffline() {
        log.info("开始执行 JetLinks 设备下线任务");

        try {
            ApiResponse response = deviceOffline();
            if (response.isSuccess()) {
                log.info("设备下线成功");
            } else {
                log.error("设备下线失败，原因: {}", response.getMessage());
            }
        } catch (Exception e) {
            log.error("设备下线异常: {}", e.getMessage(), e);
        }
    }

    @PostConstruct
    public void scheduleDeviceOnline() {
        if (!shouldAutoConnect()) {
            log.info("跳过 JetLinks 自动上线：autoConnect={}, productIdPresent={}, deviceIdPresent={}, tokenPresent={}",
                    jetLinksProperties.isAutoConnect(),
                    StringUtils.hasText(jetLinksProperties.getProductId()),
                    StringUtils.hasText(jetLinksProperties.getDeviceId()),
                    StringUtils.hasText(jetLinksProperties.getToken()));
            return;
        }

        log.info("开始执行 JetLinks 设备上线任务");

        try {
            ApiResponse response = deviceOnline();
            if (response.isSuccess()) {
                log.info("设备上线成功");
            } else {
                log.error("设备上线失败，原因: {}", response.getMessage());
            }
        } catch (Exception e) {
            log.error("设备上线异常: {}", e.getMessage(), e);
        }
    }

    public ApiResponse deviceOnline() {
        return deviceOnline(jetLinksProperties.getProductId(), jetLinksProperties.getDeviceId());
    }

    public ApiResponse deviceOffline() {
        return deviceOffline(jetLinksProperties.getProductId(), jetLinksProperties.getDeviceId());
    }

    private ApiResponse deviceOffline(String productId, String deviceId) {
        if (!hasDeviceIdentity(productId, deviceId)) {
            return ApiResponse.fail("设备下线失败: productId 或 deviceId 未配置");
        }
        if (!StringUtils.hasText(jetLinksProperties.getToken())) {
            return ApiResponse.fail("设备下线失败: token 未配置");
        }

        String endpoint = String.format("/%s/%s/offline", productId, deviceId);
        try {
            return sendRequest(endpoint, HttpMethod.POST, new HashMap<>(1));
        } catch (Exception e) {
            return ApiResponse.fail("设备下线失败: " + e.getMessage());
        }
    }

    public ApiResponse deviceOnline(String productId, String deviceId) {
        if (!hasDeviceIdentity(productId, deviceId)) {
            return ApiResponse.fail("设备上线失败: productId 或 deviceId 未配置");
        }
        if (!StringUtils.hasText(jetLinksProperties.getToken())) {
            return ApiResponse.fail("设备上线失败: token 未配置");
        }

        String endpoint = String.format("/%s/%s/online", productId, deviceId);
        try {
            return sendRequest(endpoint, HttpMethod.POST, new HashMap<>(1));
        } catch (Exception e) {
            return ApiResponse.fail("设备上线失败: " + e.getMessage());
        }
    }

    public ApiResponse reportDeviceProperties(String productId, String deviceId, Map<String, Object> properties) {
        if (!hasDeviceIdentity(productId, deviceId)) {
            return ApiResponse.fail("设备属性上报失败: productId 或 deviceId 未配置");
        }
        if (!StringUtils.hasText(jetLinksProperties.getToken())) {
            return ApiResponse.fail("设备属性上报失败: token 未配置");
        }

        String endpoint = String.format("/%s/%s/properties/report", productId, deviceId);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("deviceId", deviceId);
        requestBody.put("properties", properties);

        try {
            return sendRequest(endpoint, HttpMethod.POST, requestBody);
        } catch (Exception e) {
            return ApiResponse.fail("设备属性上报失败: " + e.getMessage());
        }
    }

    private ApiResponse sendRequest(String endpoint, HttpMethod method, Map<String, Object> requestBody) throws RestClientException {
        String requestUrl = baseUrl + endpoint;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jetLinksProperties.getToken());
        if (method == HttpMethod.POST && requestBody != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestUrl, method, entity, String.class);

        try {
            return objectMapper.readValue(responseEntity.getBody(), ApiResponse.class);
        } catch (Exception e) {
            return ApiResponse.fail("响应格式错误: " + e.getMessage());
        }
    }

    private boolean shouldAutoConnect() {
        return jetLinksProperties.isAutoConnect()
                && hasDeviceIdentity(jetLinksProperties.getProductId(), jetLinksProperties.getDeviceId())
                && StringUtils.hasText(jetLinksProperties.getToken());
    }

    private boolean hasDeviceIdentity(String productId, String deviceId) {
        return StringUtils.hasText(productId) && StringUtils.hasText(deviceId);
    }

    @Setter
    @Getter
    public static class ApiResponse {
        private boolean success;
        private String message;

        public static ApiResponse success() {
            ApiResponse response = new ApiResponse();
            response.success = true;
            response.message = "操作成功";
            return response;
        }

        public static ApiResponse fail(String message) {
            ApiResponse response = new ApiResponse();
            response.success = false;
            response.message = message;
            return response;
        }
    }

    public void sendWebSocketMessage(String message) throws IOException {
        webSocketClient.sendMessage(message);
    }

    public boolean isWebSocketConnected() {
        return webSocketClient.isConnected();
    }
}
