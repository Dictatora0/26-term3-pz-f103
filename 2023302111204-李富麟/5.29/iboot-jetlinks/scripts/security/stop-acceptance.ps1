$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Wait-ServiceStopped {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [int]$TimeoutSeconds = 120
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        $service = Get-Service -Name $Name -ErrorAction Stop
        if ($service.Status -eq "Stopped") {
            return
        }
        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $deadline)

    throw "Service did not stop in time: $Name"
}

function Stop-AndWaitService {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [int]$TimeoutSeconds = 120
    )

    $service = Get-Service -Name $Name -ErrorAction Stop
    if ($service.Status -ne "Stopped") {
        Stop-Service -Name $Name -ErrorAction SilentlyContinue
    }
    Wait-ServiceStopped -Name $Name -TimeoutSeconds $TimeoutSeconds
}

Write-Step "Stop services in reverse order"
Stop-AndWaitService -Name "iboot-jetlinks-service" -TimeoutSeconds 120
Stop-AndWaitService -Name "jetlinks-standalone-service" -TimeoutSeconds 180
Stop-AndWaitService -Name "nginx-service" -TimeoutSeconds 60
Stop-AndWaitService -Name "mosquitto" -TimeoutSeconds 60
Stop-AndWaitService -Name "elasticsearch-service-x64" -TimeoutSeconds 180
Stop-AndWaitService -Name "redis-service" -TimeoutSeconds 120
Stop-AndWaitService -Name "MySQL" -TimeoutSeconds 180

Write-Step "Current service status"
Get-Service MySQL,redis-service,elasticsearch-service-x64,nginx-service,jetlinks-standalone-service,iboot-jetlinks-service,mosquitto |
    Select-Object Status,Name,StartType |
    Format-Table -AutoSize

Write-Step "Remaining listening ports"
Get-NetTCPConnection -State Listen |
    Where-Object { $_.LocalPort -in 8443,8848,9000,1883,3306,6379,9200 } |
    Select-Object LocalAddress,LocalPort,OwningProcess,State |
    Sort-Object LocalPort |
    Format-Table -AutoSize

Write-Host ""
Write-Host "Acceptance environment stopped." -ForegroundColor Green
