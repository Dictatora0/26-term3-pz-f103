# PZ F103 + Hi3861 Python MQTT Gateway

## 1. 项目目标

本工程用于把两个串口设备接入本地 Home Assistant 和 ThingsBoard：

1. `PZ STM32F103` 开发板
2. `Huawei Hi3861 Car` 小车开发板

两块板都只负责输出串口状态数据，不直接接入 Home Assistant，不直接接入 ThingsBoard，也不在固件中保存 MQTT topic、Wi-Fi 参数或 ThingsBoard Access Token。

当前环境路径：

- `D:\core-dev`
- `D:\thingsboard-master`
- `D:\Proj\6.16\iot-upload\pz-f103-python-mqtt-gateway`

## 2. 系统架构

```text
PZ STM32F103 UART ----\
                       \
                        -> Windows Python Gateway -> Home Assistant MQTT topics
                       /
Hi3861 Car UART ------/                            -> ThingsBoard telemetry
```

职责边界：

- `F103`：传感器采集、LED/蜂鸣器状态输出、UART 文本协议
- `Hi3861 Car`：小车运行状态、方向、速度、距离输出、UART 文本协议
- `Python Gateway`：唯一协议适配层，负责串口解析、MQTT 发布、日志、重连
- `Home Assistant`：消费 MQTT state topic，展示实体
- `ThingsBoard`：按设备 Access Token 接收 telemetry

## 3. 当前功能范围

### F103

- 串口上报解析
- Home Assistant 状态上报
- ThingsBoard telemetry 上报
- 保留现有 F103 的 HA switch / 串口下发 / ThingsBoard RPC 能力

### Hi3861 Car

- 串口上报解析
- Home Assistant 状态上报
- ThingsBoard telemetry 上报
- 本阶段不实现小车远程控制

## 4. 串口协议

### 4.1 F103

状态行：

```text
TEMP:26.5,LIGHT:73,LED:OFF,BUZZER:OFF
```

### 4.2 Hi3861 Car

优先 JSON 行：

```json
{"device_id":"hi3861_car_01","type":"car","status":"RUNNING","direction":"FORWARD","speed":60,"distance_cm":35.2}
```

兼容文本行：

```text
CAR:hi3861_car_01,STATUS:RUNNING,DIR:FORWARD,SPEED:60,DIST:35.2
```

网关解析规则：

- `TEMP:` 开头：按 F103 解析
- `{...}`：按 Hi3861 JSON 解析
- `CAR:` 开头：按 Hi3861 文本协议解析
- 其他行：仅记录日志，不发布 MQTT

### 4.3 Hi3861 Car 当前上报字段

- `status`
- `direction`
- `speed`
- `distance_cm`

说明：

- `status` / `direction` / `speed` 来自当前 `auto_car_demo` 运行状态
- `distance_cm` 来自 HC-SR04 真实测距
- `battery_voltage` / `battery_percent` 本阶段不上报，因为当前不使用模拟值

## 5. Windows Python 环境准备

```powershell
cd D:\Proj\6.16\iot-upload\pz-f103-python-mqtt-gateway
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
Copy-Item .env.example .env
```

如果 PowerShell 执行策略受限：

```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```

## 6. 查找串口

```powershell
python scripts\list_serial_ports.py
```

按实际修改：

- `F103_SERIAL_PORT=COM3`
- `CAR_SERIAL_PORT=COM5`

## 7. .env 配置

实际运行使用 `.env`。当前已写入本地配置骨架：

```dotenv
F103_ENABLED=true
F103_SERIAL_PORT=COM3
F103_DEVICE_ID=f103_01
CAR_ENABLED=true
CAR_SERIAL_PORT=COM5
CAR_DEVICE_ID=hi3861_car_01
HA_MQTT_HOST=127.0.0.1
HA_MQTT_PORT=1883
TB_MQTT_HOST=127.0.0.1
TB_MQTT_PORT=1884
TB_ACCESS_TOKEN=8x8kPCELOMtfJL9f9f0q
CAR_TB_ACCESS_TOKEN=replace_with_hi3861_car_thingsboard_token
```

