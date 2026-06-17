# F103 UART Protocol

## 1. 总体原则

F103 和 Python Gateway 之间只使用 UART 文本协议。

F103 不保存：

- MQTT topic
- Wi-Fi 配置
- ThingsBoard Access Token
- Home Assistant 平台配置

## 2. 状态上报格式

```text
TEMP:26.5,LIGHT:73,LED:OFF,BUZZER:OFF
```

字段定义：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `TEMP` | float | 温度 |
| `LIGHT` | int / float | 光照 |
| `LED` | `ON` / `OFF` | LED 状态 |
| `BUZZER` | `ON` / `OFF` | 蜂鸣器状态 |

网关解析规则：

1. 空行忽略。
2. 只解析 `TEMP:` 开头的状态行。
3. 其他行作为普通串口日志打印，不发布 MQTT。
4. 字段缺失时，只发布已有字段。
5. `TEMP` / `LIGHT` 数字转换失败时，记录 warning，不崩溃。
6. `LED` / `BUZZER` 只接受 `ON` / `OFF`。

## 3. 串口控制命令

Python Gateway 写给 F103 的命令：

```text
LED_ON
LED_OFF
BUZZER_ON
BUZZER_OFF
```

命令结束符：

```text
\r\n
```

## 4. MQTT 到串口命令映射

### Home Assistant

| Topic | Payload | 串口命令 |
| --- | --- | --- |
| `pz103/f103_01/led/set` | `ON` | `LED_ON` |
| `pz103/f103_01/led/set` | `OFF` | `LED_OFF` |
| `pz103/f103_01/buzzer/set` | `ON` | `BUZZER_ON` |
| `pz103/f103_01/buzzer/set` | `OFF` | `BUZZER_OFF` |

### ThingsBoard RPC

| RPC method | params | 串口命令 |
| --- | --- | --- |
| `setLed` | `ON` / `true` / `1` | `LED_ON` |
| `setLed` | `OFF` / `false` / `0` | `LED_OFF` |
| `setBuzzer` | `ON` / `true` / `1` | `BUZZER_ON` |
| `setBuzzer` | `OFF` / `false` / `0` | `BUZZER_OFF` |

## 5. 状态回读

控制命令不是最终状态来源。最终状态以 F103 后续上报行为准。

例如：

1. HA 发送 `pz103/f103_01/led/set -> ON`
2. Python 写串口 `LED_ON`
3. F103 执行 GPIO
4. F103 下一条状态行上报：

```text
TEMP:26.5,LIGHT:73,LED:ON,BUZZER:OFF
```

5. Python 把 `LED:ON` 再发布到 MQTT state topic / ThingsBoard telemetry

## 6. 双串口模式

如果 `SERIAL_PORT` 用于状态上报，而控制方向不稳定，可以额外配置：

```dotenv
CONTROL_SERIAL_PORT=COM4
CONTROL_BAUD_RATE=115200
```

此时：

- `SERIAL_PORT` 负责读取 `TEMP:` 状态行
- `CONTROL_SERIAL_PORT` 负责写 `LED_ON` / `BUZZER_ON`

推荐 USART3 接线：

```text
USB-TTL TXD -> F103 PB11 / USART3_RX
USB-TTL RXD -> F103 PB10 / USART3_TX, optional
USB-TTL GND -> F103 GND
```
