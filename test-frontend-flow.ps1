#!/usr/bin/env pwsh
$API_BASE = "http://localhost:8080/iwan/api/v1"

# 1. 登录 iwanna3
Write-Host "[Step 1] 登录 iwanna3" -ForegroundColor Cyan
$loginBody = @{username="iwanna3";password="123456"} | ConvertTo-Json
$loginResp = Invoke-WebRequest -Uri "$API_BASE/user/login" -Method Post -Body $loginBody -ContentType "application/json" -UseBasicParsing
$loginJson = $loginResp.Content | ConvertFrom-Json
$token = $loginJson.data.token
Write-Host "[OK] 获取 Token: $($token.Substring(0,20))..." -ForegroundColor Green

# 2. 搜索用户 iwanna2
Write-Host "[Step 2] 搜索 iwanna2" -ForegroundColor Cyan
$headers = @{Authorization = "Bearer $token"}
$searchResp = Invoke-WebRequest -Uri "$API_BASE/search?keyword=iwanna2&type=user" -Method Get -Headers $headers -UseBasicParsing
$searchJson = $searchResp.Content | ConvertFrom-Json
$userId = $searchJson.data.users[0].id
Write-Host "[OK] 找到用户: $($searchJson.data.users[0].nickname), ID: $userId" -ForegroundColor Green
Write-Host "[INFO] ID 类型: $($userId.GetType().Name)" -ForegroundColor Gray

# 3. 发送好友请求
Write-Host "[Step 3] 发送好友请求" -ForegroundColor Cyan
$reqBody = @{targetUserId=$userId;message=""} | ConvertTo-Json
Write-Host "[DEBUG] 请求体: $reqBody" -ForegroundColor Gray
try {
    $reqResp = Invoke-WebRequest -Uri "$API_BASE/friends/request" -Method Post -Body $reqBody -Headers $headers -ContentType "application/json" -UseBasicParsing
    $reqJson = $reqResp.Content | ConvertFrom-Json
    Write-Host "[OK] 发送成功: code=$($reqJson.code), msg=$($reqJson.msg)" -ForegroundColor Green
} catch {
    Write-Host "[ERR] 发送失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $body = $reader.ReadToEnd()
        Write-Host "[ERR] 响应体: $body" -ForegroundColor Red
    }
}

Write-Host "=== 测试完成 ===" -ForegroundColor Green
