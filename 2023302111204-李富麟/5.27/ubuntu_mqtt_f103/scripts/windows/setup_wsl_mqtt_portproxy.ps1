#Requires -RunAsAdministrator
[CmdletBinding()]
param(
    [string]$DistroName = "Ubuntu",
    [int]$ListenPort = 1883,
    [string]$ListenAddress = "0.0.0.0",
    [string]$FirewallRuleName = "WSL MQTT 1883 Portproxy",
    [switch]$StopWindowsMosquitto
)

$ErrorActionPreference = "Stop"

$winMosquittoService = Get-Service mosquitto -ErrorAction SilentlyContinue
if ($null -ne $winMosquittoService -and $winMosquittoService.Status -eq 'Running') {
    if ($StopWindowsMosquitto) {
        Write-Host "[INFO] Stopping Windows mosquitto service so portproxy can own port $ListenPort"
        Stop-Service mosquitto -Force
        $winMosquittoService = Get-Service mosquitto -ErrorAction SilentlyContinue
        Write-Host "[INFO] Windows mosquitto service status: $($winMosquittoService.Status)"
    } else {
        Write-Warning "Windows mosquitto service is running. It can occupy TCP $ListenPort and prevent WSL portproxy from being used."
        Write-Warning "If your STM32 publishes successfully but WSL mosquitto_sub sees nothing, stop the Windows broker first:"
        Write-Warning "  powershell -ExecutionPolicy Bypass -File .\\scripts\\windows\\stop_windows_mosquitto.ps1"
    }
}

Write-Host "[INFO] Querying WSL IP from distro: $DistroName"
$wslIpRaw = wsl -d $DistroName -- hostname -I
if ($LASTEXITCODE -ne 0) {
    throw "Unable to query WSL IP from distro $DistroName."
}
$wslIp = (($wslIpRaw -split '\s+') | Where-Object { $_ -match '^\d+\.\d+\.\d+\.\d+$' } | Select-Object -First 1)
if ([string]::IsNullOrWhiteSpace($wslIp)) {
    throw "Unable to resolve WSL IP. Start the distro first, then rerun this script."
}

$lanIps = Get-NetIPAddress -AddressFamily IPv4 |
    Where-Object {
        $_.IPAddress -notmatch '^127\.' -and
        $_.IPAddress -notmatch '^169\.254\.' -and
        $_.InterfaceAlias -notmatch 'Loopback' -and
        $_.InterfaceAlias -notmatch 'vEthernet \(WSL\)'
    } |
    Select-Object -ExpandProperty IPAddress -Unique

Write-Host "[INFO] WSL IP: $wslIp"
if ($lanIps) {
    Write-Host "[INFO] Windows LAN IP(s): $($lanIps -join ', ')"
} else {
    Write-Host "[WARN] No Windows LAN IP was auto-detected. Check ipconfig manually."
}

Write-Host "[INFO] Replacing portproxy rule $ListenAddress`:$ListenPort -> $wslIp`:$ListenPort"
& netsh interface portproxy delete v4tov4 listenaddress=$ListenAddress listenport=$ListenPort | Out-Null
& netsh interface portproxy add v4tov4 listenaddress=$ListenAddress listenport=$ListenPort connectaddress=$wslIp connectport=$ListenPort | Out-Null

$fwRule = Get-NetFirewallRule -DisplayName $FirewallRuleName -ErrorAction SilentlyContinue
if ($null -eq $fwRule) {
    New-NetFirewallRule `
        -DisplayName $FirewallRuleName `
        -Direction Inbound `
        -Action Allow `
        -Protocol TCP `
        -LocalPort $ListenPort | Out-Null
    Write-Host "[INFO] Created firewall rule: $FirewallRuleName"
} else {
    Set-NetFirewallRule -DisplayName $FirewallRuleName -Enabled True -Profile Any -Action Allow | Out-Null
    Write-Host "[INFO] Reused firewall rule: $FirewallRuleName"
}

Write-Host "[INFO] Current portproxy rules:"
& netsh interface portproxy show v4tov4

$listenOwners = Get-NetTCPConnection -LocalPort $ListenPort -ErrorAction SilentlyContinue |
    Where-Object { $_.State -eq 'Listen' } |
    Select-Object -ExpandProperty OwningProcess -Unique
if ($listenOwners) {
    $listenProcesses = Get-Process -Id $listenOwners -ErrorAction SilentlyContinue | Select-Object Id, ProcessName, Path
    Write-Host "[INFO] Current Windows listen owner(s) for TCP $ListenPort:"
    $listenProcesses | Format-Table -AutoSize | Out-String | Write-Host
}

Write-Host "[INFO] Windows-side quick test:"
Write-Host "  Test-NetConnection 127.0.0.1 -Port $ListenPort"

if ($lanIps) {
    Write-Host "[INFO] Phone and ESP8266 should use one of these Windows LAN IPs as MQTT host:"
    $lanIps | ForEach-Object { Write-Host "  $_" }
}

Write-Host "[INFO] If WSL IP changes after reboot, rerun this script."
