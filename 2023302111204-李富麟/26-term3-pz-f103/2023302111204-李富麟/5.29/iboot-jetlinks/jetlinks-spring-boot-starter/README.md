# 📦 JetLinks 服务模块使用说明
https://hanta.yuque.com/px7kg1/yfac2l/ceenesef5ifta03y
> 本模块用于设备通过 HTTP/WebSocket 与 [JetLinks](https://github.com/jetlinks/jetlinks-platform) 平台进行通信。以下为简化版接口说明和使用方式，不包含具体实现代码。

---

## ✅ 一、[JetLinksService](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksService.java)

### 🔧 方法说明

| 方法名 | 参数 | 描述 |
|--------|------|------|
| `deviceOnline(String productId, String deviceId)` | [productId](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksProperties.java#L11-L11): 产品ID<br>[deviceId](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksProperties.java#L12-L12): 设备ID | 请求设备上线 |
| `deviceOffline(String productId, String deviceId)` | [productId](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksProperties.java#L11-L11): 产品ID<br>[deviceId](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksProperties.java#L12-L12): 设备ID | 请求设备下线 |
| `reportDeviceProperties(String productId, String deviceId, Map<String, Object> properties)` | [productId](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksProperties.java#L11-L11): 产品ID<br>[deviceId](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksProperties.java#L12-L12): 设备ID<br>[properties](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksWebSocketClient.java#L21-L21): 属性键值对 | 上报设备属性信息 |
| [isWebSocketConnected()](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksService.java#L201-L203) | 无 | 判断当前 WebSocket 是否已连接 |
| [sendWebSocketMessage(String message)](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksService.java#L194-L196) | [message](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksService.java#L174-L174): 要发送的消息内容 | 发送自定义 WebSocket 消息 |
| [setOnMessageHandler(JetLinksWebSocketClient.MessageHandler handler)](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksWebSocketClient.java#L92-L94) | [handler](file://D:\muge\iteaj\iboot\iboot-tos\wvp-GB28181-pro\src\main\java\com\genersoft\iot\vmp\vmanager\bean\DeferredResultFilter.java#L4-L4): 消息回调接口 | 设置收到平台消息时的处理逻辑 |

---

## ✅ 二、[JetLinksWebSocketClient](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksWebSocketClient.java)

### 🔧 方法说明

| 方法名 | 参数 | 描述 |
|--------|------|------|
| [connect()](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksWebSocketClient.java#L29-L42) | 无 | 建立与平台的 WebSocket 长连接 |
| [disconnect()](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksWebSocketClient.java#L44-L54) | 无 | 断开当前 WebSocket 连接 |
| [isConnected()](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksWebSocketClient.java#L56-L58) | 无 | 判断是否已建立连接 |
| [sendMessage(String message)](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksWebSocketClient.java#L60-L64) | [message](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksService.java#L174-L174): 要发送的消息内容 | 发送 WebSocket 消息 |
| [setOnMessageHandler(MessageHandler handler)](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksWebSocketClient.java#L92-L94) | [handler](file://D:\muge\iteaj\iboot\iboot-tos\wvp-GB28181-pro\src\main\java\com\genersoft\iot\vmp\vmanager\bean\DeferredResultFilter.java#L4-L4): 消息回调接口 | 设置收到平台消息时的处理逻辑 |

### 🔄 回调接口：[MessageHandler](file://D:\muge\iteaj\iboot\jetlinks-spring-boot-starter\src\main\java\me\x1ao\jetlinks\JetLinksWebSocketClient.java#L86-L88)

```java
void onMessageReceived(String message);
```
当平台下发消息时会触发此回调。

---

## ✅ 三、使用示例

### 示例 1：设备上线/下线

```java 
@Autowired 
private JetLinksService jetLinksService;
// 设备上线 
jetLinksService.deviceOnline("product_001", "device_001");
// 设备下线 
jetLinksService.deviceOffline("product_001", "device_001");
```
### 示例 2：上报设备属性
```java 
Map<String, Object> props = new HashMap<>(); 
props.put("temperature", 25.5); 
props.put("humidity", 60);
jetLinksService.reportDeviceProperties("product_001", "device_001", props);
```
### 示例 3：发送 WebSocket 消息

```java 
if (jetLinksService.isWebSocketConnected()) { 
    jetLinksService.sendWebSocketMessage("{"messageType":"CUSTOM","content":"Hello"}"); 
}
```
### 示例 4：监听平台下发消息

```java 
jetLinksService.setOnMessageHandler(message -> { 
    System.out.println("收到平台消息: " + message); 
    // 在这里解析并执行业务逻辑 
});
```

---

## 📌 总结

| 功能 | 支持情况 |
|------|----------|
| HTTP 接口 | ✅ 支持设备上线/下线、属性上报 |
| WebSocket 通信 | ✅ 支持双向通信 |
| 消息回调机制 | ✅ 支持平台指令监听 |
| 日志输出 | ✅ 使用 SLF4J 输出结构化日志 |

