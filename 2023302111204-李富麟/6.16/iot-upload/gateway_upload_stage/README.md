# PZ F103 Python MQTT Gateway

## Project Goal

This project connects a PZ STM32F103 board to local Home Assistant and ThingsBoard through a Windows Python MQTT gateway.

The F103 board only reports UART text lines. It does not connect to Home Assistant, ThingsBoard, Wi-Fi, or MQTT directly.

## Environment Paths

```text
Home Assistant Core source: D:\core-dev
ThingsBoard source:        D:\thingsboard-master
Python MQTT Gateway:       D:\pz-f103-python-mqtt-gateway
```

Home Assistant Core is expected to run from WSL. ThingsBoard is expected to run locally on Windows. This gateway project is independent and must not be placed inside `D:\core-dev` or `D:\thingsboard-master`.

## Architecture

```text
PZ STM32F103 Board
  UART Serial
  TEMP:26.5,LIGHT:73,LED:OFF,BUZZER:OFF
        |
        v
Windows Python MQTT Gateway
  read COM port
  parse status line
  publish Home Assistant MQTT topics
  publish ThingsBoard telemetry
        |
        +--> Home Assistant MQTT Broker
        |    pz103/f103_01/temperature
        |    pz103/f103_01/light
        |    pz103/f103_01/led/state
        |    pz103/f103_01/buzzer/state
        |
        +--> ThingsBoard MQTT Device API
             v1/devices/me/telemetry
```

The Python gateway is the only protocol adapter. F103 does not store MQTT topics, Wi-Fi settings, Home Assistant settings, or ThingsBoard Access Tokens.

## Current Scope

Implemented in this stage:

1. Read F103 serial data from Windows COM port.
2. Parse status lines that start with `TEMP:`.
3. Publish Home Assistant MQTT state topics.
4. Publish ThingsBoard telemetry JSON.
5. Provide Home Assistant YAML snippets.
6. Provide ThingsBoard setup documentation.
7. Provide Windows scripts and troubleshooting notes.

Not implemented in this stage:

1. Home Assistant reverse control for LED or buzzer.
2. ThingsBoard RPC.
3. Board-side command receiving.
4. Any F103 Wi-Fi or MQTT behavior.

## F103 UART Protocol

The gateway parses:

```text
TEMP:26.5,LIGHT:73,LED:OFF,BUZZER:OFF
```

Rules:

1. Supports `\r\n` and `\n`.
2. Empty lines are ignored.
3. Lines not starting with `TEMP:` are logged as board logs and are not published.
4. Missing fields are allowed; only valid fields are published.
5. Invalid `TEMP` or `LIGHT` values are logged as warnings.
6. `LED` and `BUZZER` only accept `ON` or `OFF`.
7. Raw serial lines are kept in logs for debugging.

## Windows Python Setup

Run:

```powershell
cd D:\pz-f103-python-mqtt-gateway
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
Copy-Item .env.example .env
```

If PowerShell blocks scripts:

```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```

## Find COM Port

```powershell
python scripts\list_serial_ports.py
```

Set the actual board port in `.env`, for example:

```dotenv
SERIAL_PORT=COM3
BAUD_RATE=115200
SERIAL_TIMEOUT=1
```

## Configure .env

Required edits:

```dotenv
SERIAL_PORT=COM3
TB_ACCESS_TOKEN=replace_with_thingsboard_device_access_token
```

Full example:

```dotenv
SERIAL_PORT=COM3
BAUD_RATE=115200
SERIAL_TIMEOUT=1
DEVICE_ID=f103_01
HA_ENABLED=true
HA_MQTT_HOST=127.0.0.1
HA_MQTT_PORT=1883
HA_MQTT_USERNAME=
HA_MQTT_PASSWORD=
TB_ENABLED=true
TB_MQTT_HOST=127.0.0.1
TB_MQTT_PORT=1884
TB_ACCESS_TOKEN=replace_with_thingsboard_device_access_token
LOG_LEVEL=INFO
```

If `TB_ACCESS_TOKEN` is still the placeholder, the gateway logs a warning and skips ThingsBoard publishing. Home Assistant publishing can still work.

Because the Python gateway runs on Windows, it can connect to a Windows-local broker with `127.0.0.1`. The F103 board does not connect to the network, so it does not need to know the PC IP.

## Run Gateway

```powershell
cd D:\pz-f103-python-mqtt-gateway
.\.venv\Scripts\Activate.ps1
python gateway.py
```

