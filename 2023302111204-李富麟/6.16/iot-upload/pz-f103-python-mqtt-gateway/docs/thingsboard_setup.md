# ThingsBoard Setup

## 1. Device isolation

Create two separate devices in ThingsBoard:

1. `PZ F103 Board`
2. `Huawei Hi3861 Car`

Do not reuse the same Access Token for both devices.

## 2. Token mapping

`.env` fields:

```dotenv
TB_ACCESS_TOKEN=...          # F103
CAR_TB_ACCESS_TOKEN=...      # Hi3861 Car
```

## 3. MQTT endpoint

Default local endpoint:

```dotenv
TB_MQTT_HOST=127.0.0.1
TB_MQTT_PORT=1884
```

If your ThingsBoard MQTT listener uses `1883`, update `.env`.

## 4. F103 telemetry

Topic:

```text
v1/devices/me/telemetry
```

Payload example:

```json
{"temperature":26.5,"light":73,"led":"OFF","buzzer":"OFF"}
```

## 5. Hi3861 Car telemetry

Topic:

```text
v1/devices/me/telemetry
```

Payload example:

```json
{"status":"RUNNING","direction":"FORWARD","speed":60,"distance_cm":35.2}
```

## 6. Quick verification

F103:

```powershell
$env:TB_ACCESS_TOKEN="your_f103_token"
.\scripts\test_tb_publish.ps1
```

Hi3861 Car:

```powershell
$env:CAR_TB_ACCESS_TOKEN="your_hi3861_car_token"
.\scripts\test_hi3861_car_tb_publish.ps1
```
