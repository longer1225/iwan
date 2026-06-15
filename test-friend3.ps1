#!/usr/bin/env pwsh
$API_BASE = "http://localhost:8080/iwan/api/v1"

# 登录 iwanna
$body = @{username="iwanna";password="password"} | ConvertTo-Json
Write-Host "Login body: $body"
$resp = Invoke-WebRequest -Uri "$API_BASE/user/login" -Method Post -Body $body -ContentType "application/json" -UseBasicParsing
Write-Host "Response: $($resp.Content)"
$json = $resp.Content | ConvertFrom-Json
$token = $json.data.token
$userId = $json.data.userId
Write-Host "Token: $token, UserId: $userId"

# 登录 iwanna2
$body2 = @{username="iwanna2";password="password"} | ConvertTo-Json
$resp2 = Invoke-WebRequest -Uri "$API_BASE/user/login" -Method Post -Body $body2 -ContentType "application/json" -UseBasicParsing
Write-Host "Response2: $($resp2.Content)"
$json2 = $resp2.Content | ConvertFrom-Json
$token2 = $json2.data.token
$userId2 = $json2.data.userId
Write-Host "Token2: $token2, UserId2: $userId2"

# 发送好友请求
$headersA = @{Authorization="Bearer $token"}
$reqBody = @{targetUserId=[long]$userId2;message="你好"} | ConvertTo-Json
Write-Host "Request body: $reqBody"
try {
    $reqResp = Invoke-WebRequest -Uri "$API_BASE/friends/request" -Method Post -Body $reqBody -Headers $headersA -ContentType "application/json" -UseBasicParsing
    Write-Host "Send Result: $($reqResp.Content)"
} catch {
    Write-Host "Send Error: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "Error Body: $($reader.ReadToEnd())"
    }
}

# 查询好友请求
$headersB = @{Authorization="Bearer $token2"}
try {
    $reqsResp = Invoke-WebRequest -Uri "$API_BASE/friends/requests" -Method Get -Headers $headersB -UseBasicParsing
    Write-Host "Friend Requests: $($reqsResp.Content)"
} catch {
    Write-Host "Get Error: $($_.Exception.Message)"
}
