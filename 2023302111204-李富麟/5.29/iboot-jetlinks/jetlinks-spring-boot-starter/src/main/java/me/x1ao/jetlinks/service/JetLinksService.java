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
    private final ObjectMapper objectMapper = new ObjectMapper(); // 用于JSON解析

    // 添加 baseUrl 字段
    private final String baseUrl;

    private final JetLinksWebSocketClient webSocketClient;

    public JetLinksService(JetLinksProperties properties, RestTemplate restTemplate, JetLinksWebSocketClient webSocketClient) {
        this.jetLinksProperties = properties;
        this.restTemplate = restTemplate;
        // 动态初始化 BASE_URL
        this.baseUrl = "http://" + properties.getHost() + ":" + properties.getPort();
        // 来自 JetLinksProperties 的动态 URL
        this.webSocketClient = webSocketClient;
    }

    /**
     * 在Bean销毁之前调用此方法进行资源清理或设备下线操作
     */
    // @PreDestroy
    public void destroyDeviceOffline() {
        log.info("开始执行设备下线任务...");

        try {
            JetLinksService.ApiResponse response = deviceOffline(); // 假设deviceOffline是下线设备的方法

            if (response.isSuccess()) {
                log.info("设备下线成功！");
            } else {
                log.error("设备下线失败，原因: " + response.getMessage());
            }
        } catch (Exception e) {
            log.error("设备下线异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void scheduleDeviceOnline() {
        log.info("开始执行设备上线任务...");

        try {
            JetLinksService.ApiResponse response = deviceOnline();

            if (response.isSuccess()) {
                log.info("设备上线成功！");
            } else {
                log.error("设备上线失败，原因: " + response.getMessage());
            }
        } catch (Exception e) {
            log.error("设备上线异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 设备上线的方法
     *
     * @return ApiResponse 统一响应对象
     */
    public ApiResponse deviceOnline() {
        return deviceOnline(jetLinksProperties.getProductId(), jetLinksProperties.getDeviceId());
    }

    /**
     * 设备下线的方法
     *
     * @return ApiResponse 统一响应对象
     */
    public ApiResponse deviceOffline() {
        return deviceOffline(jetLinksProperties.getProductId(), jetLinksProperties.getDeviceId());
    }

    private ApiResponse deviceOffline(String productId, String deviceId) {
        String endpoint = String.format("/%s/%s/offline", productId, deviceId); // 假设下线接口路径是 /{productId}/{deviceId}/offline
        try {
            return sendRequest(endpoint, HttpMethod.POST, new HashMap<>(1));
        } catch (Exception e) {
            return ApiResponse.fail("设备下线失败: " + e.getMessage());
        }
    }

    /**
     * 设备上线的方法
     *
     * @param productId 产品ID
     * @param deviceId  设备ID
     * @return ApiResponse 统一响应对象
     */
    public ApiResponse deviceOnline(String productId, String deviceId) {
        String endpoint = String.format("/%s/%s/online", productId, deviceId);
        try {
            return sendRequest(endpoint, HttpMethod.POST, new HashMap<>(1));
        } catch (Exception e) {
            return ApiResponse.fail("设备上线失败: " + e.getMessage());
        }
    }

    /**
     * 设备属性上报方法
     *
     * @param productId  产品ID
     * @param deviceId   设备ID
     * @param properties 属性键值对，如 {"temp": 12.4}
     * @return ApiResponse 统一响应对象
     */
    public ApiResponse reportDeviceProperties(String productId, String deviceId, Map<String, Object> properties) {
        String endpoint = String.format("/%s/%s/properties/report", productId, deviceId);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("deviceId", deviceId);
        requestBody.put("properties", properties);

        try {
            return sendRequest(endpoint, HttpMethod.POST, requestBody);
        } catch (Exception e) {
            return ApiResponse.fail("设备上线失败: " + e.getMessage());
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

        // 解析响应为 ApiResponse 对象
        try {
            return objectMapper.readValue(responseEntity.getBody(), ApiResponse.class);
        } catch (Exception e) {
            return ApiResponse.fail("响应格式错误: " + e.getMessage());
        }
    }


    /**
     * 统一返回格式
     */
    @Setter
    @Getter
    public static class ApiResponse {
        // Getter and Setter
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

    /**
     * 发送通用 WebSocket 消息
     */
    public void sendWebSocketMessage(String message) throws IOException {
        webSocketClient.sendMessage(message);
    }

    /**
     * 获取 WebSocket 连接状态
     */
    public boolean isWebSocketConnected() {
        return webSocketClient.isConnected();
    }
}
