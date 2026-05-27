# STM32F103 + ESP8266 + Ubuntu/WSL Mosquitto

## 1. What This Project Now Does

This repository now focuses on:

Ubuntu/WSL Mosquitto  
-> Windows LAN IP `:1883`  
-> ESP8266 AT firmware  
-> STM32F103 board

Important:

- The board does **not** connect to WSL `localhost`.
- In the default WSL2 NAT mode they should connect to the **Windows host LAN IP**.
- Windows forwards TCP `1883` to the Ubuntu/WSL Mosquitto service.
- A phone MQTT app is optional and not required for the current experiment.

## 2. Current Repository Structure

- `User/main.c`
  - firmware entry point
- `User/esp8266_mqtt_config.h`
  - centralized Wi-Fi, broker, topic and client configuration
- `APP/esp8266/wifi_function.c`
  - ESP8266 AT command layer, MQTT connect/subscribe/publish flow, AT diagnostics
- `APP/mqtt_app/mqtt_app.c`
  - telemetry publish loop, control message parsing, LED handling
- `APP/mqtt_app/sensor_temp_hum.c`
  - temperature and optional DHT11 humidity read
- `config/mosquitto/mosquitto.conf`
  - Mosquitto config template for Ubuntu/WSL
- `scripts/wsl/install_mosquitto.sh`
  - WSL Mosquitto install and config helper
- `scripts/windows/setup_wsl_mqtt_portproxy.ps1`
  - Windows admin script to forward `1883` to WSL
- `scripts/windows/remove_wsl_mqtt_portproxy.ps1`
  - cleanup helper
- `docs/WSL_MQTT_DEPLOY.md`
  - detailed deployment, validation and troubleshooting guide

## 3. Firmware Behavior

The board uses ESP-AT built-in MQTT commands:

- `AT+MQTTUSERCFG`
- `AT+MQTTCONNCFG`
- `AT+MQTTCONN`
- `AT+MQTTSUB`
- `AT+MQTTPUBRAW`

So this project does **not** hand-build MQTT frames. The MQTT protocol work is done by the ESP8266 AT firmware.

Current runtime behavior:

- STM32 reads internal temperature
- optional DHT11 humidity can be enabled
- board publishes JSON telemetry to `pz103/telemetry`
- board subscribes `pz103/control`
- Ubuntu can send raw text commands or JSON commands with `mosquitto_pub`
- board publishes status/ack to `pz103/status`

## 4. Firmware Configuration

Edit only:

- `User/esp8266_mqtt_config.h`

Key macros:

```c
#define WIFI_SSID              "DESKTOP-6NM70T"
#define WIFI_PASSWORD          "LFL-lab-204"
#define MQTT_HOST              "192.168.137.1"
#define MQTT_PORT              1883
#define MQTT_USERNAME          ""
#define MQTT_PASSWORD          ""
#define MQTT_CLIENT_ID         "pz103_f103_esp8266"
#define MQTT_TOPIC_TELEMETRY   "pz103/telemetry"
#define MQTT_TOPIC_CONTROL     "pz103/control"
#define MQTT_TOPIC_STATUS      "pz103/status"
#define SENSOR_ENABLE_DHT11    0
```

Rules:

- `MQTT_HOST` must be the Windows host LAN IP that forwards to WSL.
- Do not set `MQTT_HOST` to `127.0.0.1`.
- Do not commit your real Wi-Fi credentials back into the repo.

Current values already written into `User/esp8266_mqtt_config.h` on this machine:

- `WIFI_SSID = "DESKTOP-6NM70T"`
- `WIFI_PASSWORD = "LFL-lab-204"`
- `MQTT_HOST = "192.168.137.1"`
- `MQTT_PORT = 1883`

Current Ubuntu/WSL broker IP on this machine:

- `172.29.37.36`

Note:

- The Windows hotspot adapter on this machine currently uses `192.168.137.1`.
- ESP8266 should join hotspot `DESKTOP-6NM70T`.
- When it is on that hotspot, the MQTT broker host must be `192.168.137.1`.
- The upstream campus WLAN address `10.128.138.121` is not the address hotspot clients should use.

## 5. Ubuntu/WSL Deployment

Quick install:

```bash
chmod +x scripts/wsl/install_mosquitto.sh
./scripts/wsl/install_mosquitto.sh
```

Quick verify:

```bash
chmod +x scripts/wsl/verify_mqtt.sh
./scripts/wsl/verify_mqtt.sh
```

Manual install:

```bash
sudo apt update
sudo apt install -y mosquitto mosquitto-clients
sudo install -m 644 config/mosquitto/mosquitto.conf /etc/mosquitto/conf.d/ubuntu_mqtt_f103.conf
sudo service mosquitto restart
sudo service mosquitto status
ss -lntp | grep 1883
```

WSL self-test:

