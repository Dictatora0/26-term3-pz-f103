param(
    [string]$AuthBase = "https://127.0.0.1:8443/api",
    [string]$ClientId = "iboot-local-web",
    [string]$RedirectUri = "http://127.0.0.1/oauth/callback.html",
    [string]$Scopes = "device.read device.control user.manage",
    [string]$ViewerAccount = "viewer",
    [string]$ViewerPassword = "Viewer#2026",
    [string]$OperatorAccount = "operator",
    [string]$OperatorPassword = "Operator#2026",
    [string]$AdminAccount = "admin",
    [string]$AdminPassword = "",
    [string]$DeviceUrl = "https://127.0.0.1:8443/api/iot/panels/devices",
    [string]$LedUrl = "https://127.0.0.1:8443/api/iot/panels/switchCtrlStatus",
    [string]$AdminUrl = "https://127.0.0.1:8443/api/core/admin/view",
    [string]$CenterEditUrl = "https://127.0.0.1:8443/api/core/center/editUser",
    [string]$CenterPwdUrl = "https://127.0.0.1:8443/api/core/center/pwd",
    [string]$LedBody = '{"id":1832263091970150401,"status":"1"}',
    [string]$OutputDir = "D:\Proj\5.29\iboot-jetlinks\scripts\security\output",
    [switch]$Interactive,
    [switch]$MaskToken
)

$ErrorActionPreference = "Stop"
[System.Net.ServicePointManager]::ServerCertificateValidationCallback = { $true }

function Write-Section {
    param([string]$Title)
    Write-Host ""
    Write-Host ("=" * 80) -ForegroundColor DarkCyan
    Write-Host $Title -ForegroundColor Cyan
    Write-Host ("=" * 80) -ForegroundColor DarkCyan
}

function Write-Item {
    param(
        [string]$Label,
        [string]$Value,
        [ConsoleColor]$Color = [ConsoleColor]::Gray
    )

    Write-Host ("{0,-24}: {1}" -f $Label, $Value) -ForegroundColor $Color
}

function Pause-ForScreenshot {
    param([string]$Hint)
    if ($Interactive) {
        Write-Host ""
        Read-Host "截图后按 Enter 继续：$Hint" | Out-Null
    }
}

