# Troubleshooting

Run commands from:

```powershell
cd D:\pz-f103-python-mqtt-gateway
```

## 1. COM Port Not Found

```powershell
python scripts\list_serial_ports.py
```

If no F103 port appears, check USB cable, CH340 driver, Device Manager, and board power.

## 2. COM Port Is Busy

Close Keil serial monitor, SSCOM, XCOM, MQTTX serial tools, Arduino Serial Monitor, or any other program using the same COM port. Then run the gateway again.

## 3. Baud Rate Is Wrong

The default is:

```dotenv
BAUD_RATE=115200
```

It must match the F103 USART baud rate.

## 4. F103 Output Is Not TEMP:

The gateway only publishes lines starting with:

```text
TEMP:
```

Lines such as `[BOOT]`, `[INFO]`, `OK:`, and `ERR:` are logged and ignored.

## 5. Python Does Not Read Complete Lines

`gateway.py` uses `readline()`. The F103 status line must end with `\n` or `\r\n`.

Expected format:

```text
TEMP:26.5,LIGHT:73,LED:OFF,BUZZER:OFF
```

## 6. Home Assistant MQTT Broker Is Not Connected

Check Home Assistant Settings / Devices & services / MQTT. It must connect to the same broker used by:

```dotenv
HA_MQTT_HOST=127.0.0.1
HA_MQTT_PORT=1883
```

## 7. WSL Home Assistant Cannot Access Windows MQTT Broker

If Home Assistant runs in WSL and Mosquitto runs on Windows, `127.0.0.1` inside WSL may not point to Windows. Use the Windows host IP visible from WSL for the Home Assistant MQTT integration.

If Home Assistant and Mosquitto both run in the same WSL instance, `127.0.0.1` can be correct.

## 8. Mosquitto 1883 Is Not Listening

```powershell
Test-NetConnection 127.0.0.1 -Port 1883
```

Start Mosquitto or fix its listener configuration if the test fails.

## 9. ThingsBoard MQTT Port Is Not 1884

Check your local ThingsBoard MQTT transport port. If it is 1883, edit:

```dotenv
TB_MQTT_PORT=1883
```

Then restart the gateway.

## 10. ThingsBoard Access Token Is Wrong

If `TB_ACCESS_TOKEN` is still:

```dotenv
replace_with_thingsboard_device_access_token
```

the gateway skips ThingsBoard publishing and logs a warning. Copy the token from the ThingsBoard device details.

## 11. Latest Telemetry Has No Data

Check:

1. `TB_ENABLED=true`
2. `TB_ACCESS_TOKEN` is correct.
3. `TB_MQTT_HOST` and `TB_MQTT_PORT` are correct.
4. ThingsBoard MQTT transport is running.
5. Gateway logs show `[TB-MQTT] publish v1/devices/me/telemetry ...`.

## 12. PowerShell Execution Policy Blocks Scripts

Run:

```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```

Or run the commands manually from the script files.

## 13. mosquitto_pub Or mosquitto_sub Missing

Install Mosquitto clients and ensure the install directory is in `PATH`.

Test:

```powershell
mosquitto_pub --help
mosquitto_sub --help
```

## 14. Windows Firewall Blocks Ports

Check:

```powershell
Test-NetConnection 127.0.0.1 -Port 1883
Test-NetConnection 127.0.0.1 -Port 1884
```

If using WSL or another host, test the actual broker IP instead of `127.0.0.1`.
