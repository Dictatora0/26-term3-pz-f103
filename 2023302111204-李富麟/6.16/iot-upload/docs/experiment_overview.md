# IoT 实验说明文档

## 1. 实验主要内容

本实验围绕“嵌入式设备数据上云但设备端不直接耦合平台协议”展开，完成了两类开发板接入本地物联网平台的联调：

1. `PZ STM32F103` 开发板  
   通过 UART 周期上报温度、光照、LED 状态、蜂鸣器状态。
2. `Huawei Hi3861` 小车开发板  
   通过串口周期上报运行状态、方向、运动状态、速度、偏航角、陀螺仪 Z 轴角速度。
3. `Windows Python MQTT Gateway`  
   作为统一协议适配层，负责读取串口、解析设备数据、发布 Home Assistant MQTT topic、发布 ThingsBoard telemetry。
4. `Home Assistant`  
   负责本地可视化展示实体状态。
5. `ThingsBoard`  
   负责设备建模、遥测接收与平台侧展示。

## 2. 实验实现方式

### 2.1 总体架构

```text
F103 UART ------------------+
                            |
Hi3861 Car UART --------+   |
                        |   v
                        +-> Windows Python MQTT Gateway
                              |-- publish Home Assistant MQTT topics
                              +-- publish ThingsBoard telemetry
```

本实验采用最小耦合设计：

- F103 不直接接入 Home Assistant。
- F103 不直接接入 ThingsBoard。
- Hi3861 不直接实现 Home Assistant 协议。
- Hi3861 不在固件中保存 MQTT topic、Wi-Fi 配置或 ThingsBoard Token。
- 平台协议集中在 Windows Python 网关处理。

这种方式把“设备采集能力”和“平台接入能力”彻底分层，便于调试、扩展和复用。

### 2.2 F103 侧实现

F103 只做采样和串口文本上报，典型格式如下：

```text
TEMP:26.5,LIGHT:73,LED:OFF,BUZZER:OFF
```

Python 网关对该格式进行解析，并映射到：

- `pz103/f103_01/temperature`
- `pz103/f103_01/light`
- `pz103/f103_01/led/state`
- `pz103/f103_01/buzzer/state`

同时发布到 ThingsBoard 的：

- `v1/devices/me/telemetry`

### 2.3 Hi3861 小车侧实现

Hi3861 小车采用 JSON 串口上报，当前稳定联调格式如下：

```json
{"device_id":"hi3861_car_01","type":"car","status":"RUNNING","direction":"FORWARD","motion_state":"CRUISE","speed":50,"yaw_deg":-145.6,"gyro_z_dps":0.00}
```

当前已稳定接入的字段：

- `status`
- `direction`
- `motion_state`
- `speed`
- `yaw_deg`
- `gyro_z_dps`

其中小车串口上报周期已修正为真实约 `1s` 一次，能够持续稳定输出。

### 2.4 Python 网关实现

网关主程序位于：

- `D:\pz-f103-python-mqtt-gateway\gateway.py`

核心实现包括：

1. 使用 `pyserial` 读取 Windows `COM` 串口。
2. 同时兼容两种设备协议：
   - F103 文本协议 `TEMP:...`
   - Hi3861 JSON 协议 `{...}`
3. 使用 `paho-mqtt` 分别连接：
   - Home Assistant Mosquitto `127.0.0.1:1883`
   - ThingsBoard MQTT `127.0.0.1:1884`
4. 使用 `.env` 管理真实运行配置。
5. 使用 `logging` 输出完整联调日志。
6. 兼容新版 `paho-mqtt CallbackAPIVersion.VERSION2`，同时保留 fallback。

### 2.5 Home Assistant 接入实现

Home Assistant 本地源码目录：

- `D:\core-dev`

运行方式：

- `WSL` 内运行 Home Assistant Core

本实验通过 MQTT 将设备状态映射为 HA 实体。  
其中 Hi3861 小车不仅提供了静态 YAML 配置，还通过 MQTT Discovery 自动创建设备实体。

