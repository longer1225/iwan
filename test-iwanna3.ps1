#!/usr/bin/env pwsh
$API_BASE = "http://localhost:8080/iwan/api/v1"

function Test-Login($username, $password) {
    $body = @{username=$username;password=$password} | ConvertTo-Json
    try {
        $resp = Invoke-WebRequest -Uri "$API_BASE/user/login" -Method Post -Body $body -ContentType "application/json" -UseBasicParsing
        $json = $resp.Content | ConvertFrom-Json
        if ($json.code -eq 200) {
            Write-Host "[OK] $username login success - ID: $($json.data.userId)" -ForegroundColor Green
            return $json.data.token, $json.data.userId
        } else {
            Write-Host "[ERR] $username login failed: $($json.msg)" -ForegroundColor Red
            return $null, $null
        }
    } catch {
        Write-Host "[ERR] $username login error: $($_.Exception.Message)" -ForegroundColor Red
        return $null, $null
    }
}

function Test-SendRequest($token, $fromUser, $targetUserId, $targetName) {
    Write-Host "[INFO] $fromUser send request to $targetName (ID: $targetUserId)" -ForegroundColor Cyan
    $headers = @{Authorization = "Bearer $token"}
    $reqBody = @{targetUserId=$targetUserId.ToString();message=""} | ConvertTo-Json
    try {
        $reqResp = Invoke-WebRequest -Uri "$API_BASE/friends/request" -Method Post -Body $reqBody -Headers $headers -ContentType "application/json" -UseBasicParsing
        $json = $reqResp.Content | ConvertFrom-Json
        Write-Host "[OK] Result: code=$($json.code), msg=$($json.msg)" -ForegroundColor Green
    } catch {
        Write-Host "[ERR] Send failed: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            Write-Host "[ERR] Body: $($reader.ReadToEnd())" -ForegroundColor Red
        }
    }
}

# 1. Test iwanna3 login
$token3, $id3 = Test-Login "iwanna3" "123456"
if (-not $token3) {
    Write-Host "[INFO] Try password password123..." -ForegroundColor Yellow
    $token3, $id3 = Test-Login "iwanna3" "password123"
}

# 2. Test iwanna2 login
$token2, $id2 = Test-Login "iwanna2" "123456"

if ($token3 -and $token2) {
    # 3. iwanna3 send to iwanna2
    Test-SendRequest $token3 "iwanna3" $id2 "iwanna2"
    
    # 4. Check iwanna2 requests
    Write-Host "[INFO] Query iwanna2 requests" -ForegroundColor Cyan
    $headers2 = @{Authorization = "Bearer $token2"}
    $reqs = Invoke-WebRequest -Uri "$API_BASE/friends/requests" -Method Get -Headers $headers2 -UseBasicParsing
    $reqsJson = $reqs.Content | ConvertFrom-Json
    Write-Host "[OK] iwanna2 received $($reqsJson.data.Length) requests" -ForegroundColor Green
    foreach ($r in $reqsJson.data) {
        Write-Host "  - From: $($r.userName) (ID: $($r.id))" -ForegroundColor Gray
    }
}

Write-Host "=== Done ===" -ForegroundColor Green
