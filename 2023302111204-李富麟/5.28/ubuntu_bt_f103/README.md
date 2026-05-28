# STM32F103 + HC05 + Ubuntu MQTT Bridge

## 1. Active Lab Path

The active firmware path in this repository is now:

STM32F103 board
-> HC05 Bluetooth UART
-> Ubuntu bridge script
-> local Mosquitto broker
-> MQTT text/control topics

The older ESP8266 direct-MQTT source is still present for reference, but it is no longer the main path for this experiment.

Current machine note:

- the `Ubuntu` instance on this PC is `WSL2`
- `WSL2` on this machine has no Bluetooth controller passthrough
- so `bluetoothctl` / `rfcomm` cannot directly bind the HC05 inside WSL
- use WSL for `mosquitto`, and use Windows for the Bluetooth serial COM bridge

## 2. Firmware Behavior

The board now uses `USART3` as a Bluetooth text channel to Ubuntu.

Board-to-Ubuntu line format:

- `HELLO:{...}` on Bluetooth link-up
- `STATUS:{...}` for link or status events
- `ACK:{...}` for control replies
- `TEXT:<message>` for text payloads
- `TELEMETRY:{...}` for periodic sensor data

Ubuntu bridge behavior:

- subscribes MQTT `pz103/control` and forwards control text to the board
- subscribes MQTT `pz103/text/tx` and forwards it as `MSG:<text>`
- mirrors raw board lines to `pz103/bluetooth/rx`
- republishes board telemetry/status/text to dedicated MQTT topics

## 3. Wiring

HC05 to STM32F103:

- `HC05 TXD -> PB11` (`USART3_RX`)
- `HC05 RXD -> PB10` (`USART3_TX`)
- `HC05 STATE/LED -> PA15`
- `HC05 KEY/EN -> PA4`
- common `GND`
- stable `3.3V`

Notes:

- The firmware keeps `PA4` low in normal transparent UART mode.
- `PA15` is sampled to detect Bluetooth link state.

## 4. Firmware Topics and Commands

Default MQTT topics:

- `pz103/control`
- `pz103/telemetry`
- `pz103/status`
- `pz103/text/tx`
- `pz103/text/rx`
- `pz103/bluetooth/tx`
- `pz103/bluetooth/rx`

Supported board commands over Bluetooth/MQTT:

- `PING`
- `LED_ON`
- `LED_OFF`
- `STATUS`
- `MSG:<text>`
- plain text lines are also echoed back as `TEXT:<text>`

## 5. Ubuntu Side

Install broker and verify it locally:

```bash
chmod +x scripts/ubuntu/verify_mqtt.sh
./scripts/ubuntu/verify_mqtt.sh
```

Install Python dependencies:

```bash
pip install -r scripts/ubuntu/requirements.txt
```

Run the bridge:

```bash
python3 scripts/ubuntu/bt_mqtt_bridge.py --device /dev/rfcomm0
```

If you are on this machine's WSL2 environment, use the Windows helper instead:

```powershell
.\scripts\windows\start_bt_mqtt_bridge.ps1 -ListPorts
.\scripts\windows\start_bt_mqtt_bridge.ps1 -ComPort COM7
```

Detailed Ubuntu setup is documented in:

- `docs/UBUNTU_BT_MQTT_BRIDGE.md`

## 6. Build

Toolchain:

- Keil uVision / ARMCC5
- Device: `STM32F103ZE`

Build steps:

1. Open `F103_MQTT_REBUILD.uvprojx`
2. Build and flash the board
3. Open `USART1` at `115200`
4. Pair the HC05 in Ubuntu and expose it as `/dev/rfcomm0`
5. Run the bridge script and publish MQTT text/control messages

## 7. Legacy Reference

Legacy files kept for reference:

- `APP/esp8266/wifi_function.c`
- `docs/WSL_MQTT_DEPLOY.md`
- `docs/ESP8266_AT_BRINGUP.md`

Those documents describe the older ESP8266 direct-MQTT path rather than the current Bluetooth bridge path.
