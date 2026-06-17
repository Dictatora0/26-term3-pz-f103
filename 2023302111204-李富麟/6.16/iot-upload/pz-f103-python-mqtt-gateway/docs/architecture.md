# Architecture

## 1. Multi-device topology

```text
F103 UART
  -> Gateway parser
  -> Home Assistant MQTT topics
  -> ThingsBoard telemetry

Hi3861 Car UART / JSON line
  -> Gateway parser
  -> Home Assistant MQTT topics
  -> ThingsBoard telemetry
```

## 2. Layering

### Device layer

- `F103` only knows its own sensor/status UART protocol
- `Hi3861 Car` only knows its own car status UART protocol
- neither device knows Home Assistant topic names
- neither device knows ThingsBoard Access Token

### Gateway layer

Python Gateway is the only protocol adaptation layer.

Responsibilities:

- open Windows COM ports
- keep F103 and Hi3861 serial readers independent
- parse F103 `TEMP:` status lines
- parse Hi3861 JSON lines and `CAR:` text lines
- publish Home Assistant MQTT topics
- publish ThingsBoard telemetry with per-device token isolation
- keep logs and reconnect logic

### Platform layer

- `Home Assistant` consumes MQTT `state_topic`
- `ThingsBoard` consumes JSON telemetry on `v1/devices/me/telemetry`

## 3. Data flow

### F103

```text
TEMP:26.5,LIGHT:73,LED:OFF,BUZZER:OFF
  -> parse_f103_status_line()
  -> pz103/f103_01/temperature
  -> pz103/f103_01/light
  -> pz103/f103_01/led/state
  -> pz103/f103_01/buzzer/state
  -> v1/devices/me/telemetry
```

### Hi3861 Car

```text
{"device_id":"hi3861_car_01","type":"car","status":"RUNNING","direction":"FORWARD","speed":60,"distance_cm":35.2}
  -> parse_hi3861_json_line()
  -> iot/hi3861_car_01/status
  -> iot/hi3861_car_01/direction
  -> iot/hi3861_car_01/speed
  -> iot/hi3861_car_01/distance_cm
  -> v1/devices/me/telemetry
```

Compatible text fallback:

```text
CAR:hi3861_car_01,STATUS:RUNNING,DIR:FORWARD,SPEED:60,DIST:35.2
```

## 4. Device boundaries

### F103

- sensor sampling
- LED/buzzer status reporting
- UART text output

### Hi3861 Car

- car motion state
- direction state
- speed level
- HC-SR04 distance reporting
- UART text output

### Python Gateway

- serial parsing
- topic mapping
- MQTT publishing
- ThingsBoard client per device token

### Home Assistant

- local entity display
- MQTT Discovery / YAML config

### ThingsBoard

- telemetry ingestion
- latest telemetry
- dashboard widgets

## 5. Token isolation

F103 and Hi3861 Car are separate ThingsBoard devices.

- `TB_ACCESS_TOKEN` -> F103
- `CAR_TB_ACCESS_TOKEN` -> Hi3861 Car

They must not share the same token.

## 6. Hi3861 Car current telemetry scope

Current car telemetry fields:

- `status`
- `direction`
- `speed`
- `distance_cm`

Not reported in this phase:

- `battery_voltage`
- `battery_percent`

Reason: this phase does not use simulated values.

## 7. Control boundary

Current control path is only retained for F103.

Not implemented for Hi3861 Car in this phase:

- Home Assistant car control panel
- car serial control command downlink
- ThingsBoard car RPC
