$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($env:TB_ACCESS_TOKEN)) {
    Write-Error "TB_ACCESS_TOKEN is not set. Run: `$env:TB_ACCESS_TOKEN='xxx'; .\scripts\test_tb_publish.ps1"
    exit 1
}

$HostName = if ($env:TB_MQTT_HOST) { $env:TB_MQTT_HOST } else { "127.0.0.1" }
$Port = if ($env:TB_MQTT_PORT) { [int]$env:TB_MQTT_PORT } else { 1884 }
$Payload = '{"temperature":26.5,"light":73,"led":"OFF","buzzer":"OFF"}'

mosquitto_pub -h $HostName -p $Port `
  -u "$env:TB_ACCESS_TOKEN" `
  -t v1/devices/me/telemetry `
  -m $Payload