```bash
mosquitto_sub -h 127.0.0.1 -p 1883 -t "test/#" -v
mosquitto_pub -h 127.0.0.1 -p 1883 -t "test/ping" -m "hello"
```

Board topic monitor:

```bash
mosquitto_sub -h 127.0.0.1 -p 1883 -t "pz103/#" -v
```

## 6. Windows Port Forwarding to WSL

Run PowerShell as Administrator:

```powershell
.\scripts\windows\setup_wsl_mqtt_portproxy.ps1
```

If Windows itself also has a `Mosquitto Broker` service installed and running, stop it first or let the setup script warn you. Otherwise the board may connect to the Windows broker instead of the WSL broker.

Admin helper:

```powershell
.\scripts\windows\stop_windows_mosquitto.ps1
.\scripts\windows\setup_wsl_mqtt_portproxy.ps1
```

This script:

- reads the current WSL IP
- recreates the `netsh interface portproxy` rule for `1883`
- creates or reuses a Windows firewall allow rule
- prints the Windows LAN IP candidates for ESP8266 and any optional external MQTT client

If WSL IP changes after reboot, run the script again.

## 7. Ubuntu-to-Board MQTT Test

Use WSL directly for the required experiment.

Terminal A:

```bash
mosquitto_sub -h 127.0.0.1 -p 1883 -t "pz103/#" -v
```

Terminal B:

```bash
mosquitto_pub -h 127.0.0.1 -p 1883 -t "pz103/control" -m '{"cmd":"ping"}'
mosquitto_pub -h 127.0.0.1 -p 1883 -t "pz103/control" -m '{"led":1}'
mosquitto_pub -h 127.0.0.1 -p 1883 -t "pz103/control" -m '{"led":0}'
```

Legacy raw text is also accepted:

- `LED_ON`
- `LED_OFF`
- `PING`

Optional later:

- you can still attach a phone MQTT app to `192.168.137.1:1883`

## 8. Build and Flash

Toolchain:

- Keil uVision / ARMCC5
- Device: `STM32F103ZE`
- Macros: `USE_STDPERIPH_DRIVER`, `STM32F10X_HD`

Build:

1. Open `F103_MQTT_REBUILD.uvprojx`
2. Edit `User/esp8266_mqtt_config.h`
3. Build
4. Flash to the board
5. Open USART1 at `115200`

Hardware wiring:

- USART1 debug: `PA9 / PA10`
- USART3 to ESP8266: `PB10 / PB11`
- common `GND`
- stable `3.3V` power for ESP8266

## 9. Acceptance Flow

1. Start Ubuntu/WSL Mosquitto.
2. In WSL run the `mosquitto_sub` / `mosquitto_pub` self-test.
3. On Windows run the portproxy setup script.
4. Confirm Windows port `1883` is reachable.
5. Flash the board and open USART1 logs.
6. In WSL run `mosquitto_sub -h 127.0.0.1 -p 1883 -t "pz103/#" -v`.
7. Wait for the board MQTT init log and first telemetry frame.
8. In another WSL terminal publish `{"cmd":"ping"}` to `pz103/control` and confirm the board logs the message and replies on `pz103/status`.
9. Publish `{"led":1}` / `{"led":0}` and confirm LED changes.
10. Observe periodic JSON telemetry on `pz103/telemetry`.

Expected telemetry example:

```json
{"device":"pz103-f103","seq":1,"temperature_c":25.40}
```

Expected status example:

```json
{"event":"control","result":"PONG","device":"pz103-f103","client_id":"pz103_f103_esp8266"}
```

## 10. Known Limits

- WSL2 NAT mode usually needs port forwarding for LAN devices.
- ESP8266 must stay on a reachable `2.4 GHz` network.
- Light sensor data is not implemented in this repo, so telemetry no longer sends fake `light` values.
- DHT11 is optional and disabled by default for faster bring-up.
- If the board uses the Windows hotspot path, `MQTT_HOST` must stay `192.168.137.1`.

## 11. Troubleshooting

Short version:

- Optional external client cannot connect: wrong host IP, firewall blocked, or `portproxy` missing
- WSL local test works but board fails: Windows forwarding or firewall issue
- Board publishes successfully but WSL `mosquitto_sub` sees nothing: Windows native `mosquitto.exe` is probably still occupying `1883`
- ESP8266 AT fails: stop checking phone topics first; fix power, baud, TX/RX, GND, EN, RST, and AT firmware
- MQTT connect fails: `MQTT_HOST` still wrong or broker not listening on `0.0.0.0:1883`
- Messages not received: wrong topic, wrong broker IP, or network isolation

For the current experiment, the direct path is:

- WSL `mosquitto_sub` sees no `pz103/telemetry`: board did not reach MQTT ready state
- board log shows repeated `AT` timeout: fix ESP8266 power/wiring/firmware first

Detailed troubleshooting is in:

- `docs/WSL_MQTT_DEPLOY.md`
- `docs/ESP8266_AT_BRINGUP.md`
