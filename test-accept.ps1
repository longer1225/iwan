#!/usr/bin/env pwsh
$API_BASE = "http://localhost:8080/iwan/api/v1"

# 登录 iwanna2
Write-Host "[INFO] 登录 iwanna2 (密码: 123456)" -ForegroundColor Cyan
$body = @{username="iwanna2";password="123456"} | ConvertTo-Json
$resp = Invoke-WebRequest -Uri "$API_BASE/user/login" -Method Post -Body $body -ContentType "application/json" -UseBasicParsing
$json = $resp.Content | ConvertFrom-Json
$token = $json.data.token
$userId = $json.data.userId
Write-Host "[OK] 登录成功 - ID: $userId" -ForegroundColor Green

# 查询好友请求
Write-Host "[INFO] 查询好友请求" -ForegroundColor Cyan
$headers = @{Authorization = "Bearer $token"}
$reqs = Invoke-WebRequest -Uri "$API_BASE/friends/requests" -Method Get -Headers $headers -UseBasicParsing
$reqsJson = $reqs.Content | ConvertFrom-Json
Write-Host "[OK] 好友请求列表: $($reqs.Content)" -ForegroundColor Green

if ($reqsJson.data.Length -gt 0) {
    $requestId = $reqsJson.data[0].id
    Write-Host "[INFO] 尝试同意请求 ID: $requestId" -ForegroundColor Cyan
    
    # 尝试同意
    try {
        $acceptResp = Invoke-WebRequest -Uri "$API_BASE/friends/request/$requestId/accept" -Method Post -Headers $headers -ContentType "application/json" -UseBasicParsing
        Write-Host "[OK] 同意成功: $($acceptResp.Content)" -ForegroundColor Green
    } catch {
        Write-Host "[ERR] 同意失败: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            Write-Host "Error Body: $($reader.ReadToEnd())" -ForegroundColor Red
        }
    }
} else {
    Write-Host "[INFO] 没有待处理的好友请求" -ForegroundColor Yellow
}

Write-Host "=== 测试完成 ===" -ForegroundColor Green
