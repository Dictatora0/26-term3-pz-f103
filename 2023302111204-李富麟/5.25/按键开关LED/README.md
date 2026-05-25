# STM32F103 + ESP8266 AT MQTT LED/LCD Demo

## Overview
- MCU: `STM32F103ZE`
- Wi-Fi module: `ESP8266` with AT firmware
- Broker protocol: `MQTT over TCP`
- Functions:
  - periodic temperature publish
  - optional DHT11 humidity
  - MQTT command subscribe
  - LED control by MQTT
  - local key control for LED
  - LCD real-time status display

## Main Project File
- Open `F103_MQTT_REBUILD.uvprojx`

Other `.uvprojx` files in this repository are historical variants and are not required for normal use.

## Configuration
Edit `User/esp8266_mqtt_config.h`:

- `WIFI_SSID`
- `WIFI_PASSWORD`
- `MQTT_HOST`
- `MQTT_PORT`
- `MQTT_CLIENT_ID`
- `MQTT_TOPIC_DATA`
- `MQTT_TOPIC_CMD`
- `MQTT_TOPIC_ACK`
- `MQTT_PUBLISH_PERIOD_MS`
- `SENSOR_ENABLE_DHT11`

Default values are placeholders and must be changed for your own network.

## Topics
- Publish data: `puzhong/f103/data`
- Publish temperature: `puzhong/f103/temperature`
- Receive commands: `puzhong/f103/cmd`
- Publish ack: `puzhong/f103/ack`

## Current Behavior
- 手机通过 MQTT 发布命令到 `puzhong/f103/cmd`
- 开发板通过 ESP8266 订阅命令并控制 `LED1`
- 本地按键也可以直接控制 LED，同时把状态回传到 `puzhong/f103/ack`
- 温度会额外发布到 `puzhong/f103/temperature`，方便手机单独显示
- LCD 页面实时显示：
  - MQTT 在线状态
  - LED 当前状态
  - 最近一次控制来源与动作
  - 本地/远端控制次数
  - 温度/湿度

## Current Payload Format
The project currently publishes plain text payloads through `AT+MQTTPUBRAW`:

```text
T=31.17;H=null;L=null;DEV=puzhong-f103;TS=1
```

This avoids ESP-AT string parsing issues seen with `AT+MQTTPUB`.

## Typical Commands
Publish to `puzhong/f103/cmd`:

- `LED_ON`
- `LED_OFF`
- `TOGGLE`
- `PING`
- `STATUS`
- `ON`
- `OFF`
- `1`
- `0`

Expected ack messages on `puzhong/f103/ack`:

- `LED_ON_OK`
- `LED_OFF_OK`
- `LED_TOGGLE_OK`
- `PONG`
- `CMD_UNKNOWN`
- state text like:

```text
LED=ON;MQTT=ON;SRC=MQTT;ACT=LED_ON;LOCAL=1;REMOTE=3;T=30.25;H=null
```

Temperature topic payload example:

```text
30.25
```

## Local Keys
- `KEY0` or `KEY_UP`: toggle LED
- `KEY1`: force LED on
- `KEY2`: force LED off

Default key pins reused from the sibling board projects:
- `KEY_UP -> PA0`
- `KEY0 -> PE4`
- `KEY1 -> PE3`
- `KEY2 -> PE2`

## LCD
- Reused the sibling project FSMC TFT LCD driver
- Default LCD driver macro: `TFTLCD_HX8357DN`
- Default display direction: portrait
- Backlight: `PB0`

If your LCD controller differs, switch the macro in `APP/tftlcd/tftlcd.h`.

## Wiring
- USART1 debug: `PA9 / PA10`, `115200`
- USART3 to ESP8266: `PB10 TX / PB11 RX`, `115200`
- LCD FSMC bus: reused from sibling LCD projects on this board family
- Common `GND`
- Stable `3.3V` supply for ESP8266

## Build Notes
- Toolchain: `Keil uVision / ARMCC5`
- Device: `STM32F103ZE`
- Macros: `USE_STDPERIPH_DRIVER`, `STM32F10X_HD`
- Startup file: `startup_stm32f10x_hd.s`

## Bring-up Steps
1. Open `F103_MQTT_REBUILD.uvprojx` in Keil.
2. Edit `User/esp8266_mqtt_config.h`.
3. Build and flash the board.
4. Open USART1 terminal at `115200`.
5. Start an MQTT broker reachable from ESP8266.
6. Subscribe to `puzhong/f103/#`.
7. Send commands from the phone MQTT app to `puzhong/f103/cmd`.
8. Verify LED state change, LCD refresh, and `puzhong/f103/ack` response.

## Notes
- ESP8266 must join a `2.4 GHz` Wi-Fi network.
- Broker host must be a LAN-reachable IP, not `127.0.0.1`.
- If using a Windows hotspot, phone and ESP8266 must both join that hotspot.
- This version assumes the same board wiring as the sibling LCD/key projects under the parent `D:\Proj` directory.