必须确认的字段：

- `F103_SERIAL_PORT`
- `CAR_SERIAL_PORT`
- `TB_ACCESS_TOKEN`
- `CAR_TB_ACCESS_TOKEN`
- `TB_MQTT_PORT`

## 8. 运行网关

```powershell
python gateway.py
```

或：

```powershell
.\scripts\run_gateway.ps1
```

启动后应看到类似日志：

```text
[CONFIG] loaded ...
[HA-MQTT] connected
[TB-MQTT-F103] connected
[CAR] raw {"device_id":"hi3861_car_01","type":"car","status":"RUNNING","direction":"FORWARD","speed":60,"distance_cm":35.2}
[CAR] parsed {'status': 'RUNNING', 'direction': 'FORWARD', 'speed': 60, 'distance_cm': 35.2}
```

## 9. Home Assistant 接入

### 9.1 F103 配置片段

- `homeassistant/configuration_pz_f103.yaml`
- `homeassistant/dashboard_example.yaml`

### 9.2 Hi3861 Car 配置片段

- `homeassistant/configuration_hi3861_car.yaml`
- `homeassistant/dashboard_hi3861_car.yaml`

Hi3861 Car topic 映射：

- `iot/hi3861_car_01/status`
- `iot/hi3861_car_01/direction`
- `iot/hi3861_car_01/speed`
- `iot/hi3861_car_01/distance_cm`

如果使用 MQTT Discovery，网关会自动注册这些实体。

如果使用手工 YAML，把配置片段合并到 Home Assistant 配置目录内的 `configuration.yaml` 或 `packages` 目录。

## 10. ThingsBoard 接入

F103 和 Hi3861 Car 必须在 ThingsBoard 中创建为两个独立设备，并使用两个独立 Access Token。

### F103

- 设备 token：`TB_ACCESS_TOKEN`
- telemetry topic：`v1/devices/me/telemetry`

### Hi3861 Car

- 设备 token：`CAR_TB_ACCESS_TOKEN`
- telemetry topic：`v1/devices/me/telemetry`

Hi3861 Car telemetry 示例：

```json
{"status":"RUNNING","direction":"FORWARD","speed":60,"distance_cm":35.2}
```

详见：

- `docs/thingsboard_setup.md`
- `docs/thingsboard_hi3861_car_setup.md`

## 11. MQTT 验证

### 11.1 F103 topic

```powershell
mosquitto_sub -h 127.0.0.1 -p 1883 -t "pz103/f103_01/#" -v
```

### 11.2 Hi3861 Car topic

```powershell
mosquitto_sub -h 127.0.0.1 -p 1883 -t "iot/hi3861_car_01/#" -v
```

### 11.3 Hi3861 Car HA 测试脚本

```powershell
.\scripts\test_hi3861_car_ha_publish.ps1
```

### 11.4 Hi3861 Car TB 测试脚本

```powershell
$env:CAR_TB_ACCESS_TOKEN="替换为小车设备 Access Token"
.\scripts\test_hi3861_car_tb_publish.ps1
```

## 12. WSL 与 Windows 网络注意事项

- Python 网关运行在 Windows 本机，因此网关连接本机 Broker 时可以使用 `127.0.0.1`
- F103 和 Hi3861 都不直接联网，所以都不需要知道电脑 IP
- 如果 Home Assistant 跑在 WSL、Mosquitto 跑在 Windows，Home Assistant 里的 MQTT Broker 地址不能想当然写 `127.0.0.1`
- 如果 Home Assistant 和 Mosquitto 在同一个 WSL 环境中运行，才可以直接使用 WSL 内部的 `127.0.0.1`

## 13. 常见问题

见：

- `docs/architecture.md`
- `docs/homeassistant_setup.md`
- `docs/thingsboard_setup.md`
- `docs/thingsboard_hi3861_car_setup.md`
- `docs/troubleshooting.md`

## 14. 后续扩展

1. Hi3861 Car 远程控制
2. Hi3861 Car ThingsBoard RPC
3. 双设备串口与平台断线重连可视化
4. F103 与 Hi3861 联合场景编排
