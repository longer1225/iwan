#!/usr/bin/env pwsh
# 好友请求功能完整测试脚本
# 使用方法: pwsh test-friend.ps1

$ErrorActionPreference = "Stop"
$API_BASE = "http://localhost:8080/iwan/api/v1"

# 颜色函数
function Write-Success($msg) { Write-Host "[OK] $msg" -ForegroundColor Green }
function Write-Info($msg) { Write-Host "[INFO] $msg" -ForegroundColor Cyan }
function Write-Err($msg) { Write-Host "[ERROR] $msg" -ForegroundColor Red }
function Write-Test($msg) { Write-Host "`n=== $msg ===" -ForegroundColor Yellow }

# 1. 注册用户A
Write-Test "Step 1: 注册用户A (iwanna)"
$bodyA = @{
    username = "iwanna"
    password = "password123"
    nickname = "用户A"
} | ConvertTo-Json
try {
    $rA = Invoke-WebRequest -Uri "$API_BASE/user/register" -Method Post -Body $bodyA -ContentType "application/json" -UseBasicParsing
    Write-Success "用户A注册成功"
} catch {
    Write-Info "用户A可能已存在，继续"
}

# 2. 注册用户B
Write-Test "Step 2: 注册用户B (iwanna2)"
$bodyB = @{
    username = "iwanna2"
    password = "password123"
    nickname = "用户B"
} | ConvertTo-Json
try {
    $rB = Invoke-WebRequest -Uri "$API_BASE/user/register" -Method Post -Body $bodyB -ContentType "application/json" -UseBasicParsing
    Write-Success "用户B注册成功"
} catch {
    Write-Info "用户B可能已存在，继续"
}

# 3. 登录用户A
Write-Test "Step 3: 登录用户A"
$loginA = @{
    username = "iwanna"
    password = "password123"
} | ConvertTo-Json
$respA = Invoke-WebRequest -Uri "$API_BASE/user/login" -Method Post -Body $loginA -ContentType "application/json" -UseBasicParsing
$jsonA = $respA.Content | ConvertFrom-Json
$tokenA = $jsonA.data.token
$userIdA = $jsonA.data.userId
Write-Success "用户A登录成功 - ID: $userIdA"

# 4. 登录用户B
Write-Test "Step 4: 登录用户B"
$loginB = @{
    username = "iwanna2"
    password = "password123"
} | ConvertTo-Json
$respB = Invoke-WebRequest -Uri "$API_BASE/user/login" -Method Post -Body $loginB -ContentType "application/json" -UseBasicParsing
$jsonB = $respB.Content | ConvertFrom-Json
$tokenB = $jsonB.data.token
$userIdB = $jsonB.data.userId
Write-Success "用户B登录成功 - ID: $userIdB"

# 5. 用户A搜索用户B
Write-Test "Step 5: 用户A搜索用户B"
$headersA = @{ Authorization = "Bearer $tokenA" }
$searchResp = Invoke-WebRequest -Uri "$API_BASE/search?keyword=iwanna2&type=user" -Method Get -Headers $headersA -UseBasicParsing
Write-Success "搜索结果: $($searchResp.Content)"

# 6. 用户A向用户B发送好友请求
Write-Test "Step 6: 用户A向用户B发送好友请求"
$reqBody = @{
    targetUserId = $userIdB
    message = "你好，加个好友吧"
} | ConvertTo-Json
$reqResp = Invoke-WebRequest -Uri "$API_BASE/friends/request" -Method Post -Body $reqBody -Headers $headersA -ContentType "application/json" -UseBasicParsing
Write-Success "发送请求结果: $($reqResp.Content)"

# 7. 用户B查看好友请求
Write-Test "Step 7: 用户B查看待处理的好友请求"
$headersB = @{ Authorization = "Bearer $tokenB" }
$reqsResp = Invoke-WebRequest -Uri "$API_BASE/friends/requests" -Method Get -Headers $headersB -UseBasicParsing
Write-Success "好友请求列表: $($reqsResp.Content)"

Write-Host "`n=== 测试完成 ===" -ForegroundColor Yellow
