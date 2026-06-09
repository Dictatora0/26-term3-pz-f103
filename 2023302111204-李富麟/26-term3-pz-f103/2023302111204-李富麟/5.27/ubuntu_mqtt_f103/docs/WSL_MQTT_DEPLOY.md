# Ubuntu/WSL MQTT Deployment Guide

## 1. Target Topology

Ubuntu/WSL Mosquitto  
-> Windows host LAN IP `:1883`  
-> `netsh interface portproxy` forwards to Ubuntu/WSL Mosquitto  
-> ESP8266 AT MQTT client  
-> STM32F103 board

Optional only:

- a phone MQTT app can also attach to this broker later
- it is not required for the current experiment

Use this IP split consistently:

| Role | Example | Who can use it |
| --- | --- | --- |
| WSL internal IP | `172.xx.xx.xx` | Windows host, WSL shell |
| Windows LAN IP | `192.168.1.100` | Phone, ESP8266, other LAN devices |
| `127.0.0.1` | loopback | Only the same machine / same stack |

For this lab:

- `MQTT_HOST` in `User/esp8266_mqtt_config.h` must be the Windows LAN IP.
- Do not set the broker host to `127.0.0.1`.
- Do not set the broker host to the changing WSL2 NAT IP unless all clients run on the same PC.

Current machine-specific hotspot values:

- Hotspot SSID: `DESKTOP-6NM70T`
- Hotspot password: `LFL-lab-204`
- Windows hotspot adapter IP: `192.168.137.1`
- Current WSL IP: `172.29.37.36`

If the ESP8266 joins this hotspot, it must use `192.168.137.1` as the MQTT host.

## 2. Install Mosquitto in Ubuntu/WSL

Use the repo script:

```bash
chmod +x scripts/wsl/install_mosquitto.sh
./scripts/wsl/install_mosquitto.sh
```

Fast verification after install:

```bash
chmod +x scripts/wsl/verify_mqtt.sh
./scripts/wsl/verify_mqtt.sh
```

Equivalent manual commands:

```bash
sudo apt update
sudo apt install -y mosquitto mosquitto-clients
sudo install -m 644 config/mosquitto/mosquitto.conf /etc/mosquitto/conf.d/ubuntu_mqtt_f103.conf
sudo service mosquitto restart
sudo service mosquitto status
ss -lntp | grep 1883
```

The Mosquitto template used by this project is:

```conf
listener 1883 0.0.0.0
allow_anonymous true
```

## 3. Verify Broker Inside WSL

Open two WSL terminals.

Terminal A:

```bash
mosquitto_sub -h 127.0.0.1 -p 1883 -t "test/#" -v
```

Terminal B:

```bash
mosquitto_pub -h 127.0.0.1 -p 1883 -t "test/ping" -m "hello"
```

Expected result in Terminal A:

```text
test/ping hello
```

## 4. Expose WSL Mosquitto to ESP8266

### Option A: WSL2 default NAT mode

This is the common case. LAN devices cannot directly reach the WSL2 IP, so forward Windows `1883` to WSL `1883`.

Run PowerShell as Administrator:

```powershell
.\scripts\windows\setup_wsl_mqtt_portproxy.ps1
```

Important conflict check:

- If Windows also runs a native `Mosquitto Broker` service on port `1883`, ESP8266 may connect to the Windows broker instead of the WSL broker.
- In that case, STM32 logs may show publish success, but `mosquitto_sub` inside WSL will still see nothing.

Stop the Windows broker first:

```powershell
.\scripts\windows\stop_windows_mosquitto.ps1
.\scripts\windows\setup_wsl_mqtt_portproxy.ps1
```

Equivalent manual commands:

```powershell
$wslIp = (wsl -d Ubuntu -- bash -lc "hostname -I | awk '{print \$1}'").Trim()
netsh interface portproxy delete v4tov4 listenaddress=0.0.0.0 listenport=1883
netsh interface portproxy add v4tov4 listenaddress=0.0.0.0 listenport=1883 connectaddress=$wslIp connectport=1883
New-NetFirewallRule -DisplayName "WSL MQTT 1883 Portproxy" -Direction Inbound -Action Allow -Protocol TCP -LocalPort 1883
netsh interface portproxy show v4tov4
```

Verify on Windows:

```powershell
Test-NetConnection 127.0.0.1 -Port 1883
```

Then find the Windows LAN IP with `ipconfig` and use that IP on the ESP8266.

### Option B: WSL mirrored networking

If your WSL version supports mirrored networking and it is enabled, WSL may be directly reachable on the LAN without `portproxy`.

Even then:

- verify that Mosquitto is listening on `0.0.0.0:1883`
- confirm phone and ESP8266 can really open `1883`
- keep the Windows LAN IP / WSL IP distinction clear in your notes

