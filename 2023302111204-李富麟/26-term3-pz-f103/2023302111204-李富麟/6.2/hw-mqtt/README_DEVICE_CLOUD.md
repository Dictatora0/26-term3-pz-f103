# README_DEVICE_CLOUD

## 1. 项目目标

本工程用于 STM32 开发板通过 ESP8266 AT 固件接入 MQTT Broker，周期上报温度和光照数据，并订阅云端 LED 控制命令。

- 仅采集温度和光照。
- 不采集湿度。
- 支持本地验证模式和完整上云模式。

## 2. 当前工程路径

- 当前工程：`D:\Proj\6.2\hw-mqtt`
- 备份目录：`D:\Proj\6.2\hw-mqtt_backup_20260602_155957`

## 3. 扫描过的父文件夹

已扫描：

- `D:\Proj\5.21`
- `D:\Proj\5.26`
- `D:\Proj\5.27`
- `D:\Proj\5.29`
- `D:\Proj\6.2`

## 4. 找到的参考工程

- `D:\Proj\5.21\21-内部温度传感器实验`
  - 内部温度传感器 ADC1/通道 16 读取。
- `D:\Proj\5.21\22-光敏传感器实验`
  - 光敏传感器实验目录，验证存在独立光照实验。
- `D:\Proj\5.21\温度光照仪表盘`
  - 温度与光照联合采样，确认光照走 `PF8 / ADC3_CH6`。
- `D:\Proj\5.26\温度-蜂鸣器-LED实验`
  - `PZ103 TEMP CONTROL DEMO`，提供 TFT LCD 显示逻辑与 LED 参考。
- `D:\Proj\5.27\ubuntu_mqtt_f103`
  - ESP8266 AT + MQTT 参考实现，包含 `AT+MQTTUSERCFG / AT+MQTTCONN / AT+MQTTSUB / AT+MQTTPUBRAW`。

## 5. 实际识别出的开发板和 MCU

- 开发板型号：
  - `PZ103`。
  - 依据：`5.26\温度-蜂鸣器-LED实验\User\main.c` 中 LCD 文案为 `PZ103 TEMP CONTROL DEMO`，并且其 LED/LCD/ADC/USART 引脚映射与当前工程一致。
- MCU：
  - `STM32F103ZE`
  - 依据：当前 `Template.uvprojx` 的 `<Device>STM32F103ZE</Device>`。
- 固件库：
  - `STM32F10x StdPeriph`
  - 依据：`USE_STDPERIPH_DRIVER`、`STM32F10X_HD`、`Libraries/STM32F10x_StdPeriph_Driver`。

## 6. 串口与 ESP8266 识别结果

- ESP8266 串口：
  - `USART3`
  - 引脚：`PB10(TX)` / `PB11(RX)`
  - 依据：`APP/esp8266/wifi_config.c`
- ESP8266 控制引脚：
  - `PA4`：CH/EN
  - `PA15`：RST
  - 依据：`APP/esp8266/wifi_config.c`
- ESP8266 波特率：
  - 静态工程无法唯一确认固定值。
  - 依据不足原因：
    - 当前 6.2 起始工程曾把 USART3 配成 `115200`
    - `5.27` 参考工程把 USART3 初始值设为 `9600`
  - 当前交付程序采用运行时自动探测：
    - `9600 -> 115200 -> 74880 -> 57600`
  - 串口日志会打印实际探测到的值。
- 调试串口：
  - `USART1`
  - 引脚：`PA9(TX)` / `PA10(RX)`
  - 当前交付波特率：`115200`
  - 依据：历史 `5.21/5.26/5.27` 工程普遍使用 `115200`，当前交付代码统一为 `DEBUG_USART_BAUD=115200`。

## 7. 传感器、LED、LCD 识别结果

- 温度传感器类型：
  - `STM32F103 内部温度传感器`
  - 配置：`ADC1 / ADC_Channel_16`
  - 依据：`ADC_TempSensorVrefintCmd(ENABLE)` 与 `Get_ADC_Temp_Value(ADC_Channel_16, ...)`
