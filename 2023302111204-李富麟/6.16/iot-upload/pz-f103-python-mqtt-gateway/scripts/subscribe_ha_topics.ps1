$ErrorActionPreference = "Stop"

$F103DeviceId = if ($env:F103_DEVICE_ID) { $env:F103_DEVICE_ID } elseif ($env:DEVICE_ID) { $env:DEVICE_ID } else { "f103_01" }
$CarDeviceId = if ($env:CAR_DEVICE_ID) { $env:CAR_DEVICE_ID } else { "hi3861_car_01" }
$HostName = if ($env:HA_MQTT_HOST) { $env:HA_MQTT_HOST } else { "127.0.0.1" }
$Port = if ($env:HA_MQTT_PORT) { [int]$env:HA_MQTT_PORT } else { 1883 }
$F103Topic = "pz103/$F103DeviceId/#"
$CarTopic = "iot/$CarDeviceId/#"

mosquitto_sub -h $HostName -p $Port -t $F103Topic -t $CarTopic -v
