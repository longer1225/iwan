#!/usr/bin/env pwsh
# 完整测试好友请求功能 - 直接访问后端

$ErrorActionPreference = "Continue"
$API_BASE = "http://localhost:8080/iwan/api/v1"

function Write-Info($msg) { Write-Host "[INFO] $msg" -ForegroundColor Cyan }
function Write-OK($msg) { Write-Host "[OK] $msg" -ForegroundColor Green }
function Write-Err($msg) { Write-Host "[ERR] $msg" -ForegroundColor Red }

# 1. 登录用户A
Write-Info "Step 1: 登录用户A (iwanna)"
$loginA = @{username="iwanna";password="password123"} | ConvertTo-Json
$respA = Invoke-WebRequest -Uri "$API_BASE/user/login" -Method Post -Body $loginA -ContentType "application/json" -UseBasicParsing
$jsonA = $respA.Content | ConvertFrom-Json
if ($jsonA.code -ne 200) {
    Write-Err "用户A登录失败: $($respA.Content)"
    Write-Info "尝试用户B"
    $loginA = @{username="testuser1";password="test123456"} | ConvertTo-Json
    $respA = Invoke-WebRequest -Uri "$API_BASE/user/login" -Method Post -Body $loginA -ContentType "application/json" -UseBasicParsing
    $jsonA = $respA.Content | ConvertFrom-Json
}
$tokenA = $jsonA.data.token
$userIdA = $jsonA.data.userId
Write-OK "用户A登录成功 - ID: $userIdA"

# 2. 登录用户B
Write-Info "Step 2: 登录用户B (iwanna2)"
$loginB = @{username="iwanna2";password="password123"} | ConvertTo-Json
$respB = Invoke-WebRequest -Uri "$API_BASE/user/login" -Method Post -Body $loginB -ContentType "application/json" -UseBasicParsing
$jsonB = $respB.Content | ConvertFrom-Json
if ($jsonB.code -ne 200) {
    Write-Err "用户B登录失败: $($respB.Content)"
    Write-Info "尝试testuser2"
    $loginB = @{username="testuser2";password="test123456"} | ConvertTo-Json
    $respB = Invoke-WebRequest -Uri "$API_BASE/user/login" -Method Post -Body $loginB -ContentType "application/json" -UseBasicParsing
    $jsonB = $respB.Content | ConvertFrom-Json
}
$tokenB = $jsonB.data.token
$userIdB = $jsonB.data.userId
Write-OK "用户B登录成功 - ID: $userIdB"

# 3. 用户A发送好友请求给用户B
Write-Info "Step 3: 用户A发送好友请求给用户B (userIdB=$userIdB)"
$headersA = @{Authorization="Bearer $tokenA"}
$reqBody = @{targetUserId=$userIdB;message="你好"} | ConvertTo-Json
try {
    $reqResp = Invoke-WebRequest -Uri "$API_BASE/friends/request" -Method Post -Body $reqBody -Headers $headersA -ContentType "application/json" -UseBasicParsing
    Write-OK "发送成功: $($reqResp.Content)"
} catch {
    $errBody = $_.Exception.Response
    Write-Err "发送失败: HTTP $($_.Exception.Response.StatusCode)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Info "错误详情: $($reader.ReadToEnd())"
    }
}

# 4. 用户B查看好友请求
Write-Info "Step 4: 用户B查看好友请求"
$headersB = @{Authorization="Bearer $tokenB"}
try {
    $reqsResp = Invoke-WebRequest -Uri "$API_BASE/friends/requests" -Method Get -Headers $headersB -UseBasicParsing
    Write-OK "好友请求列表: $($reqsResp.Content)"
} catch {
    Write-Err "查询失败: HTTP $($_.Exception.Response.StatusCode)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Info "错误详情: $($reader.ReadToEnd())"
    }
}
