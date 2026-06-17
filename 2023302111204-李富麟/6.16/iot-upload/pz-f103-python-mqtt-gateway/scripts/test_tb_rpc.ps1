$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($env:TB_ACCESS_TOKEN)) {
    Write-Error "TB_ACCESS_TOKEN is not set. Run: `$env:TB_ACCESS_TOKEN='xxx'; .\scripts\test_tb_rpc.ps1"
    exit 1
}

$HostName = if ($env:TB_MQTT_HOST) { $env:TB_MQTT_HOST } else { "127.0.0.1" }
$Port = if ($env:TB_MQTT_PORT) { [int]$env:TB_MQTT_PORT } else { 1884 }

mosquitto_pub -h $HostName -p $Port -u "$env:TB_ACCESS_TOKEN" -t v1/devices/me/rpc/request/1 -m '{"method":"setLed","params":"ON"}'
Start-Sleep -Milliseconds 800
mosquitto_pub -h $HostName -p $Port -u "$env:TB_ACCESS_TOKEN" -t v1/devices/me/rpc/request/2 -m '{"method":"setLed","params":"OFF"}'
Start-Sleep -Milliseconds 800
mosquitto_pub -h $HostName -p $Port -u "$env:TB_ACCESS_TOKEN" -t v1/devices/me/rpc/request/3 -m '{"method":"setBuzzer","params":"ON"}'
Start-Sleep -Milliseconds 800
mosquitto_pub -h $HostName -p $Port -u "$env:TB_ACCESS_TOKEN" -t v1/devices/me/rpc/request/4 -m '{"method":"setBuzzer","params":"OFF"}'
Start-Sleep -Milliseconds 800
mosquitto_pub -h $HostName -p $Port -u "$env:TB_ACCESS_TOKEN" -t v1/devices/me/rpc/request/5 -m '{"method":"getStatus","params":{}}'
