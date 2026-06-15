#!/usr/bin/env pwsh
$API_BASE = "http://localhost:8080/iwan/api/v1"

# 1. 登录 iwanna
Write-Host "[INFO] 登录 iwanna" -ForegroundColor Cyan
$body = @{username="iwanna";password="password123"} | ConvertTo-Json
$resp = Invoke-WebRequest -Uri "$API_BASE/user/login" -Method Post -Body $body -ContentType "application/json" -UseBasicParsing
$json = $resp.Content | ConvertFrom-Json
$token = $json.data.token
$userId = $json.data.userId
Write-Host "[OK] 登录成功 - ID: $userId" -ForegroundColor Green

# 2. 获取 iwanna2 的 ID
Write-Host "[INFO] 搜索 iwanna2" -ForegroundColor Cyan
$headers = @{Authorization = "Bearer $token"}
$search = Invoke-WebRequest -Uri "$API_BASE/search?keyword=iwanna2&type=user" -Method Get -Headers $headers -UseBasicParsing
$searchJson = $search.Content | ConvertFrom-Json
$targetId = $searchJson.data.users[0].id
Write-Host "[OK] iwanna2 ID: $targetId" -ForegroundColor Green

# 3. 发送好友请求（使用 String 类型的 targetUserId）
Write-Host "[INFO] 发送好友请求（String类型ID）" -ForegroundColor Cyan
$reqBody = @{targetUserId=$targetId.ToString();message=""} | ConvertTo-Json
Write-Host "Request Body: $reqBody"
try {
    $reqResp = Invoke-WebRequest -Uri "$API_BASE/friends/request" -Method Post -Body $reqBody -Headers $headers -ContentType "application/json" -UseBasicParsing
    Write-Host "[OK] 发送结果: $($reqResp.Content)" -ForegroundColor Green
} catch {
    Write-Host "[ERR] 发送失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "Error Body: $($reader.ReadToEnd())" -ForegroundColor Red
    }
}

Write-Host "=== 测试完成 ===" -ForegroundColor Green
