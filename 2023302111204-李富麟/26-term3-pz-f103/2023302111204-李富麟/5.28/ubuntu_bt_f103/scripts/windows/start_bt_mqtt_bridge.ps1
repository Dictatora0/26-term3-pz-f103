param(
    [string]$ComPort = "",
    [string]$PythonExe = "python",
    [string]$BrokerHost = "127.0.0.1",
    [int]$BrokerPort = 1883,
    [string]$ClientId = "windows_bt_bridge",
    [switch]$ListPorts
)

$ErrorActionPreference = "Stop"

function Get-BluetoothSerialPorts {
    $ports = @(Get-CimInstance Win32_SerialPort -ErrorAction SilentlyContinue |
        Where-Object {
            $_.PNPDeviceID -like "BTHENUM\\*" -or
            $_.Description -like "*Bluetooth*" -or
            $_.Description -like "*Serial over Bluetooth*" -or
            $_.Name -like "*Bluetooth*" -or
            $_.Name -like "*标准串行*"
        } |
        Select-Object DeviceID, Name, Description, PNPDeviceID)

    return $ports
}

$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$bridgeScript = Join-Path $repoRoot "scripts\ubuntu\bt_mqtt_bridge.py"

if (-not (Test-Path $bridgeScript)) {
    throw "Bridge script not found: $bridgeScript"
}

$ports = Get-BluetoothSerialPorts

if ($ListPorts) {
    if ($ports.Count -eq 0) {
        Write-Host "No Bluetooth serial COM ports found."
        Write-Host "Pair HC05 in Windows first, then create an outgoing 'Standard Serial over Bluetooth link (COMx)'."
        exit 1
    }

    $ports | Format-Table -AutoSize
    exit 0
}

if ([string]::IsNullOrWhiteSpace($ComPort)) {
    if ($ports.Count -eq 1) {
        $ComPort = $ports[0].DeviceID
        Write-Host "Using detected Bluetooth COM port: $ComPort"
    }
    elseif ($ports.Count -gt 1) {
        Write-Host "Multiple Bluetooth COM ports found:"
        $ports | Format-Table -AutoSize
        throw "Please rerun with -ComPort COMx"
    }
    else {
        throw "No Bluetooth COM port found. Pair HC05 and create a Bluetooth serial port first."
    }
}

$command = @(
    $bridgeScript,
    "--device", $ComPort,
    "--broker-host", $BrokerHost,
    "--broker-port", $BrokerPort,
    "--client-id", $ClientId
)

Write-Host "Starting Bluetooth-MQTT bridge..."
Write-Host "$PythonExe $($command -join ' ')"

& $PythonExe @command