Do not assume mirrored networking is enabled unless you verified it on your machine.

## 5. Firmware Settings

Edit `User/esp8266_mqtt_config.h`:

```c
#define WIFI_SSID              "DESKTOP-6NM70T"
#define WIFI_PASSWORD          "LFL-lab-204"
#define MQTT_HOST              "192.168.137.1"
#define MQTT_PORT              1883
#define MQTT_USERNAME          ""
#define MQTT_PASSWORD          ""
#define MQTT_CLIENT_ID         "pz103_f103_esp8266"
#define MQTT_TOPIC_TELEMETRY   "pz103/telemetry"
#define MQTT_TOPIC_CONTROL     "pz103/control"
#define MQTT_TOPIC_STATUS      "pz103/status"
```

Notes:

- `MQTT_HOST` = Windows LAN IP, not WSL IP, not localhost.
- ESP8266 must connect to a `2.4 GHz` Wi-Fi network.
- Default auth is anonymous for fast bring-up.
- DHT11 is optional. If not wired, keep `SENSOR_ENABLE_DHT11 0`.

## 6. Ubuntu-to-Board MQTT Test

The current experiment can be completed without any phone app.

Terminal A:

```bash
mosquitto_sub -h 127.0.0.1 -p 1883 -t "pz103/#" -v
```

Terminal B:

```bash
mosquitto_pub -h 127.0.0.1 -p 1883 -t "pz103/control" -m '{"cmd":"ping"}'
mosquitto_pub -h 127.0.0.1 -p 1883 -t "pz103/control" -m '{"led":1}'
mosquitto_pub -h 127.0.0.1 -p 1883 -t "pz103/control" -m '{"led":0}'
```

Expected:

- telemetry appears on `pz103/telemetry`
- replies appear on `pz103/status`
- board logs show the received control payload

## 7. Phone MQTT App Setup

This section is optional now.

Any of these apps can be used:

- MQTTX
- MQTT Dash
- IoT MQTT Panel

Recommended settings:

- Host: Windows hotspot IP `192.168.137.1`
- Port: `1883`
- Client ID: `phone_client`
- Username: empty
- Password: empty

Subscribe:

- `pz103/telemetry`
- `pz103/status`

Publish:

- Topic: `pz103/control`

Sample payloads:

```json
{"led":1}
```

```json
{"led":0}
```

```json
{"cmd":"ping"}
```

Legacy raw text commands are also accepted:

- `LED_ON`
- `LED_OFF`
- `PING`

## 8. Expected Board Logs

Typical USART1 logs after power-up:

```text
[MQTT_CFG] Broker=192.168.1.100:1883
[ESP] WiFi connected
[MQTT] connected to 192.168.1.100:1883
[MQTT] subscribed: pz103/control
[MQTT] online status published: pz103/status
```

On publish:

```text
[MQTT] publish topic=pz103/telemetry payload={"device":"pz103-f103","seq":1,"temperature_c":25.40}
```

On control message:

```text
[MQTT] recv topic=pz103/control payload={"led":1}
[MQTT] control result: LED ON
```

## 9. Troubleshooting

| Symptom | Check |
| --- | --- |
| Optional phone cannot connect to broker | Confirm it uses Windows LAN IP, not `127.0.0.1`; verify Windows firewall rule and `Test-NetConnection` |
| WSL works locally but board cannot access | `portproxy` missing, wrong WSL IP after reboot, or firewall blocked |
| STM32 shows publish OK but WSL subscriber sees nothing | Windows native `mosquitto.exe` is likely listening on `1883`; stop it and reapply `portproxy` |
| ESP8266 AT no response | This is upstream of WiFi/MQTT/phone. Verify power supply, baud rate, TX/RX wiring, common GND, EN high, RST high, and ESP-AT firmware |
| WiFi join fails | SSID must be 2.4 GHz; verify password and signal strength |
| MQTT connect fails | `MQTT_HOST` may still point to wrong IP; broker may not be listening on `0.0.0.0:1883` |
| Subscribe succeeds but no control message arrives | `mosquitto_pub` published to wrong topic, wrong broker IP, or ESP8266 is not on a reachable network |
| Telemetry payload looks wrong | Check USART1 logs; current firmware publishes JSON and omits unsupported light data |
| `portproxy` stops working after reboot | WSL2 IP changed; rerun `scripts/windows/setup_wsl_mqtt_portproxy.ps1` |
| `MQTTDISCONNECTED` appears repeatedly | Broker unstable, Wi-Fi dropped, or Windows forwarding broke |

## 10. Remove the Port Forward

If you want to clean up the Windows forwarding rule:

```powershell
.\scripts\windows\remove_wsl_mqtt_portproxy.ps1
```
