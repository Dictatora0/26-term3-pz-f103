# Puzhong F103 + ESP8266 MQTT Lab

## Goal
- Read temperature on STM32F103.
- Use USART3 to control ESP8266 AT firmware.
- Connect WiFi and MQTT broker.
- Publish sensor data periodically to MQTT.
- iPhone subscribes and sees live data.
- iPhone publishes `LED_ON`/`LED_OFF` to control board LED.

## Implementation Status
- Temperature: implemented (internal ADC temp sensor, always available).
- Humidity: optional. DHT11 driver exists but can be disabled for fast bring-up.
- MQTT: implemented via `AT+MQTTUSERCFG`, `AT+MQTTCONN`, `AT+MQTTSUB`, `AT+MQTTPUB`.

## DHT11 Note
- DHT11 driver is now added in this project (`APP/dht11/dht11.c`).
- Default DHT11 pin is `PA8`, configurable in `User/esp8266_mqtt_config.h`.
- Added diagnostics:
  - read success/fail counters
  - consecutive fail counter
  - auto reset counter
  - last error code
  - last raw 5 bytes from DHT11 frame
- Added auto-recovery:
  - when consecutive read failures reach `DHT11_AUTO_RESET_FAIL_THRESHOLD`, data pin is reinitialized automatically.
- If DHT11 read checksum/timing fails at runtime, payload will fallback to:
  - valid temperature
  - `humidity: null`

## Central Config File
- `User/esp8266_mqtt_config.h`

Edit these macros only:
- `WIFI_SSID`
- `WIFI_PASSWORD`
- `MQTT_HOST`
- `MQTT_PORT`
- `MQTT_CLIENT_ID`
- `MQTT_TOPIC_DATA`
- `MQTT_TOPIC_CMD`
- `MQTT_TOPIC_ACK`
- `MQTT_PUBLISH_PERIOD_MS`
- `SENSOR_ENABLE_DHT11` (`0` = temperature only, `1` = enable DHT11 humidity)
- `DHT11_GPIO_PORT`
- `DHT11_GPIO_PIN`
- `DHT11_GPIO_RCC`
- `DHT11_AUTO_RESET_FAIL_THRESHOLD`

## Topics and Payload
- Publish data: `puzhong/f103/data`
- Receive command: `puzhong/f103/cmd`
- Ack topic: `puzhong/f103/ack`

Example payload:
```json
{"temperature":25.60,"humidity":62.00,"light":null,"device":"puzhong-f103","ts":1}
```

## iPhone MQTT App Setup
- Host: broker LAN IP (for Windows hotspot usually `192.168.137.1`, not `127.0.0.1`)
- Port: `1883`
- Client ID: `iphone_client_001`
- Username/Password: empty
- Subscribe:
  - `puzhong/f103/data`
  - `puzhong/f103/ack` (optional)
- Publish topic: `puzhong/f103/cmd`
- Test commands:
  - `LED_ON`
  - `LED_OFF`
  - `PING`

## Broker Notes (Mosquitto)
Start:
```bash
mosquitto -v
```

Or if installed as Windows service:
```powershell
net start mosquitto
```

Optional config:
```conf
listener 1883 0.0.0.0
allow_anonymous true
```

Requirements:
- Phone, ESP8266, broker host are in same LAN.
- ESP8266 uses 2.4GHz WiFi.
- Windows firewall allows 1883.

## Serial and Wiring
- USART1 debug: PA9/PA10, 115200.
- USART3 ESP8266: PB10 TX / PB11 RX, 115200.
- Common GND required.
- Stable 3.3V power for ESP8266.

## Keil Build Notes
- Device: `STM32F103ZE`
- Macros: `USE_STDPERIPH_DRIVER, STM32F10X_HD`
- Startup: `startup_stm32f10x_hd.s`
- Compiler: ARMCC 5.06
- Added source files in `Template.uvprojx`:
  - `APP/adc_temp/adc_temp.c`
  - `APP/mqtt_app/sensor_temp_hum.c`
  - `APP/mqtt_app/mqtt_app.c`
  - `APP/dht11/dht11.c`

## Validation Steps
1. Open `Template.uvprojx` in Keil and build.
2. Download firmware to board.
3. Edit `User/esp8266_mqtt_config.h` with your WiFi and broker info.
4. For temperature-only demo, set `SENSOR_ENABLE_DHT11` to `0`.
5. Start broker (`mosquitto -v` or `net start mosquitto`).
5. Open USART1 terminal at 115200.
6. Connect iPhone MQTT app to broker.
7. Subscribe `puzhong/f103/data`.
8. Observe periodic temperature JSON.
9. Publish `LED_ON` / `LED_OFF` to `puzhong/f103/cmd`.
10. Confirm LED changes and ack messages.

## Typical Debug Logs
- `[ESP] AT check OK`
- `[ESP] WiFi connected`
- `[MQTT] connected`
- `[MQTT] subscribed: puzhong/f103/cmd`
- `[SENSOR] temperature=xx.xx humidity=null`
- `[MQTT] publish OK`
- `[MQTT] recv cmd: LED_ON`
- `[DHT11] ok=10 fail=2 cons_fail=0 auto_reset=0 last_err=0 raw=62,0,25,0,87`

## Current Lab Preset
- WiFi SSID: `DESKTOP-6NM70T`
- WiFi password: `LFL-lab-204`
- MQTT host: `192.168.137.1`
- MQTT port: `1883`
- `SENSOR_ENABLE_DHT11`: `0` (temperature-only mode)
