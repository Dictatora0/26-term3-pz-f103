# Troubleshooting

## 1. Serial ports

List available ports:

```powershell
python scripts\list_serial_ports.py
```

Common issues:

- `F103_SERIAL_PORT` or `CAR_SERIAL_PORT` is wrong
- COM port is occupied by Keil, serial assistant, MQTTX, or another program
- baud rate does not match device firmware

## 2. Gateway logs

Start the gateway:

```powershell
python gateway.py
```

Expected markers:

- F103: `[SERIAL] received raw line: ...`
- Hi3861 Car: `[CAR] raw ...`
- Hi3861 Car parsed: `[CAR] parsed ...`

If a line is not `TEMP:` / JSON / `CAR:`, it is logged only and not published.

## 3. F103 parsing problems

- line must start with `TEMP:`
- fields with invalid numbers are ignored
- `LED` / `BUZZER` only accept `ON` / `OFF`

## 4. Hi3861 Car parsing problems

Supported formats:

```text
{"device_id":"hi3861_car_01","type":"car","status":"RUNNING","direction":"FORWARD","speed":60,"distance_cm":35.2}
CAR:hi3861_car_01,STATUS:RUNNING,DIR:FORWARD,SPEED:60,DIST:35.2
```

Current car telemetry does not report:

- `battery_voltage`
- `battery_percent`

If `distance_cm` is missing in a cycle, the gateway still forwards the other car fields.

## 5. Home Assistant MQTT verification

F103:

```powershell
mosquitto_sub -h 127.0.0.1 -p 1883 -t "pz103/f103_01/#" -v
```

Hi3861 Car:

```powershell
mosquitto_sub -h 127.0.0.1 -p 1883 -t "iot/hi3861_car_01/#" -v
```

Check:

- Mosquitto is running
- Home Assistant MQTT integration points to the correct broker
- Home Assistant in WSL can actually reach the broker host

## 6. ThingsBoard verification

Check MQTT endpoint:

```powershell
Test-NetConnection 127.0.0.1 -Port 1884
```

If your local listener is not on `1884`, update `.env`.

Test Hi3861 car telemetry:

```powershell
$env:CAR_TB_ACCESS_TOKEN="your_real_token"
.\scripts\test_hi3861_car_tb_publish.ps1
```

Check:

```text
Devices -> Huawei Hi3861 Car -> Latest telemetry
```

## 7. PowerShell and command tools

If scripts cannot run:

```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```

If `mosquitto_pub` / `mosquitto_sub` do not exist, install Mosquitto client tools or add them to `PATH`.

## 8. Network and firewall

- Windows firewall may block broker ports
- WSL to Windows routing may differ from `127.0.0.1`
- verify `1883` and `1884` are actually listening before debugging the gateway itself
