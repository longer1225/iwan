#!/usr/bin/env pwsh
$API_BASE = "http://localhost:8080/iwan/api/v1"

# 登录 iwanna 并发送好友请求
Write-Host "[INFO] 登录 iwanna (密码: 123456)" -ForegroundColor Cyan
$body = @{username="iwanna";password="123456"} | ConvertTo-Json
$resp = Invoke-WebRequest -Uri "$API_BASE/user/login" -Method Post -Body $body -ContentType "application/json" -UseBasicParsing
$json = $resp.Content | ConvertFrom-Json
$token1 = $json.data.token
Write-Host "[OK] 登录成功" -ForegroundColor Green

# 发送好友请求
Write-Host "[INFO] 发送好友请求" -ForegroundColor Cyan
$headers1 = @{Authorization = "Bearer $token1"}
$reqBody = @{targetUserId="2064987888404586497";message=""} | ConvertTo-Json
$reqResp = Invoke-WebRequest -Uri "$API_BASE/friends/request" -Method Post -Body $reqBody -Headers $headers1 -ContentType "application/json" -UseBasicParsing
Write-Host "[OK] 发送成功: $($reqResp.Content)" -ForegroundColor Green

# 登录 iwanna2 查询请求
Write-Host "[INFO] 登录 iwanna2 (密码: 123456)" -ForegroundColor Cyan
$body2 = @{username="iwanna2";password="123456"} | ConvertTo-Json
$resp2 = Invoke-WebRequest -Uri "$API_BASE/user/login" -Method Post -Body $body2 -ContentType "application/json" -UseBasicParsing
$json2 = $resp2.Content | ConvertFrom-Json
$token2 = $json2.data.token
Write-Host "[OK] 登录成功" -ForegroundColor Green

# 查询好友请求（检查ID是否为字符串）
Write-Host "[INFO] 查询好友请求" -ForegroundColor Cyan
$headers2 = @{Authorization = "Bearer $token2"}
$reqs = Invoke-WebRequest -Uri "$API_BASE/friends/requests" -Method Get -Headers $headers2 -UseBasicParsing
Write-Host "[OK] 好友请求: $($reqs.Content)" -ForegroundColor Green

# 尝试同意
$reqsJson = $reqs.Content | ConvertFrom-Json
if ($reqsJson.data.Length -gt 0) {
    $requestId = $reqsJson.data[0].id
    Write-Host "[INFO] 同意请求，ID类型: $($requestId.GetType().Name), 值: $requestId" -ForegroundColor Cyan
    
    $acceptResp = Invoke-WebRequest -Uri "$API_BASE/friends/request/$requestId/accept" -Method Post -Headers $headers2 -ContentType "application/json" -UseBasicParsing
    Write-Host "[OK] 同意成功: $($acceptResp.Content)" -ForegroundColor Green
}

Write-Host "=== 测试完成 ===" -ForegroundColor Green
