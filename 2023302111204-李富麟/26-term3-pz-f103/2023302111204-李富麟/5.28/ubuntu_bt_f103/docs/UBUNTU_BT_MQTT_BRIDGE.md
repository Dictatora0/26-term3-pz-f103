# Ubuntu Bluetooth MQTT Bridge Guide

## 1. Goal

Run this chain on Ubuntu:

STM32F103
-> HC05 Bluetooth serial
-> `scripts/ubuntu/bt_mqtt_bridge.py`
-> Mosquitto
-> MQTT publishers/subscribers

Important for this PC:

- the detected Ubuntu environment is `WSL2`
- this WSL2 instance currently has no `/sys/class/bluetooth`
- it also has no `/dev/rfcomm0`
- therefore the HC05 cannot be paired with `bluetoothctl` directly inside this WSL2 environment
- keep Mosquitto in WSL2, but move the Bluetooth serial bridge to Windows COM

Windows-side helper added in this repo:

```powershell
.\scripts\windows\start_bt_mqtt_bridge.ps1 -ListPorts
.\scripts\windows\start_bt_mqtt_bridge.ps1 -ComPort COM7
```

## 2. Pair the HC05

Use `bluetoothctl`:

```bash
bluetoothctl
power on
agent on
default-agent
scan on
pair XX:XX:XX:XX:XX:XX
trust XX:XX:XX:XX:XX:XX
connect XX:XX:XX:XX:XX:XX
quit
```

Bind it as a serial device:

```bash
sudo rfcomm bind 0 XX:XX:XX:XX:XX:XX 1
ls -l /dev/rfcomm0
```

If the device was already bound and needs to be reset:

```bash
sudo rfcomm release 0
sudo rfcomm bind 0 XX:XX:XX:XX:XX:XX 1
```

## 3. Start Mosquitto

If Mosquitto is not installed yet:

```bash
sudo apt update
sudo apt install -y mosquitto mosquitto-clients python3-pip
```

Verify the broker:

```bash
chmod +x scripts/ubuntu/verify_mqtt.sh
./scripts/ubuntu/verify_mqtt.sh
```

## 4. Install Bridge Dependencies

```bash
pip install -r scripts/ubuntu/requirements.txt
```

## 5. Run the Bridge

```bash
python3 scripts/ubuntu/bt_mqtt_bridge.py --device /dev/rfcomm0
```

Useful options:

```bash
python3 scripts/ubuntu/bt_mqtt_bridge.py \
  --device /dev/rfcomm0 \
  --broker-host 127.0.0.1 \
  --broker-port 1883 \
  --log-level DEBUG
```

## 6. MQTT Test Commands

Monitor everything:

```bash
mosquitto_sub -h 127.0.0.1 -p 1883 -t "pz103/#" -v
```

Send control commands:

```bash
mosquitto_pub -h 127.0.0.1 -p 1883 -t "pz103/control" -m "PING"
mosquitto_pub -h 127.0.0.1 -p 1883 -t "pz103/control" -m "LED_ON"
mosquitto_pub -h 127.0.0.1 -p 1883 -t "pz103/control" -m "LED_OFF"
```

Send a text message:

```bash
mosquitto_pub -h 127.0.0.1 -p 1883 -t "pz103/text/tx" -m "hello from ubuntu over bluetooth"
```

Raw pass-through:

```bash
mosquitto_pub -h 127.0.0.1 -p 1883 -t "pz103/bluetooth/tx" -m "STATUS"
```

## 7. Expected Topic Flow

Board publishes:

- `pz103/telemetry` from `TELEMETRY:{...}`
- `pz103/status` from `STATUS:{...}` and `ACK:{...}`
- `pz103/text/rx` from `TEXT:<message>`
- `pz103/bluetooth/rx` as a raw mirror of every Bluetooth line

Bridge subscribes:

- `pz103/control`
- `pz103/text/tx`
- `pz103/bluetooth/tx`

## 8. Troubleshooting

- `/dev/rfcomm0` missing: pair and bind the HC05 again.
- Bridge opens serial but no board data arrives: check `PB10/PB11`, baud `9600`, GND, and HC05 power.
- MQTT works but control has no effect: watch `pz103/bluetooth/rx` and `USART1` logs to confirm the board received the line.
- `PA15` never reports connected: verify the HC05 `STATE`/`LED` pin really reaches `PA15`.
- If the board keeps sending telemetry but Ubuntu sees nothing: the HC05 link is not connected even if power is present.
