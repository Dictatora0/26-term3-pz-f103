if ([string]::IsNullOrWhiteSpace($env:CAR_TB_ACCESS_TOKEN)) {
  Write-Error "CAR_TB_ACCESS_TOKEN is not set. Example: `$env:CAR_TB_ACCESS_TOKEN=`"your_hi3861_car_token`""
  exit 1
}

mosquitto_pub -h 127.0.0.1 -p 1884 `
  -u "$env:CAR_TB_ACCESS_TOKEN" `
  -t v1/devices/me/telemetry `
  -m '{\"status\":\"RUNNING\",\"direction\":\"FORWARD\",\"speed\":60,\"distance_cm\":35.2}'
