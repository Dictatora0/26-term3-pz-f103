$ErrorActionPreference = "Stop"

$DeviceId = if ($env:DEVICE_ID) { $env:DEVICE_ID } else { "f103_01" }
$HostName = if ($env:HA_MQTT_HOST) { $env:HA_MQTT_HOST } else { "127.0.0.1" }
$Port = if ($env:HA_MQTT_PORT) { [int]$env:HA_MQTT_PORT } else { 1883 }
$BaseTopic = "pz103/$DeviceId"

mosquitto_pub -h $HostName -p $Port -t "$BaseTopic/led/set" -m "ON"
Start-Sleep -Milliseconds 500
mosquitto_pub -h $HostName -p $Port -t "$BaseTopic/led/set" -m "OFF"
Start-Sleep -Milliseconds 500
mosquitto_pub -h $HostName -p $Port -t "$BaseTopic/buzzer/set" -m "ON"
Start-Sleep -Milliseconds 500
mosquitto_pub -h $HostName -p $Port -t "$BaseTopic/buzzer/set" -m "OFF"
