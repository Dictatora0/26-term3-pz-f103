# ThingsBoard Hi3861 Car Setup

## 1. Create the car device

1. Log in to local ThingsBoard.
2. Open `Devices`.
3. Create a new device:
   - `Name`: `Huawei Hi3861 Car`
   - `Type` or `Profile`: `smart_car`

## 2. Copy the Access Token

Open the device details and copy its Access Token.

Write it into `.env`:

```dotenv
CAR_TB_ACCESS_TOKEN=replace_with_real_hi3861_car_token
```

## 3. Confirm MQTT endpoint

Default local endpoint:

```dotenv
TB_MQTT_HOST=127.0.0.1
TB_MQTT_PORT=1884
```

If your local ThingsBoard MQTT listener is actually on `1883`, change:

```dotenv
TB_MQTT_PORT=1883
```

## 4. Start the gateway

```powershell
cd D:\Proj\6.16\iot-upload\pz-f103-python-mqtt-gateway
python gateway.py
```

## 5. Expected telemetry payload

```json
{"status":"RUNNING","direction":"FORWARD","speed":60,"distance_cm":35.2}
```

Current telemetry keys:

- `status`
- `direction`
- `speed`
- `distance_cm`

Battery fields are intentionally not reported in this phase.

## 6. Validate with mosquitto_pub

```powershell
$env:CAR_TB_ACCESS_TOKEN="your_real_hi3861_car_token"
.\scripts\test_hi3861_car_tb_publish.ps1
```

## 7. Validate in ThingsBoard

Open:

```text
Devices -> Huawei Hi3861 Car -> Latest telemetry
```

You should see:

- `status`
- `direction`
- `speed`
- `distance_cm`