实验联调后，HA 中已能看到小车设备及以下实体：

- 状态
- 方向
- 运动状态
- 速度
- 偏航角
- 陀螺仪 Z 轴角速度

### 2.6 ThingsBoard 接入实现

ThingsBoard 本地源码目录：

- `D:\thingsboard-master`

运行方式：

- Windows 本地运行

本实验为不同设备分配独立 Access Token：

- `PZ F103 Board`
- `Huawei Hi3861 Car`

每台设备都通过自己的 token 向：

- `v1/devices/me/telemetry`

发送遥测数据，避免不同设备的 telemetry 混写。

## 3. 实际联调结果

本实验已完成真实环境联调，不是停留在脚本模板阶段。

### 3.1 串口链路

- `COM3` 已确认可稳定读取 Hi3861 小车数据。
- 小车能持续输出 JSON 遥测。
- F103 与 Hi3861 的解析逻辑已在同一网关中兼容。

### 3.2 Home Assistant 链路

- HA 已成功启动并可从 Windows 访问 `8123`。
- HA MQTT 已连接到本机 Mosquitto。
- 小车设备与实体已实际出现在 HA 实体注册表中。

### 3.3 ThingsBoard 链路

- TB `8080` Web 服务正常。
- TB MQTT `1884` 端口正常。
- 已定位并修复“Hi3861 数据误写到 F103 设备”的问题。
- 已为小车新建独立设备 `Huawei Hi3861 Car`。
- 已将小车 telemetry 正确写入该设备的 `Latest telemetry`。

## 4. 本实验的亮点

### 4.1 平台协议与设备固件彻底解耦

设备端只输出串口数据，不感知 Home Assistant、ThingsBoard、MQTT、Wi-Fi、Token。  
这使得固件复杂度明显降低，平台切换成本也更低。

### 4.2 一个网关同时适配多设备、多协议

本实验不是单一设备直连，而是通过一个 Python 网关同时兼容：

- F103 文本协议
- Hi3861 JSON 协议

这体现了统一协议适配层的工程设计能力。

### 4.3 本地双平台同步接入

同一份串口遥测数据被同步转发到：

- Home Assistant，本地展示和实体化
- ThingsBoard，平台侧设备遥测管理

体现了“同源数据，多平台分发”的设计亮点。

### 4.4 完成了真实链路问题定位与修复

实验过程中定位并解决了多个真实问题：

- Hi3861 telemetry 周期表面 1 秒、实际 10 秒的问题
- Home Assistant 在 WSL 场景下的访问与启动问题
- ThingsBoard token 误指向 F103 设备，导致小车数据不可见的问题
- F103 设备下误写 telemetry 的清理问题

说明本实验不仅完成了功能开发，也完成了真实环境联调和问题闭环。

### 4.5 具备后续扩展基础

当前架构已经为后续功能留出明确扩展路径：

- Home Assistant 反向控制
- ThingsBoard RPC
- Python 网关订阅控制 topic 后串口下发命令
- 设备断线重连
- 多串口多设备并行接入

## 5. 当前完成度与后续可扩展项

当前已稳定完成：

- F103 数据上报
- Hi3861 小车数据上报
- Python 多设备网关
- Home Assistant 实体接入
- ThingsBoard 遥测接入

当前保留项：

- `distance_cm` 尚未稳定进入 Hi3861 实时串口数据
- 小车远程控制尚未实现
- ThingsBoard RPC 尚未实现

## 6. 结论

本实验完成了“串口设备 -> Windows 网关 -> 本地物联网平台”的完整工程闭环。  
其核心价值不在于单点上传，而在于通过统一协议适配层，实现了多设备、双平台、可扩展、低耦合的本地 IoT 接入方案。

从工程角度看，本实验已经具备以下特征：

- 架构清晰
- 联调真实
- 问题闭环完整
- 可扩展性强
- 适合继续向控制闭环和平台化方向深化