- 光照传感器类型：
  - 可确认是模拟量光敏传感器/光敏电阻模块。
  - 无法从现有工程静态确认具体模块商品型号。
  - 配置：`PF8 / ADC3_CH6`
  - 依据：`5.21\温度光照仪表盘\APP\adc_temp\adc_temp.c`
- LED：
  - `LED1 -> PB5`
  - `LED2 -> PE5`
  - 当前云控使用 `LED1`
  - 依据：`APP/led/led.h`
- LCD：
  - 已存在可复用逻辑。
  - 当前工程已集成 `APP/tftlcd/tftlcd.c`
  - 底层依赖 `FSMC`
  - 可通过 `User/config/cloud_config.h` 中 `LCD_ENABLE` 开关启停。

## 8. ESP8266 与 MQTT 实现判断

- 当前工程存在可用 ESP8266 AT 驱动。
  - 复用基础：`APP/esp8266/wifi_config.c`
  - 复用增强逻辑来源：`5.27\ubuntu_mqtt_f103\APP\esp8266`
- 当前工程存在可用 MQTT 实现。
  - 不是 MCU 侧手工组帧 MQTT 3.1.1。
  - 实际复用的是 `ESP-AT` 固件内置 MQTT 指令：
    - `AT+MQTTUSERCFG`
    - `AT+MQTTCONNCFG`
    - `AT+MQTTCONN`
    - `AT+MQTTSUB`
    - `AT+MQTTPUBRAW`
- KeepAlive 由 ESP8266 的 `AT+MQTTCONNCFG` 负责。

## 9. 新增和修改的文件

### 新增

- `User/config/cloud_config.h`
- `User/bsp_sensor.h`
- `User/bsp_sensor.c`
- `User/cloud_service.h`
- `User/cloud_service.c`
- `README_DEVICE_CLOUD.md`

### 复制并改造

- `APP/adc_temp/adc_temp.h`
- `APP/adc_temp/adc_temp.c`
- `APP/tftlcd/tftlcd.h`
- `APP/tftlcd/tftlcd.c`
- `APP/tftlcd/font.h`
- `APP/esp8266/wifi_config.h`
- `APP/esp8266/wifi_config.c`
- `APP/esp8266/wifi_function.h`
- `APP/esp8266/wifi_function.c`

### 修改

- `User/main.c`
- `Public/SysTick.h`
- `Public/SysTick.c`
- `Public/usart.c`
- `Template.uvprojx`

## 10. cloud_config.h 中需要替换的参数

文件：`User/config/cloud_config.h`

至少需要替换：

- `WIFI_SSID`
- `WIFI_PASSWORD`
- `MQTT_BROKER_HOST`
- `MQTT_USERNAME`
- `MQTT_PASSWORD`

按需调整：

- `CLOUD_ENABLE`
- `LCD_ENABLE`
- `MQTT_BROKER_PORT`
- `MQTT_DEVICE_ID`
- `MQTT_TOPIC_TELEMETRY`
- `MQTT_TOPIC_STATUS`
- `MQTT_TOPIC_COMMAND`
- `MQTT_TOPIC_REPLY`

## 11. MQTT Topic

- 遥测发布：
  - `devices/device_001/telemetry`
- 在线状态：
  - `devices/device_001/status`
- 控制订阅：
  - `devices/device_001/command`
- 命令回复：
  - `devices/device_001/command_reply`

## 12. JSON Payload 格式

### 遥测

```json
{
  "device_id": "device_001",
  "temperature": 25.6,
  "light": 378,
  "uptime": 120,
  "status": "online"
}
```

说明：

- `temperature` 保留 1 位小数。
- `light` 为 `PF8/ADC3_CH6` 的原始 ADC 整数值。
- 如果传感器读取失败：
  - 程序记录错误日志；
  - JSON 对应字段输出 `null`；
  - 不会崩溃，不会生成伪数据。

### 在线状态

```json
{
  "device_id": "device_001",
  "status": "online"
}
```

### LED 控制命令

