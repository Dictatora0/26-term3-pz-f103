# Home Assistant Setup

Home Assistant Core source directory in this environment:

```text
D:\core-dev
```

Do not modify Home Assistant core source files. Copy the MQTT configuration snippet from this gateway project into the Home Assistant configuration directory used by your local Core runtime.

## MQTT Configuration

Use:

```text
homeassistant\configuration_pz_f103.yaml
```

If your Home Assistant `configuration.yaml` already has an `mqtt:` section, merge only the `sensor:` list into the existing section. Do not create two top-level `mqtt:` keys.

This stage only displays LED and BUZZER as sensors. Do not add MQTT switches yet.

## Dashboard Example

Use:

```text
homeassistant\dashboard_example.yaml
```

It contains these entities:

```text
sensor.pz_f103_temperature
sensor.pz_f103_light
sensor.pz_f103_led_state
sensor.pz_f103_buzzer_state
```

## WSL And Windows Network Notes

Home Assistant is described as running from WSL, while the Python gateway and Mosquitto may run on Windows.

If Home Assistant runs in WSL and Mosquitto runs on Windows, `127.0.0.1` inside WSL means the WSL VM, not necessarily Windows. In that case configure Home Assistant MQTT integration to connect to the Windows host IP as visible from WSL.

If Home Assistant and Mosquitto both run inside the same WSL environment, `127.0.0.1` can be correct.

If Python gateway runs on Windows and Mosquitto runs on Windows, the gateway can use:

```dotenv
HA_MQTT_HOST=127.0.0.1
HA_MQTT_PORT=1883
```

Always verify that Home Assistant MQTT integration is connected to the same broker where the Python gateway publishes.

## Reload

After changing YAML, restart Home Assistant or reload the relevant YAML configuration. Then check Developer Tools / States for the four `sensor.pz_f103_*` entities.
