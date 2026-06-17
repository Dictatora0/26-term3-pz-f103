$ErrorActionPreference = "Stop"

$HostName = if ($env:HA_MQTT_HOST) { $env:HA_MQTT_HOST } else { "127.0.0.1" }
$Port = if ($env:HA_MQTT_PORT) { [int]$env:HA_MQTT_PORT } else { 1883 }
$DeviceId = if ($env:DEVICE_ID) { $env:DEVICE_ID } else { "f103_01" }

mosquitto_sub -h $HostName -p $Port -t "pz103/$DeviceId/#" -v