```json
{
  "action": "led",
  "value": "on"
}
```

```json
{
  "action": "led",
  "value": "off"
}
```

### 命令响应

```json
{
  "device_id": "device_001",
  "action": "led",
  "value": "on",
  "result": "success"
}
```

```json
{
  "device_id": "device_001",
  "result": "unsupported_command"
}
```

## 13. 正常启动日志示例

```text
[BOOT] System initialized
[BOOT] MCU: STM32F103ZE
[BOOT] Debug UART: USART1 @ 115200
[BOOT] Mode: cloud
[SENSOR] Temperature driver ready
[SENSOR] Light driver ready
[LCD] Display ready
[SENSOR] temperature=25.6, light=378
[WIFI] ESP8266 driver ready (USART3)
[WIFI] ESP8266 detected
[WIFI] ESP8266 baud=9600
[WIFI] Joining AP...
[WIFI] Connected
[MQTT] Connecting to broker...
[MQTT] Connected
[MQTT] Subscribed: devices/device_001/command
[MQTT] Online status published
{"device_id":"device_001","temperature":25.6,"light":378,"uptime":5,"status":"online"}
[MQTT] Publish telemetry success
[MQTT] Command received: led=on
```

## 14. 本地验证模式使用方法

1. 打开 `User/config/cloud_config.h`
2. 将 `#define CLOUD_ENABLE 1` 改为 `0`
3. 编译下载
4. 打开串口工具，连接 `USART1 @ 115200`
5. 观察：
   - 每 5 秒打印一次 JSON
   - LCD 显示温度、光照、运行时间
   - 不会初始化 Wi-Fi，不会连接 MQTT

## 15. 上云模式使用方法

1. 打开 `User/config/cloud_config.h`
2. 保持 `#define CLOUD_ENABLE 1`
3. 填写：
   - `WIFI_SSID`
   - `WIFI_PASSWORD`
   - `MQTT_BROKER_HOST`
   - `MQTT_USERNAME`
   - `MQTT_PASSWORD`
4. 编译下载
5. 上电后查看调试串口日志
6. 确认出现：
   - `[WIFI] Connected`
   - `[MQTT] Connected`
   - `[MQTT] Subscribed: devices/device_001/command`
7. 用 MQTT 客户端向 `devices/device_001/command` 下发 LED JSON 命令

说明：

- 如果 `WIFI_SSID`、`WIFI_PASSWORD`、`MQTT_BROKER_HOST`、`MQTT_USERNAME`、`MQTT_PASSWORD` 仍保留 `YOUR_...` 占位值，程序会输出配置未完成日志，并自动停留在仅传感器本地输出模式，不会反复尝试上云。

## 16. Keil 打开、编译和烧录步骤

1. 打开 `D:\Proj\6.2\hw-mqtt\Template.uvprojx`
2. 确认 Target 仍为 `STM32F103ZE`
3. 检查工程中已包含：
   - `User/main.c`
   - `User/bsp_sensor.c`
   - `User/cloud_service.c`
   - `APP/adc_temp/adc_temp.c`
   - `APP/tftlcd/tftlcd.c`
   - `APP/esp8266/wifi_config.c`
   - `APP/esp8266/wifi_function.c`
4. 进入 `Options for Target -> C/C++`
5. 确认 Include Path 中包含：
   - `.\User`
   - `.\User\config`
   - `.\APP\adc_temp`
   - `.\APP\tftlcd`
   - `.\APP\esp8266`
6. 点击 `Rebuild`
7. 连接下载器
8. 点击 `Load`
9. 复位开发板并打开调试串口

## 17. 串口调试工具配置

- 调试串口：`USART1`
- 波特率：`115200`
- 数据位：`8`
- 校验位：`None`
- 停止位：`1`
- 流控：`None`

## 18. 烧录后首轮联调建议

按顺序观察：

