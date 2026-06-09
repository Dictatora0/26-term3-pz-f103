# STM32F103 + ESP8266 AT MQTT Demo

## Overview
- MCU: `STM32F103ZE`
- Wi-Fi module: `ESP8266` with AT firmware
- Broker protocol: `MQTT over TCP`
- Functions:
  - periodic temperature publish
  - optional DHT11 humidity
  - MQTT command subscribe
  - LED control by MQTT

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
- Receive commands: `puzhong/f103/cmd`
- Publish ack: `puzhong/f103/ack`

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
- `PING`

Expected ack messages on `puzhong/f103/ack`:

- `LED_ON_OK`
- `LED_OFF_OK`
- `PONG`

## Wiring
- USART1 debug: `PA9 / PA10`, `115200`
- USART3 to ESP8266: `PB10 TX / PB11 RX`, `115200`
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
7. Verify periodic data publish and command response.

## Notes
- ESP8266 must join a `2.4 GHz` Wi-Fi network.
- Broker host must be a LAN-reachable IP, not `127.0.0.1`.
- If using a Windows hotspot, phone and ESP8266 must both join that hotspot.