Or:

```powershell
.\scripts\run_gateway.ps1
```

Expected logs include:

```text
[CONFIG] loaded .env
[SERIAL] opening COM3 at 115200
[HA-MQTT] connecting 127.0.0.1:1883
[HA-MQTT] connected
[SERIAL] raw TEMP:26.5,LIGHT:73,LED:OFF,BUZZER:OFF
[SERIAL] parsed {'temperature': 26.5, 'light': 73, 'led': 'OFF', 'buzzer': 'OFF'}
[HA-MQTT] publish pz103/f103_01/temperature 26.5
```

## Home Assistant Setup

Use:

```text
homeassistant\configuration_pz_f103.yaml
```

Copy or merge it into the Home Assistant configuration directory used by the local Core runtime. Do not modify Home Assistant core source files under `D:\core-dev`.

If `configuration.yaml` already contains `mqtt:`, merge the `sensor:` list into the existing `mqtt:` section.

Entities:

```text
sensor.pz_f103_temperature
sensor.pz_f103_light
sensor.pz_f103_led_state
sensor.pz_f103_buzzer_state
```

Use dashboard example:

```text
homeassistant\dashboard_example.yaml
```

After editing YAML, restart Home Assistant or reload the relevant YAML configuration.

## ThingsBoard Setup

Open local ThingsBoard, create a device:

```text
Name: PZ F103 Board
Device profile: default or custom STM32F103
```

Copy the device Access Token and set:

```dotenv
TB_ENABLED=true
TB_MQTT_HOST=127.0.0.1
TB_MQTT_PORT=1884
TB_ACCESS_TOKEN=your_device_access_token
```

Recommended local port plan:

```text
Home Assistant / Mosquitto: 1883
ThingsBoard MQTT:          1884
```

If local ThingsBoard MQTT uses 1883, change:

```dotenv
TB_MQTT_PORT=1883
```

The gateway publishes:

```text
topic: v1/devices/me/telemetry
```

Payload:

```json
{"temperature":26.5,"light":73,"led":"OFF","buzzer":"OFF"}
```

## Verify Home Assistant MQTT

Subscribe:

```powershell
mosquitto_sub -h 127.0.0.1 -p 1883 -t "pz103/f103_01/#" -v
```

Or use the script:

```powershell
.\scripts\subscribe_ha_topics.ps1
```

Publish test values:

```powershell
.\scripts\test_ha_publish.ps1
```

MQTTX can also connect to `127.0.0.1:1883` and subscribe to:

```text
pz103/f103_01/#
```

## Verify ThingsBoard

Set token and publish test telemetry:

```powershell
$env:TB_ACCESS_TOKEN="替换为ThingsBoard设备AccessToken"
.\scripts\test_tb_publish.ps1
```

Then open ThingsBoard device details and check:

```text
Latest telemetry
```

Expected keys:

```text
temperature
light
led
buzzer
```

## WSL And Windows Network Notes

If Home Assistant runs in WSL, `127.0.0.1` inside Home Assistant means the WSL environment, not necessarily Windows.

If Mosquitto runs on Windows and Home Assistant runs in WSL, configure Home Assistant MQTT integration to use the Windows host IP visible from WSL instead of `127.0.0.1`.

If Home Assistant and Mosquitto both run in the same WSL environment, `127.0.0.1` can be correct.

The Python gateway runs on Windows, so it can use `127.0.0.1` for Windows-local Mosquitto or ThingsBoard services.

## Common Troubleshooting

See:

```text
docs\troubleshooting.md
```

High-frequency checks:

1. COM port exists: `python scripts\list_serial_ports.py`
2. COM port is not occupied by Keil or serial assistant.
3. Baud rate is `115200`.
4. F103 outputs lines starting with `TEMP:`.
5. Mosquitto listens on `1883`.
6. Home Assistant MQTT integration is connected to the right broker.
7. ThingsBoard MQTT port is correct.
8. ThingsBoard Access Token is not the placeholder.
9. PowerShell execution policy allows scripts.
10. `mosquitto_pub` and `mosquitto_sub` are available in `PATH`.

## Future Extensions

Later stages can add:

1. Home Assistant MQTT switch command topics.
2. Python subscription and serial command writing.
3. F103 command receiving and GPIO control.
4. ThingsBoard RPC.
5. Command acknowledgements, retries, and error reporting.
