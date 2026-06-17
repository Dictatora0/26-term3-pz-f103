# ThingsBoard Setup

ThingsBoard source directory in this environment:

```text
D:\thingsboard-master
```

Do not modify ThingsBoard core source files. The Python gateway uses ThingsBoard MQTT Device API.

## Create Device

1. Open the local ThingsBoard UI.
2. Log in with your tenant account.
3. Create a device:

```text
Name: PZ F103 Board
Device profile: default or custom STM32F103 profile
```

4. Open the device details.
5. Copy the device Access Token.

## Configure Gateway Token

Edit `.env`:

```dotenv
TB_ENABLED=true
TB_MQTT_HOST=127.0.0.1
TB_MQTT_PORT=1884
TB_ACCESS_TOKEN=your_thingsboard_device_access_token
```

The gateway never sends this token to the F103 board. The token stays on the Windows Python gateway.

## MQTT Port

Recommended local ports:

```text
Home Assistant / Mosquitto: 1883
ThingsBoard MQTT:          1884
```

If your local ThingsBoard MQTT transport uses 1883, change:

```dotenv
TB_MQTT_PORT=1883
```

## Test Telemetry

Use:

```powershell
$env:TB_ACCESS_TOKEN="xxx"
.\scripts\test_tb_publish.ps1
```

Then open the ThingsBoard device details and check:

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
