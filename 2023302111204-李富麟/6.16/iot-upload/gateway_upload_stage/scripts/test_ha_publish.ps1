$ErrorActionPreference = "Stop"

$HostName = if ($env:HA_MQTT_HOST) { $env:HA_MQTT_HOST } else { "127.0.0.1" }
$Port = if ($env:HA_MQTT_PORT) { [int]$env:HA_MQTT_PORT } else { 1883 }
$DeviceId = if ($env:DEVICE_ID) { $env:DEVICE_ID } else { "f103_01" }
$BaseTopic = "pz103/$DeviceId"

mosquitto_pub -h $HostName -p $Port -t "$BaseTopic/temperature" -m "26.5"
mosquitto_pub -h $HostName -p $Port -t "$BaseTopic/light" -m "73"
mosquitto_pub -h $HostName -p $Port -t "$BaseTopic/led/state" -m "OFF"
mosquitto_pub -h $HostName -p $Port -t "$BaseTopic/buzzer/state" -m "OFF"
