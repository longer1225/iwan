#!/usr/bin/env pwsh
$API_BASE = "http://localhost:8080/iwan/api/v1"
function Write-Info($msg) { Write-Host "[INFO] $msg" -ForegroundColor Cyan }
function Write-OK($msg) { Write-Host "[OK] $msg" -ForegroundColor Green }
function Write-Err($msg) { Write-Host "[ERR] $msg" -ForegroundColor Red }

# 1. 登录 iwanna
Write-Info "Step 1: 登录 iwanna (password123)"
$body = @{username="iwanna";password="password123"} | ConvertTo-Json
Write-Info "Request: $body"
$resp = Invoke-WebRequest -Uri "$API_BASE/user/login" -Method Post -Body $body -ContentType "application/json" -UseBasicParsing
$json = $resp.Content | ConvertFrom-Json
Write-Info "Response: $($resp.Content)"
if ($json.code -ne 200) {
    Write-Err "登录失败!"
    exit 1
}
$token = $json.data.token
$userId = $json.data.userId
Write-OK "登录成功 - ID: $userId"

# 2. 登录 iwanna2
Write-Info "Step 2: 登录 iwanna2 (password123)"
$body2 = @{username="iwanna2";password="password123"} | ConvertTo-Json
$resp2 = Invoke-WebRequest -Uri "$API_BASE/user/login" -Method Post -Body $body2 -ContentType "application/json" -UseBasicParsing
$json2 = $resp2.Content | ConvertFrom-Json
if ($json2.code -ne 200) {
    Write-Err "登录失败!"
    exit 1
}
$token2 = $json2.data.token
$userId2 = $json2.data.userId
Write-OK "登录成功 - ID: $userId2"

# 3. 发送好友请求
Write-Info "Step 3: iwanna 发送好友请求给 iwanna2"
$headers = @{Authorization = "Bearer $token"}
$reqBody = @{targetUserId = [long]$userId2; message = "你好"} | ConvertTo-Json
Write-Info "Request: $reqBody"
try {
    $reqResp = Invoke-WebRequest -Uri "$API_BASE/friends/request" -Method Post -Body $reqBody -Headers $headers -ContentType "application/json" -UseBasicParsing
    Write-OK "发送结果: $($reqResp.Content)"
} catch {
    Write-Err "发送失败: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Err "Error: $($reader.ReadToEnd())"
    }
    exit 1
}

# 4. 查询好友请求
Write-Info "Step 4: iwanna2 查询好友请求"
$headers2 = @{Authorization = "Bearer $token2"}
try {
    $reqsResp = Invoke-WebRequest -Uri "$API_BASE/friends/requests" -Method Get -Headers $headers2 -UseBasicParsing
    Write-OK "好友请求: $($reqsResp.Content)"
} catch {
    Write-Err "查询失败: $($_.Exception.Message)"
}

Write-OK "=== 测试通过 ==="
