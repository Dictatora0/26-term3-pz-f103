# Home Assistant Setup

## 1. MQTT broker planning

- Windows Python Gateway connects to local broker with `127.0.0.1`
- If Home Assistant runs in WSL, `127.0.0.1` inside WSL may not mean Windows host
- If Mosquitto runs on Windows and Home Assistant runs in WSL, use the Windows host IP visible from WSL
- If Home Assistant and Mosquitto both run inside the same WSL environment, `127.0.0.1` is fine

## 2. F103 configuration files

- `homeassistant/configuration_pz_f103.yaml`
- `homeassistant/dashboard_example.yaml`

## 3. Hi3861 Car configuration files

- `homeassistant/configuration_hi3861_car.yaml`
- `homeassistant/dashboard_hi3861_car.yaml`

## 4. Hi3861 Car topic mapping

- `iot/hi3861_car_01/status`
- `iot/hi3861_car_01/direction`
- `iot/hi3861_car_01/speed`
- `iot/hi3861_car_01/distance_cm`

## 5. Entity list

F103:

- `sensor.pz_f103_temperature`
- `sensor.pz_f103_light`
- `sensor.pz_f103_led_state`
- `sensor.pz_f103_buzzer_state`

Hi3861 Car:

- `sensor.hi3861_car_status`
- `sensor.hi3861_car_direction`
- `sensor.hi3861_car_speed`
- `sensor.hi3861_car_distance`

## 6. Discovery and manual YAML

If `HA_DISCOVERY_ENABLED=true`, the gateway publishes MQTT Discovery payloads automatically.

If you use manual YAML instead, merge the configuration fragments into Home Assistant config and reload or restart Home Assistant.

## 7. Verification

Subscribe to the MQTT topics:

```powershell
mosquitto_sub -h 127.0.0.1 -p 1883 -t "pz103/f103_01/#" -v
mosquitto_sub -h 127.0.0.1 -p 1883 -t "iot/hi3861_car_01/#" -v
```
