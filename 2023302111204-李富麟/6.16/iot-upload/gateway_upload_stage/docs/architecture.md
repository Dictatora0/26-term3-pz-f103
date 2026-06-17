# Architecture

## Data Path

```text
F103 UART line
  -> Windows Python Gateway
  -> Home Assistant MQTT topics
  -> ThingsBoard telemetry
```

The F103 board only emits UART text lines such as:

```text
TEMP:26.5,LIGHT:73,LED:OFF,BUZZER:OFF
```

It does not know Home Assistant, ThingsBoard, MQTT topics, Wi-Fi credentials, or ThingsBoard Access Tokens.

## Responsibility Boundary

| Layer | Responsibility |
| --- | --- |
| F103 | Sensor sampling, GPIO state collection, UART text status protocol |
| Python Gateway | Read COM port, parse telemetry, publish MQTT topics, publish ThingsBoard telemetry |
| Home Assistant | Consume MQTT `state_topic` values as sensors |
| ThingsBoard | Receive JSON telemetry through `v1/devices/me/telemetry` |

The Python gateway is the only protocol adapter. This keeps the F103 firmware independent from platform-specific protocols.

## Home Assistant Topics

```text
pz103/f103_01/temperature
pz103/f103_01/light
pz103/f103_01/led/state
pz103/f103_01/buzzer/state
```

Home Assistant uses MQTT sensors to consume these `state_topic` values.

## ThingsBoard Telemetry

Topic:

```text
v1/devices/me/telemetry
```

Payload:

```json
{"temperature":26.5,"light":73,"led":"OFF","buzzer":"OFF"}
```

The MQTT username is the ThingsBoard device Access Token. The MQTT password is empty.

## Not Implemented In This Stage

This stage does not implement Home Assistant reverse control, ThingsBoard RPC, or board-side command receiving.
