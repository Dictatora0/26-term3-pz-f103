#Requires -RunAsAdministrator
[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"

$svc = Get-Service mosquitto -ErrorAction SilentlyContinue
if ($null -eq $svc) {
    Write-Host "[INFO] Windows mosquitto service not installed."
    exit 0
}

if ($svc.Status -eq 'Running') {
    Write-Host "[INFO] Stopping Windows mosquitto service"
    Stop-Service mosquitto -Force
}

$svc = Get-Service mosquitto -ErrorAction SilentlyContinue
Write-Host "[INFO] Windows mosquitto service status: $($svc.Status)"
Write-Host "[NEXT] Reapply WSL portproxy if needed:"
Write-Host "  powershell -ExecutionPolicy Bypass -File .\\scripts\\windows\\setup_wsl_mqtt_portproxy.ps1"