1. 先看是否有 `[BOOT]`、`[SENSOR]` 日志
2. 再看 `[WIFI] ESP8266 detected`
3. 再看 `[WIFI] Joining AP...` / `[WIFI] Connected`
4. 再看 `[MQTT] Connected`
5. 再看 5 秒周期 JSON 与 `[MQTT] Publish telemetry success`
6. 最后测试 LED 命令订阅与回包

## 19. 常见故障排查

### ESP8266 无 AT 响应

- 检查 `PA4` 是否把 CH/EN 拉高
- 检查 `PA15` 复位脚是否正常拉高
- 检查 `PB10/PB11` 是否接反
- 检查模块是否真的是 `ESP-AT` 固件
- 当前程序会自动探测 `9600/115200/74880/57600`

### USART 收不到数据

- 检查 `USART3` 线序与共地
- 检查 `USART1` 串口工具是否接在调试串口而不是 ESP8266 串口
- 检查 `PB10/PB11`、`PA9/PA10` 是否混接

### Wi-Fi 连接失败

- 检查 `WIFI_SSID`
- 检查 `WIFI_PASSWORD`
- 确认热点为 2.4GHz
- 检查路由器是否允许新设备接入

### TCP / MQTT 连接超时

- 检查 `MQTT_BROKER_HOST`
- 检查云主机安全组是否放行 `1883`
- 检查本地网络是否能访问 Broker

### MQTT CONNACK 失败 / 用户名密码错误

- 检查 `MQTT_USERNAME`
- 检查 `MQTT_PASSWORD`
- 检查 Broker 是否允许该客户端 ID 登录

### Broker IP 不可达

- 优先使用 ECS 公网 IP 或有效域名
- 不要填写 `127.0.0.1`
- 不要填写只在服务器内部可见的私网地址

### 发布数据失败

- 看是否先出现 `[MQTT] Connected`
- 检查 Topic 权限
- 检查 Broker 是否限制 payload 长度

### 收不到 LED 控制指令

- 确认下发 Topic 是否为 `devices/device_001/command`
- 确认 payload 是否为 JSON：
  - `{"action":"led","value":"on"}`
  - `{"action":"led","value":"off"}`

### LED 无法控制

- 当前云控默认控制 `LED1 -> PB5`
- 该 LED 为低电平点亮
- 检查板上 LED1 是否工作正常

### 温度读取异常

- 当前温度来自 MCU 内部温度传感器
- 内部温度反映芯片温度，不是外界空气温度
- 若出现持续 `Temperature read failed`，优先排查 ADC 初始化

### 光照读取异常

- 当前光照来自 `PF8 / ADC3_CH6`
- 如果光敏模块未接好或通道悬空，数值会异常
- 若持续 `Light read failed`，排查 ADC3 与 PF8

### Keil 编译报错

- 先确认 `Template.uvprojx` 已重新加载
- 确认 `.\User\config` 已加入 Include Path
- 确认工程已包含新增源文件

### 烧录失败

- 检查下载器与目标板供电
- 检查芯片型号是否仍为 `STM32F103ZE`
- 若 LCD/FSMC 总线外接模块异常，不影响下载，但可能影响运行后显示

## 20. 交付代码的已知局限

- ESP8266 连接流程仍是阻塞式 AT 命令，单次 `Join AP` 或 `MQTT Connect` 最长可阻塞数秒。
- 为满足“不猜测固定波特率”的约束，当前采用运行时自动探测 ESP8266 波特率。
- 当 `cloud_config.h` 仍保留默认占位参数时，程序会主动跳过 Wi-Fi / MQTT 连接流程，仅保留本地传感器输出。
- 本机未发现 `UV4.exe`/`armcc.exe`，因此未能在本机完成真实 Keil 命令行编译。

## 21. 建议的首轮回传日志

首次真实硬件测试后，建议回传以下日志片段：

1. 上电后从 `[BOOT]` 到 `[SENSOR]` 的完整启动日志
2. `[WIFI] ESP8266 baud=...`
3. `[WIFI] Joining AP...` 之后的全部日志
4. `[MQTT] Connecting to broker...` 之后的全部日志
5. 第一条遥测 JSON
6. 一次 LED 控制命令前后的完整日志