function New-PkcePair {
    $bytes = New-Object byte[] 48
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    $verifier = [Convert]::ToBase64String($bytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')

    $sha = [System.Security.Cryptography.SHA256]::Create()
    $challengeBytes = $sha.ComputeHash([System.Text.Encoding]::ASCII.GetBytes($verifier))
    $challenge = [Convert]::ToBase64String($challengeBytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')

    $stateBytes = New-Object byte[] 16
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($stateBytes)
    $state = [Convert]::ToBase64String($stateBytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')

    [pscustomobject]@{
        Verifier  = $verifier
        Challenge = $challenge
        State     = $state
    }
}

function ConvertFrom-Base64Url {
    param([Parameter(Mandatory = $true)][string]$InputString)

    $value = $InputString.Replace('-', '+').Replace('_', '/')
    while (($value.Length % 4) -ne 0) {
        $value += '='
    }

    [System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($value))
}

function Get-JwtPayload {
    param([Parameter(Mandatory = $true)][string]$Token)

    $parts = $Token.Split('.')
    if ($parts.Length -lt 2) {
        throw "非法 JWT"
    }

    (ConvertFrom-Base64Url -InputString $parts[1]) | ConvertFrom-Json
}

function Format-Token {
    param([string]$Token)

    if (-not $MaskToken) {
        return $Token
    }

    if ([string]::IsNullOrWhiteSpace($Token) -or $Token.Length -lt 24) {
        return $Token
    }

    return $Token.Substring(0, 16) + " ... " + $Token.Substring($Token.Length - 16)
}

function Convert-BodyToString {
    param(
        [AllowNull()]$Body,
        [string]$ContentType
    )

    if ($null -eq $Body) {
        return $null
    }

    if ($Body -is [string]) {
        return $Body
    }

    if ($ContentType -like "application/json*") {
        return ($Body | ConvertTo-Json -Compress -Depth 8)
    }

    return $Body
}

function Invoke-DemoRequest {
    param(
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Url,
        [hashtable]$Headers = @{},
        [AllowNull()]$Body = $null,
        [string]$ContentType = "application/json"
    )

    $requestBody = Convert-BodyToString -Body $Body -ContentType $ContentType

    try {
        if ($null -ne $requestBody) {
            $response = Invoke-WebRequest -UseBasicParsing -Method $Method -Uri $Url -Headers $Headers -Body $requestBody -ContentType $ContentType
        } else {
            $response = Invoke-WebRequest -UseBasicParsing -Method $Method -Uri $Url -Headers $Headers
        }

        [pscustomobject]@{
            HttpStatus = [int]$response.StatusCode
            Body       = $response.Content
            Json       = if ($response.Content) { try { $response.Content | ConvertFrom-Json } catch { $null } } else { $null }
        }
    } catch {
        if (-not $_.Exception.Response) {
            throw
        }

        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $body = $reader.ReadToEnd()

        [pscustomobject]@{
            HttpStatus = [int]$_.Exception.Response.StatusCode
            Body       = $body
            Json       = if ($body) { try { $body | ConvertFrom-Json } catch { $null } } else { $null }
        }
    }
}

function Get-OAuthToken {
    param(
        [Parameter(Mandatory = $true)][string]$Account,
        [Parameter(Mandatory = $true)][string]$Password,
        [Parameter(Mandatory = $true)][string]$RequestScopes
    )

    $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $pkce = New-PkcePair

    $loginResp = Invoke-RestMethod -UseBasicParsing -WebSession $session -Method Post -Uri "$AuthBase/oauth2/doLogin" -Body @{
        name = $Account
        pwd  = $Password
    }
    if ($loginResp.code -ne 200) {
        throw "OAuth2 登录失败：$Account"
    }

    $authorizeUrl = "$AuthBase/oauth2/authorize?response_type=code" +
        "&client_id=$([uri]::EscapeDataString($ClientId))" +
        "&redirect_uri=$([uri]::EscapeDataString($RedirectUri))" +
        "&scope=$([uri]::EscapeDataString($RequestScopes))" +
        "&state=$($pkce.State)" +
        "&code_challenge=$($pkce.Challenge)" +
        "&code_challenge_method=S256"

    $authorizeResp = Invoke-WebRequest -UseBasicParsing -WebSession $session -Method Get -Uri $authorizeUrl -MaximumRedirection 0 -ErrorAction SilentlyContinue
    $location = $authorizeResp.Headers["Location"]
    if ([string]::IsNullOrWhiteSpace($location)) {
        throw "未获取到 authorization code 跳转地址：$Account"
    }

    $code = [System.Web.HttpUtility]::ParseQueryString(([uri]$location).Query).Get("code")
    if ([string]::IsNullOrWhiteSpace($code)) {
        throw "未获取到 authorization code：$Account"
    }

    $tokenResp = Invoke-RestMethod -UseBasicParsing -Method Post -Uri "$AuthBase/oauth2/token" -Body @{
        grant_type    = "authorization_code"
        client_id     = $ClientId
        code          = $code
        redirect_uri  = $RedirectUri
        code_verifier = $pkce.Verifier
    }

    $data = if ($tokenResp.data) { $tokenResp.data } else { $tokenResp }
    $payload = Get-JwtPayload -Token $data.access_token

    [pscustomobject]@{
        Account      = $Account
        AccessToken  = $data.access_token
        RefreshToken = $data.refresh_token
        Payload      = $payload
    }
}

function Add-Result {
    param(
        [System.Collections.Generic.List[object]]$Results,
        [string]$Item,
        [bool]$Passed,
        [string]$Detail
    )

    $Results.Add([pscustomobject]@{
        Item   = $Item
        Result = if ($Passed) { "PASS" } else { "FAIL" }
        Detail = $Detail
    }) | Out-Null
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$transcriptPath = Join-Path $OutputDir "permission-demo-$timestamp.txt"
Start-Transcript -Path $transcriptPath -Force | Out-Null

$results = New-Object 'System.Collections.Generic.List[object]'

try {
    Write-Section "1. HTTPS 与 OAuth2 基础信息"
    $configResp = Invoke-DemoRequest -Method GET -Url "$AuthBase/oauth2/config"
    Write-Item "OAuth2 Config HTTP" "$($configResp.HttpStatus)" Green
    Write-Item "Issuer" "$($configResp.Json.data.issuer)" Green
    Write-Item "Client ID" "$($configResp.Json.data.defaultClientId)" Green
    Write-Item "Requested Scopes" $Scopes Yellow
    Add-Result $results "HTTPS /oauth2/config" ($configResp.HttpStatus -eq 200) "HTTP $($configResp.HttpStatus)"
    Pause-ForScreenshot "HTTPS 与 OAuth2 配置"

    Write-Section "2. VIEWER 获取 Token 与 Claims"
    $viewer = Get-OAuthToken -Account $ViewerAccount -Password $ViewerPassword -RequestScopes $Scopes
    Write-Item "VIEWER Access Token" (Format-Token $viewer.AccessToken) Yellow
    Write-Item "VIEWER Refresh Token" (Format-Token $viewer.RefreshToken) Yellow
    Write-Host "VIEWER JWT Payload:" -ForegroundColor Green
    $viewer.Payload | ConvertTo-Json -Depth 8
    Add-Result $results "VIEWER Token 获取" $true "scope=$($viewer.Payload.scope)"
    Pause-ForScreenshot "VIEWER Token 与 Claims"

    Write-Section "3. OPERATOR 获取 Token 与 Claims"
    $operator = Get-OAuthToken -Account $OperatorAccount -Password $OperatorPassword -RequestScopes $Scopes
    Write-Item "OPERATOR Access Token" (Format-Token $operator.AccessToken) Yellow
    Write-Item "OPERATOR Refresh Token" (Format-Token $operator.RefreshToken) Yellow
    Write-Host "OPERATOR JWT Payload:" -ForegroundColor Green
    $operator.Payload | ConvertTo-Json -Depth 8
    Add-Result $results "OPERATOR Token 获取" $true "scope=$($operator.Payload.scope)"
    Pause-ForScreenshot "OPERATOR Token 与 Claims"

    $viewerHeaders = @{ Authorization = "Bearer $($viewer.AccessToken)" }
    $operatorHeaders = @{ Authorization = "Bearer $($operator.AccessToken)" }

    Write-Section "4. 401 Unauthorized 演示"
    $noTokenResp = Invoke-DemoRequest -Method GET -Url $DeviceUrl
    $badTokenResp = Invoke-DemoRequest -Method GET -Url $DeviceUrl -Headers @{ Authorization = "Bearer invalid-token" }
    Write-Item "No Token HTTP" "$($noTokenResp.HttpStatus)" $(if ($noTokenResp.HttpStatus -eq 401) { "Green" } else { "Red" })
    Write-Item "Bad Token HTTP" "$($badTokenResp.HttpStatus)" $(if ($badTokenResp.HttpStatus -eq 401) { "Green" } else { "Red" })
    Add-Result $results "无 Token 返回 401" ($noTokenResp.HttpStatus -eq 401) "HTTP $($noTokenResp.HttpStatus)"
    Add-Result $results "非法 Token 返回 401" ($badTokenResp.HttpStatus -eq 401) "HTTP $($badTokenResp.HttpStatus)"
    Pause-ForScreenshot "401 Unauthorized"

    Write-Section "5. VIEWER 权限边界"
    $viewerDeviceResp = Invoke-DemoRequest -Method GET -Url $DeviceUrl -Headers $viewerHeaders
    $viewerAdminResp = Invoke-DemoRequest -Method GET -Url $AdminUrl -Headers $viewerHeaders
    $viewerLedResp = Invoke-DemoRequest -Method POST -Url $LedUrl -Headers $viewerHeaders -Body $LedBody
    $viewerCenterPwdResp = Invoke-DemoRequest -Method POST -Url $CenterPwdUrl -Headers $viewerHeaders -Body @{
        id       = 1
        oldPwd   = $ViewerPassword
        password = $ViewerPassword
    }
    Write-Item "VIEWER 查设备" "HTTP $($viewerDeviceResp.HttpStatus)" $(if ($viewerDeviceResp.HttpStatus -eq 200) { "Green" } else { "Red" })
    Write-Item "VIEWER 管理域" "HTTP $($viewerAdminResp.HttpStatus), BizCode $($viewerAdminResp.Json.code)" $(if ($viewerAdminResp.Json.code -eq 403) { "Green" } else { "Red" })
    Write-Item "VIEWER 控灯" "HTTP $($viewerLedResp.HttpStatus), BizCode $($viewerLedResp.Json.code)" $(if ($viewerLedResp.Json.code -eq 403) { "Green" } else { "Red" })
    Write-Item "VIEWER 改自己密码" "HTTP $($viewerCenterPwdResp.HttpStatus), BizCode $($viewerCenterPwdResp.Json.code)" $(if ($viewerCenterPwdResp.Json.code -eq 200) { "Green" } else { "Red" })
    Add-Result $results "VIEWER 可查设备" ($viewerDeviceResp.HttpStatus -eq 200) "HTTP $($viewerDeviceResp.HttpStatus)"
    Add-Result $results "VIEWER 禁止访问管理域" ($viewerAdminResp.Json.code -eq 403) "HTTP $($viewerAdminResp.HttpStatus), BizCode $($viewerAdminResp.Json.code)"
    Add-Result $results "VIEWER 禁止控灯" ($viewerLedResp.Json.code -eq 403) "HTTP $($viewerLedResp.HttpStatus), BizCode $($viewerLedResp.Json.code)"
    Add-Result $results "VIEWER Bearer 用户中心可用" ($viewerCenterPwdResp.Json.code -eq 200) "HTTP $($viewerCenterPwdResp.HttpStatus), BizCode $($viewerCenterPwdResp.Json.code)"
    Pause-ForScreenshot "VIEWER 权限边界"

    Write-Section "6. OPERATOR 权限亮点"
    $operatorAdminResp = Invoke-DemoRequest -Method GET -Url $AdminUrl -Headers $operatorHeaders
    $operatorLedResp = Invoke-DemoRequest -Method POST -Url $LedUrl -Headers $operatorHeaders -Body $LedBody
    $operatorPassedPermission = $false
    $operatorDetail = ""
    if ($operatorLedResp.Json -and $operatorLedResp.Json.code -ne 403) {
        $operatorPassedPermission = $true
        $operatorDetail = "已通过权限校验，当前业务返回 code=$($operatorLedResp.Json.code)"
    } else {
        $operatorDetail = "仍被权限拦截"
    }
    Write-Item "OPERATOR 管理域" "HTTP $($operatorAdminResp.HttpStatus), BizCode $($operatorAdminResp.Json.code)" $(if ($operatorAdminResp.Json.code -eq 403) { "Green" } else { "Red" })
    Write-Item "OPERATOR 控灯" "HTTP $($operatorLedResp.HttpStatus), BizCode $($operatorLedResp.Json.code)" $(if ($operatorPassedPermission) { "Green" } else { "Red" })
    if ($operatorLedResp.Body) {
        Write-Host "OPERATOR 控灯响应体：" -ForegroundColor Green
        Write-Output $operatorLedResp.Body
    }
    Add-Result $results "OPERATOR 禁止访问管理域" ($operatorAdminResp.Json.code -eq 403) "HTTP $($operatorAdminResp.HttpStatus), BizCode $($operatorAdminResp.Json.code)"
    Add-Result $results "OPERATOR 允许进入控灯业务链路" $operatorPassedPermission $operatorDetail
    Pause-ForScreenshot "OPERATOR 权限与控灯链路"

    if (-not [string]::IsNullOrWhiteSpace($AdminPassword)) {
        Write-Section "7. ADMIN 管理权限"
        $admin = Get-OAuthToken -Account $AdminAccount -Password $AdminPassword -RequestScopes $Scopes
        $adminHeaders = @{ Authorization = "Bearer $($admin.AccessToken)" }
        $adminResp = Invoke-DemoRequest -Method GET -Url $AdminUrl -Headers $adminHeaders
        Write-Item "ADMIN Scope" "$($admin.Payload.scope)" Green
        Write-Item "ADMIN 管理域访问" "HTTP $($adminResp.HttpStatus), BizCode $($adminResp.Json.code)" $(if ($adminResp.HttpStatus -eq 200 -and $adminResp.Json.code -ne 403) { "Green" } else { "Red" })
        Add-Result $results "ADMIN 可访问管理域" ($adminResp.HttpStatus -eq 200 -and $adminResp.Json.code -ne 403) "HTTP $($adminResp.HttpStatus)"
        Pause-ForScreenshot "ADMIN 管理权限"
    } else {
        Add-Result $results "ADMIN 演示" $true "未提供 AdminPassword，已跳过"
    }

    Write-Section "8. Refresh Token 与 Revoke"
    $refreshResp = Invoke-RestMethod -UseBasicParsing -Method Post -Uri "$AuthBase/oauth2/token" -Body @{
        grant_type    = "refresh_token"
        client_id     = $ClientId
        refresh_token = $viewer.RefreshToken
    }
    $revokeResp = Invoke-RestMethod -UseBasicParsing -Method Post -Uri "$AuthBase/oauth2/revoke" -Body @{
        client_id = $ClientId
        token     = $viewer.RefreshToken
    }
    $refreshAfterRevokeResp = Invoke-DemoRequest -Method POST -Url "$AuthBase/oauth2/token" -ContentType "application/x-www-form-urlencoded" -Body "grant_type=refresh_token&client_id=$ClientId&refresh_token=$($viewer.RefreshToken)"
    Write-Item "Refresh 成功" (Format-Token $refreshResp.data.access_token) Green
    Write-Item "Revoke 结果" "code=$($revokeResp.code), msg=$($revokeResp.msg)" Green
    Write-Item "撤销后再刷新" "HTTP $($refreshAfterRevokeResp.HttpStatus), BizCode $($refreshAfterRevokeResp.Json.code), Msg $($refreshAfterRevokeResp.Json.msg)" $(if ($refreshAfterRevokeResp.Json.msg -match "revoked") { "Green" } else { "Red" })
    Add-Result $results "Refresh Token 可换新 Token" (-not [string]::IsNullOrWhiteSpace($refreshResp.data.access_token)) "refresh success"
    Add-Result $results "Revoke 后 Refresh Token 失效" ($refreshAfterRevokeResp.Json.msg -match "revoked") "HTTP $($refreshAfterRevokeResp.HttpStatus), Msg $($refreshAfterRevokeResp.Json.msg)"
    Pause-ForScreenshot "Refresh / Revoke"

    Write-Section "9. 截图用总结"
    $results | Format-Table -AutoSize
    Write-Host ""
    Write-Host "演示输出已保存：" -ForegroundColor Green
    Write-Host $transcriptPath -ForegroundColor Yellow
} finally {
    Stop-Transcript | Out-Null
}
