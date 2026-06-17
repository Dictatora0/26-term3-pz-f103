$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($env:TB_ACCESS_TOKEN)) {
    Write-Error "TB_ACCESS_TOKEN is not set. Run: `$env:TB_ACCESS_TOKEN='xxx'; .\scripts\subscribe_tb_rpc_responses.ps1"
    exit 1
}

$HostName = if ($env:TB_MQTT_HOST) { $env:TB_MQTT_HOST } else { "127.0.0.1" }
$Port = if ($env:TB_MQTT_PORT) { [int]$env:TB_MQTT_PORT } else { 1884 }

mosquitto_sub -h $HostName -p $Port -u "$env:TB_ACCESS_TOKEN" -t "v1/devices/me/rpc/response/+" -v
