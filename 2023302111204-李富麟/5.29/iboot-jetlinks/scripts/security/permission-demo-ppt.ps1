param(
    [string]$AuthBase = "http://127.0.0.1/api",
    [string]$ClientId = "iboot-local-web",
    [string]$RedirectUri = "http://127.0.0.1/oauth/callback.html",
    [string]$Scopes = "device.read device.control user.manage",
    [string]$ViewerAccount = "viewer",
    [string]$ViewerPassword = "Viewer#2026",
    [string]$OperatorAccount = "operator",
    [string]$OperatorPassword = "Operator#2026",
    [string]$AdminAccount = "admin",
    [string]$AdminPassword = "",
    [string]$DeviceUrl = "http://127.0.0.1/api/iot/panels/devices",
    [string]$LedUrl = "http://127.0.0.1/api/iot/panels/switchCtrlStatus",
    [string]$AdminUrl = "http://127.0.0.1/api/core/admin/view",
    [string]$CenterPwdUrl = "http://127.0.0.1/api/core/center/pwd",
    [string]$LedBody = '{"id":1832263091970150401,"status":"1"}',
    [string]$OutputDir = "D:\Proj\5.29\iboot-jetlinks\scripts\security\output",
    [switch]$Interactive,
    [switch]$MaskToken
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12
[System.Net.ServicePointManager]::ServerCertificateValidationCallback = { $true }

function Write-Section {
    param([string]$Title)
    Write-Host ""
    Write-Host ("=" * 96) -ForegroundColor DarkCyan
    Write-Host $Title -ForegroundColor Cyan
    Write-Host ("=" * 96) -ForegroundColor DarkCyan
}

function Write-Item {
    param(
        [string]$Name,
        [string]$Value,
        [ConsoleColor]$Color = [ConsoleColor]::Gray
    )
    Write-Host ("{0,-34}: {1}" -f $Name, $Value) -ForegroundColor $Color
}

function Wait-Screenshot {
    param([string]$Name)
    if ($Interactive) {
        Read-Host "Press Enter after screenshot: $Name" | Out-Null
    }
}

function New-PkcePair {
    $bytes = New-Object byte[] 48
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $rng.GetBytes($bytes)
    $verifier = [Convert]::ToBase64String($bytes).TrimEnd("=").Replace("+", "-").Replace("/", "_")
    $sha = [System.Security.Cryptography.SHA256]::Create()
    $hash = $sha.ComputeHash([System.Text.Encoding]::ASCII.GetBytes($verifier))
    $challenge = [Convert]::ToBase64String($hash).TrimEnd("=").Replace("+", "-").Replace("/", "_")
    [pscustomobject]@{
        Verifier = $verifier
        Challenge = $challenge
        State = [guid]::NewGuid().ToString("N")
    }
}

function ConvertFrom-Base64Url {
    param([Parameter(Mandatory = $true)][string]$Value)
    $text = $Value.Replace("-", "+").Replace("_", "/")
    while (($text.Length % 4) -ne 0) {
        $text = $text + "="
    }
    [System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($text))
}

function Get-JwtPayload {
    param([Parameter(Mandatory = $true)][string]$Token)
    $parts = $Token.Split(".")
    if ($parts.Length -lt 2) {
        throw "Invalid JWT format"
    }
    ConvertFrom-Base64Url -Value $parts[1] | ConvertFrom-Json
}

function Format-Token {
    param([string]$Token)
    if (-not $MaskToken) {
        return $Token
    }
    if ([string]::IsNullOrWhiteSpace($Token) -or $Token.Length -lt 48) {
        return $Token
    }
    return $Token.Substring(0, 24) + " ... " + $Token.Substring($Token.Length - 24)
}

function ConvertTo-FormBody {
    param([hashtable]$Values)
    $parts = New-Object System.Collections.Generic.List[string]
    foreach ($key in $Values.Keys) {
        $encodedKey = [uri]::EscapeDataString([string]$key)
        $encodedValue = [uri]::EscapeDataString([string]$Values[$key])
        $parts.Add($encodedKey + "=" + $encodedValue) | Out-Null
    }
    return ($parts -join "&")
}

function Invoke-DemoRequest {
    param(
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Url,
        [hashtable]$Headers = @{},
        [AllowNull()]$Body = $null,
        [string]$ContentType = "application/json"
    )

    $requestBody = $null
    if ($null -ne $Body) {
        if ($Body -is [string]) {
            $requestBody = $Body
        } elseif ($ContentType -like "application/json*") {
            $requestBody = $Body | ConvertTo-Json -Compress -Depth 8
        } else {
            $requestBody = $Body
        }
    }

    try {
        if ($null -ne $requestBody) {
            $resp = Invoke-WebRequest -UseBasicParsing -Method $Method -Uri $Url -Headers $Headers -Body $requestBody -ContentType $ContentType
        } else {
            $resp = Invoke-WebRequest -UseBasicParsing -Method $Method -Uri $Url -Headers $Headers
        }
        $json = $null
        if (-not [string]::IsNullOrWhiteSpace($resp.Content)) {
            try { $json = $resp.Content | ConvertFrom-Json } catch { $json = $null }
        }
        return [pscustomobject]@{
            HttpStatus = [int]$resp.StatusCode
            Body = $resp.Content
            Json = $json
            Headers = $resp.Headers
        }
    } catch {
        if (-not $_.Exception.Response) {
            throw
        }
        $response = $_.Exception.Response
        $bodyText = ""
        $stream = $response.GetResponseStream()
        if ($stream) {
            $reader = New-Object System.IO.StreamReader($stream)
            $bodyText = $reader.ReadToEnd()
        }
        $json = $null
        if (-not [string]::IsNullOrWhiteSpace($bodyText)) {
            try { $json = $bodyText | ConvertFrom-Json } catch { $json = $null }
        }
        return [pscustomobject]@{
            HttpStatus = [int]$response.StatusCode
            Body = $bodyText
            Json = $json
            Headers = $response.Headers
        }
    }
}

function Get-RedirectLocation {
    param(
        [Parameter(Mandatory = $true)][string]$Url,
        [Parameter(Mandatory = $true)][Microsoft.PowerShell.Commands.WebRequestSession]$Session
    )

    $request = [System.Net.HttpWebRequest]::Create($Url)
    $request.Method = "GET"
    $request.AllowAutoRedirect = $false
    $request.CookieContainer = $Session.Cookies
    try {
        $response = $request.GetResponse()
        $location = $response.Headers["Location"]
        $response.Close()
        return $location
    } catch [System.Net.WebException] {
        if ($_.Exception.Response) {
            $response = $_.Exception.Response
            $location = $response.Headers["Location"]
            $response.Close()
            return $location
        }
        throw
    }
}

function Get-QueryValue {
    param(
        [Parameter(Mandatory = $true)][string]$Url,
        [Parameter(Mandatory = $true)][string]$Name
    )

    $pattern = "[?&]" + [regex]::Escape($Name) + "=([^&]+)"
    $match = [regex]::Match($Url, $pattern)
    if (-not $match.Success) {
        return ""
    }
    return [uri]::UnescapeDataString($match.Groups[1].Value)
}

function Get-OAuthToken {
    param(
        [Parameter(Mandatory = $true)][string]$Account,
        [Parameter(Mandatory = $true)][string]$Password,
        [Parameter(Mandatory = $true)][string]$RequestScopes
    )

    $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $pkce = New-PkcePair

    $loginResp = Invoke-RestMethod -UseBasicParsing -WebSession $session -Method POST -Uri "$AuthBase/oauth2/doLogin" -Body @{
        name = $Account
        pwd = $Password
    }
    if ($loginResp.code -ne 200) {
        throw "OAuth2 doLogin failed for account: $Account"
    }

    $authorizeParams = @{
        response_type = "code"
        client_id = $ClientId
        redirect_uri = $RedirectUri
        scope = $RequestScopes
        state = $pkce.State
        code_challenge = $pkce.Challenge
        code_challenge_method = "S256"
    }
    $authorizeUrl = "$AuthBase/oauth2/authorize?" + (ConvertTo-FormBody -Values $authorizeParams)
    $location = Get-RedirectLocation -Url $authorizeUrl -Session $session
    if ([string]::IsNullOrWhiteSpace($location)) {
        throw "Authorization endpoint did not return redirect location for account: $Account"
    }

    $code = Get-QueryValue -Url $location -Name "code"
    if ([string]::IsNullOrWhiteSpace($code)) {
        throw "Authorization code missing for account: $Account"
    }

    $tokenResp = Invoke-RestMethod -UseBasicParsing -Method POST -Uri "$AuthBase/oauth2/token" -Body @{
        grant_type = "authorization_code"
        client_id = $ClientId
        code = $code
        redirect_uri = $RedirectUri
        code_verifier = $pkce.Verifier
    }
    $data = if ($tokenResp.data) { $tokenResp.data } else { $tokenResp }
    if ([string]::IsNullOrWhiteSpace($data.access_token)) {
        throw "access_token missing for account: $Account"
    }

    [pscustomobject]@{
        Account = $Account
        AccessToken = $data.access_token
        RefreshToken = $data.refresh_token
        Payload = Get-JwtPayload -Token $data.access_token
        CodeChallengeMethod = "S256"
    }
}

function Get-Code {
    param($Json)
    if ($null -eq $Json) {
        return ""
    }
    if ($null -ne $Json.code) {
        return [string]$Json.code
    }
    return ""
}

function Add-Result {
    param(
        [System.Collections.Generic.List[object]]$Results,
        [string]$Item,
        [bool]$Pass,
        [string]$Detail
    )
    $Results.Add([pscustomobject]@{
        Item = $Item
        Result = if ($Pass) { "PASS" } else { "FAIL" }
        Detail = $Detail
    }) | Out-Null
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$transcriptPath = Join-Path $OutputDir "permission-demo-ppt-$timestamp.txt"
Start-Transcript -Path $transcriptPath -Force | Out-Null

$results = New-Object "System.Collections.Generic.List[object]"

try {
    Write-Section "1. DEMO TARGET AND OAUTH2 CONFIG"
    Write-Item "AuthBase" $AuthBase Yellow
    Write-Item "ClientId" $ClientId Yellow
    Write-Item "RedirectUri" $RedirectUri Yellow
    Write-Item "RequestedScopes" $Scopes Yellow

    $configResp = Invoke-DemoRequest -Method GET -Url "$AuthBase/oauth2/config"
    Write-Item "GET /oauth2/config" "HTTP $($configResp.HttpStatus)" $(if ($configResp.HttpStatus -eq 200) { [ConsoleColor]::Green } else { [ConsoleColor]::Red })
    if ($configResp.Json -and $configResp.Json.data) {
        Write-Item "Issuer" ([string]$configResp.Json.data.issuer) Green
        Write-Item "DefaultClientId" ([string]$configResp.Json.data.defaultClientId) Green
    }
    Add-Result $results "OAuth2 config endpoint" ($configResp.HttpStatus -eq 200) "HTTP $($configResp.HttpStatus)"
    Wait-Screenshot "OAuth2 config"

    Write-Section "2. VIEWER AUTHORIZATION CODE + PKCE + JWT TOKEN"
    $viewer = Get-OAuthToken -Account $ViewerAccount -Password $ViewerPassword -RequestScopes $Scopes
    Write-Item "Account" $viewer.Account Green
    Write-Item "PKCE" $viewer.CodeChallengeMethod Green
    Write-Item "Access Token" (Format-Token $viewer.AccessToken) Yellow
    Write-Item "Refresh Token" (Format-Token $viewer.RefreshToken) Yellow
    Write-Host "JWT payload:" -ForegroundColor Cyan
    $viewer.Payload | ConvertTo-Json -Depth 8
    Add-Result $results "VIEWER token issued" $true "scope=$($viewer.Payload.scope)"
    Wait-Screenshot "viewer token and jwt claims"

    Write-Section "3. OPERATOR AUTHORIZATION CODE + PKCE + JWT TOKEN"
    $operator = Get-OAuthToken -Account $OperatorAccount -Password $OperatorPassword -RequestScopes $Scopes
    Write-Item "Account" $operator.Account Green
    Write-Item "PKCE" $operator.CodeChallengeMethod Green
    Write-Item "Access Token" (Format-Token $operator.AccessToken) Yellow
    Write-Item "Refresh Token" (Format-Token $operator.RefreshToken) Yellow
    Write-Host "JWT payload:" -ForegroundColor Cyan
    $operator.Payload | ConvertTo-Json -Depth 8
    Add-Result $results "OPERATOR token issued" $true "scope=$($operator.Payload.scope)"
    Wait-Screenshot "operator token and jwt claims"

    $viewerHeaders = @{ Authorization = "Bearer $($viewer.AccessToken)" }
    $operatorHeaders = @{ Authorization = "Bearer $($operator.AccessToken)" }

    Write-Section "4. TOKEN REQUIRED: 401 UNAUTHORIZED"
    $noTokenResp = Invoke-DemoRequest -Method GET -Url $DeviceUrl
    $badTokenResp = Invoke-DemoRequest -Method GET -Url $DeviceUrl -Headers @{ Authorization = "Bearer invalid-token" }
    Write-Item "No token device query" "HTTP $($noTokenResp.HttpStatus)" $(if ($noTokenResp.HttpStatus -eq 401) { [ConsoleColor]::Green } else { [ConsoleColor]::Red })
    Write-Item "Bad token device query" "HTTP $($badTokenResp.HttpStatus)" $(if ($badTokenResp.HttpStatus -eq 401) { [ConsoleColor]::Green } else { [ConsoleColor]::Red })
    Add-Result $results "No token rejected" ($noTokenResp.HttpStatus -eq 401) "HTTP $($noTokenResp.HttpStatus)"
    Add-Result $results "Bad token rejected" ($badTokenResp.HttpStatus -eq 401) "HTTP $($badTokenResp.HttpStatus)"
    Wait-Screenshot "401 token required"

    Write-Section "5. VIEWER RBAC: READ ALLOWED, ADMIN AND LED CONTROL DENIED"
    $viewerDeviceResp = Invoke-DemoRequest -Method GET -Url $DeviceUrl -Headers $viewerHeaders
    $viewerAdminResp = Invoke-DemoRequest -Method GET -Url $AdminUrl -Headers $viewerHeaders
    $viewerLedResp = Invoke-DemoRequest -Method POST -Url $LedUrl -Headers $viewerHeaders -Body $LedBody
    $viewerCenterResp = Invoke-DemoRequest -Method POST -Url $CenterPwdUrl -Headers $viewerHeaders -Body @{
        id = 1
        oldPwd = $ViewerPassword
        password = $ViewerPassword
    }

    $viewerAdminCode = Get-Code $viewerAdminResp.Json
    $viewerLedCode = Get-Code $viewerLedResp.Json
    $viewerCenterCode = Get-Code $viewerCenterResp.Json

    Write-Item "VIEWER device query" "HTTP $($viewerDeviceResp.HttpStatus)" $(if ($viewerDeviceResp.HttpStatus -eq 200) { [ConsoleColor]::Green } else { [ConsoleColor]::Red })
    Write-Item "VIEWER admin API" "HTTP $($viewerAdminResp.HttpStatus), BizCode $viewerAdminCode" $(if ($viewerAdminCode -eq "403") { [ConsoleColor]::Green } else { [ConsoleColor]::Red })
    Write-Item "VIEWER LED control" "HTTP $($viewerLedResp.HttpStatus), BizCode $viewerLedCode" $(if ($viewerLedCode -eq "403") { [ConsoleColor]::Green } else { [ConsoleColor]::Red })
    Write-Item "VIEWER self center" "HTTP $($viewerCenterResp.HttpStatus), BizCode $viewerCenterCode" $(if ($viewerCenterCode -eq "200") { [ConsoleColor]::Green } else { [ConsoleColor]::Yellow })

    Add-Result $results "VIEWER can read device data" ($viewerDeviceResp.HttpStatus -eq 200) "HTTP $($viewerDeviceResp.HttpStatus)"
    Add-Result $results "VIEWER cannot access admin API" ($viewerAdminCode -eq "403") "HTTP $($viewerAdminResp.HttpStatus), BizCode $viewerAdminCode"
    Add-Result $results "VIEWER cannot control LED" ($viewerLedCode -eq "403") "HTTP $($viewerLedResp.HttpStatus), BizCode $viewerLedCode"
    Add-Result $results "VIEWER bearer principal works" ($viewerCenterCode -eq "200") "HTTP $($viewerCenterResp.HttpStatus), BizCode $viewerCenterCode"
    Wait-Screenshot "viewer permission boundary"

    Write-Section "6. OPERATOR RBAC: LED CONTROL PERMISSION PASSES"
    $operatorAdminResp = Invoke-DemoRequest -Method GET -Url $AdminUrl -Headers $operatorHeaders
    $operatorLedResp = Invoke-DemoRequest -Method POST -Url $LedUrl -Headers $operatorHeaders -Body $LedBody
    $operatorAdminCode = Get-Code $operatorAdminResp.Json
    $operatorLedCode = Get-Code $operatorLedResp.Json
    $operatorControlPass = ($operatorLedCode -ne "" -and $operatorLedCode -ne "403")

    Write-Item "OPERATOR admin API" "HTTP $($operatorAdminResp.HttpStatus), BizCode $operatorAdminCode" $(if ($operatorAdminCode -eq "403") { [ConsoleColor]::Green } else { [ConsoleColor]::Red })
    Write-Item "OPERATOR LED control" "HTTP $($operatorLedResp.HttpStatus), BizCode $operatorLedCode" $(if ($operatorControlPass) { [ConsoleColor]::Green } else { [ConsoleColor]::Red })
    Add-Result $results "OPERATOR cannot access admin API" ($operatorAdminCode -eq "403") "HTTP $($operatorAdminResp.HttpStatus), BizCode $operatorAdminCode"
    Add-Result $results "OPERATOR enters LED business path" $operatorControlPass "HTTP $($operatorLedResp.HttpStatus), BizCode $operatorLedCode; any non-403 code means RBAC passed"
    Wait-Screenshot "operator led control"

    if (-not [string]::IsNullOrWhiteSpace($AdminPassword)) {
        Write-Section "7. OPTIONAL ADMIN RBAC: ADMIN API ALLOWED"
        $admin = Get-OAuthToken -Account $AdminAccount -Password $AdminPassword -RequestScopes $Scopes
        $adminHeaders = @{ Authorization = "Bearer $($admin.AccessToken)" }
        $adminResp = Invoke-DemoRequest -Method GET -Url $AdminUrl -Headers $adminHeaders
        $adminCode = Get-Code $adminResp.Json
        $adminPass = ($adminResp.HttpStatus -eq 200 -and $adminCode -ne "403")
        Write-Item "ADMIN scope" ([string]$admin.Payload.scope) Green
        Write-Item "ADMIN admin API" "HTTP $($adminResp.HttpStatus), BizCode $adminCode" $(if ($adminPass) { [ConsoleColor]::Green } else { [ConsoleColor]::Red })
        Add-Result $results "ADMIN can access admin API" $adminPass "HTTP $($adminResp.HttpStatus), BizCode $adminCode"
        Wait-Screenshot "admin permission"
    } else {
        Write-Section "7. OPTIONAL ADMIN RBAC: SKIPPED"
        Write-Item "Reason" "Run with -AdminPassword <password> to include ADMIN API proof" Yellow
        Add-Result $results "ADMIN optional demo" $true "Skipped because -AdminPassword was not provided"
        Wait-Screenshot "admin skipped"
    }

    Write-Section "8. REFRESH TOKEN AND REVOKE"
    $refreshResp = Invoke-RestMethod -UseBasicParsing -Method POST -Uri "$AuthBase/oauth2/token" -Body @{
        grant_type = "refresh_token"
        client_id = $ClientId
        refresh_token = $viewer.RefreshToken
    }
    $refreshData = if ($refreshResp.data) { $refreshResp.data } else { $refreshResp }

    $revokeResp = Invoke-RestMethod -UseBasicParsing -Method POST -Uri "$AuthBase/oauth2/revoke" -Body @{
        client_id = $ClientId
        token = $viewer.RefreshToken
    }

    $afterRevokeBody = ConvertTo-FormBody -Values @{
        grant_type = "refresh_token"
        client_id = $ClientId
        refresh_token = $viewer.RefreshToken
    }
    $afterRevokeResp = Invoke-DemoRequest -Method POST -Url "$AuthBase/oauth2/token" -ContentType "application/x-www-form-urlencoded" -Body $afterRevokeBody
    $afterRevokeCode = Get-Code $afterRevokeResp.Json
    $afterRevokeMsg = if ($afterRevokeResp.Json -and $afterRevokeResp.Json.msg) { [string]$afterRevokeResp.Json.msg } else { "" }

    Write-Item "Refresh new access token" (Format-Token $refreshData.access_token) Green
    Write-Item "Revoke response" "code=$($revokeResp.code), msg=$($revokeResp.msg)" Green
    Write-Item "Refresh after revoke" "HTTP $($afterRevokeResp.HttpStatus), BizCode $afterRevokeCode, Msg $afterRevokeMsg" $(if ($afterRevokeMsg -match "revoked") { [ConsoleColor]::Green } else { [ConsoleColor]::Red })

    Add-Result $results "Refresh token can issue new access token" (-not [string]::IsNullOrWhiteSpace($refreshData.access_token)) "refresh success"
    Add-Result $results "Revoked refresh token is rejected" ($afterRevokeMsg -match "revoked") "HTTP $($afterRevokeResp.HttpStatus), Msg $afterRevokeMsg"
    Wait-Screenshot "refresh and revoke"

    Write-Section "9. PPT SCREENSHOT SUMMARY"
    $results | Format-Table -AutoSize
    Write-Host ""
    Write-Host "Transcript saved to:" -ForegroundColor Green
    Write-Host $transcriptPath -ForegroundColor Yellow
} finally {
    Stop-Transcript | Out-Null
}
