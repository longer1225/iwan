#!/usr/bin/env pwsh
$API_BASE = "http://localhost:8080/iwan/api/v1"

# 登录 iwanna2
Write-Host "[INFO] 登录 iwanna2" -ForegroundColor Cyan
$body = @{username="iwanna2";password="password123"} | ConvertTo-Json
$resp = Invoke-WebRequest -Uri "$API_BASE/user/login" -Method Post -Body $body -ContentType "application/json" -UseBasicParsing
$json = $resp.Content | ConvertFrom-Json
$token2 = $json.data.token
$userId2 = $json.data.userId
Write-Host "[OK] 登录成功 - ID: $userId2" -ForegroundColor Green

# 查询好友请求
Write-Host "[INFO] 查询好友请求" -ForegroundColor Cyan
$headers2 = @{Authorization = "Bearer $token2"}
$reqs = Invoke-WebRequest -Uri "$API_BASE/friends/requests" -Method Get -Headers $headers2 -UseBasicParsing
Write-Host "[OK] 好友请求: $($reqs.Content)" -ForegroundColor Green

Write-Host "=== 测试完成 ===" -ForegroundColor Green
