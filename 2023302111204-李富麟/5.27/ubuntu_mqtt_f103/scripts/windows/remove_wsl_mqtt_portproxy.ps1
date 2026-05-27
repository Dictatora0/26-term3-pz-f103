#Requires -RunAsAdministrator
[CmdletBinding()]
param(
    [int]$ListenPort = 1883,
    [string]$ListenAddress = "0.0.0.0",
    [string]$FirewallRuleName = "WSL MQTT 1883 Portproxy",
    [switch]$KeepFirewallRule
)

$ErrorActionPreference = "Stop"

Write-Host "[INFO] Removing portproxy rule $ListenAddress`:$ListenPort"
& netsh interface portproxy delete v4tov4 listenaddress=$ListenAddress listenport=$ListenPort | Out-Null
& netsh interface portproxy show v4tov4

if (-not $KeepFirewallRule) {
    $fwRule = Get-NetFirewallRule -DisplayName $FirewallRuleName -ErrorAction SilentlyContinue
    if ($null -ne $fwRule) {
        Remove-NetFirewallRule -DisplayName $FirewallRuleName | Out-Null
        Write-Host "[INFO] Removed firewall rule: $FirewallRuleName"
    } else {
        Write-Host "[INFO] Firewall rule not found: $FirewallRuleName"
    }
} else {
    Write-Host "[INFO] Firewall rule preserved: $FirewallRuleName"
}
