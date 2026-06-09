$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Wait-ServiceRunning {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [int]$TimeoutSeconds = 120
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        $service = Get-Service -Name $Name -ErrorAction Stop
        if ($service.Status -eq "Running") {
            return
        }
        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $deadline)

    throw "Service did not reach Running in time: $Name"
}

function Wait-PortReady {
    param(
        [Parameter(Mandatory = $true)][int]$Port,
        [string]$TargetHost = "127.0.0.1",
        [int]$TimeoutSeconds = 120
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        try {
            $test = Test-NetConnection $TargetHost -Port $Port -WarningAction SilentlyContinue
            if ($test.TcpTestSucceeded) {
                return
            }
        } catch {
        }
        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $deadline)

    throw "Port did not become ready in time: $TargetHost`:$Port"
}

function Start-AndWaitService {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [int]$TimeoutSeconds = 120
    )

    $service = Get-Service -Name $Name -ErrorAction Stop
    if ($service.Status -ne "Running") {
        Start-Service -Name $Name
    }
    Wait-ServiceRunning -Name $Name -TimeoutSeconds $TimeoutSeconds
}

function Assert-FileContains {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Pattern,
        [Parameter(Mandatory = $true)][string]$ErrorMessage
    )

    if (!(Test-Path -LiteralPath $Path)) {
        throw "Missing file: $Path"
    }

    $content = Get-Content -LiteralPath $Path -Raw
    if ($content -notmatch [regex]::Escape($Pattern)) {
        throw $ErrorMessage
    }
}

function Assert-CommandOutputContains {
    param(
        [Parameter(Mandatory = $true)][string]$Command,
        [Parameter(Mandatory = $true)][string]$Pattern,
        [Parameter(Mandatory = $true)][string]$ErrorMessage
    )

    $output = Invoke-Expression $Command | Out-String
    if ($output -notmatch [regex]::Escape($Pattern)) {
        throw $ErrorMessage
    }
}

Write-Step "Start base services"
Start-AndWaitService -Name "MySQL" -TimeoutSeconds 120
Start-AndWaitService -Name "redis-service" -TimeoutSeconds 60
Start-AndWaitService -Name "elasticsearch-service-x64" -TimeoutSeconds 180

Write-Step "Wait for base ports"
Wait-PortReady -Port 3306 -TimeoutSeconds 60
Wait-PortReady -Port 6379 -TimeoutSeconds 60
Wait-PortReady -Port 9200 -TimeoutSeconds 180

Write-Step "Start app services"
Start-AndWaitService -Name "jetlinks-standalone-service" -TimeoutSeconds 180
Start-AndWaitService -Name "iboot-jetlinks-service" -TimeoutSeconds 120
Start-AndWaitService -Name "nginx-service" -TimeoutSeconds 60
Start-AndWaitService -Name "mosquitto" -TimeoutSeconds 60

Write-Step "Wait for app ports"
Wait-PortReady -Port 8848 -TimeoutSeconds 180
Wait-PortReady -Port 8443 -TimeoutSeconds 120
Wait-PortReady -Port 9000 -TimeoutSeconds 60
Wait-PortReady -Port 1883 -TimeoutSeconds 60

Write-Step "Verify nginx proxy configuration"
Assert-FileContains `
    -Path "C:\tools\nginx\conf.d\server.iboot.conf" `
    -Pattern "proxy_pass         https://127.0.0.1:8443;" `
    -ErrorMessage "nginx /api proxy is not pointing to https://127.0.0.1:8443"
Assert-FileContains `
    -Path "C:\tools\nginx\conf.d\server.iboot.conf" `
    -Pattern "proxy_ssl_verify   off;" `
    -ErrorMessage "nginx proxy_ssl_verify off is missing for local self-signed HTTPS"

Write-Step "Verify OAuth2 redirect whitelist"
Assert-CommandOutputContains `
    -Command "mysql -uroot -p123456 -D iboot -e `"SELECT allow_url FROM oauth2_app WHERE client_id='iboot-local-web';`"" `
    -Pattern "http://127.0.0.1/oauth/callback.html" `
    -ErrorMessage "OAuth2 client iboot-local-web is missing http://127.0.0.1/oauth/callback.html"

Write-Step "Current service status"
Get-Service MySQL,redis-service,elasticsearch-service-x64,nginx-service,jetlinks-standalone-service,iboot-jetlinks-service,mosquitto |
    Select-Object Status,Name,StartType |
    Format-Table -AutoSize

Write-Step "Current listening ports"
Get-NetTCPConnection -State Listen |
    Where-Object { $_.LocalPort -in 8443,8848,9000,1883,3306,6379,9200 } |
    Select-Object LocalAddress,LocalPort,OwningProcess,State |
    Sort-Object LocalPort |
    Format-Table -AutoSize

Write-Step "Verify web and HTTPS endpoints"
$ibootHome = Invoke-WebRequest -UseBasicParsing http://127.0.0.1/
$jetlinksAdmin = Invoke-WebRequest -UseBasicParsing http://127.0.0.1:9000/admin/index.html
curl.exe -k -I https://127.0.0.1:8443/api/oauth2/config

Write-Host ""
Write-Host "iBOOT home status: $($ibootHome.StatusCode)" -ForegroundColor Green
Write-Host "JetLinks admin status: $($jetlinksAdmin.StatusCode)" -ForegroundColor Green

Write-Step "MQTT listening status"
netstat -ano | findstr :1883

Write-Step "Next verification commands"
Write-Host "Security script: bash D:\Proj\5.29\iboot-jetlinks\scripts\security\test-security-flow.sh" -ForegroundColor Yellow
Write-Host "Admin security script: `$env:ADMIN_PASSWORD='your-admin-password'; bash D:\Proj\5.29\iboot-jetlinks\scripts\security\test-security-flow.sh" -ForegroundColor Yellow
Write-Host "iBOOT: http://127.0.0.1/" -ForegroundColor Yellow
Write-Host "JetLinks: http://127.0.0.1:9000/admin/index.html" -ForegroundColor Yellow
Write-Host "HTTPS: https://127.0.0.1:8443/api/oauth2/config" -ForegroundColor Yellow
Write-Host "Login callback whitelist: http://127.0.0.1/oauth/callback.html" -ForegroundColor Yellow

Write-Host ""
Write-Host "Acceptance environment started." -ForegroundColor Green
