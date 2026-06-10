
@echo off
powershell -Command "
Write-Host '=========================================='
Write-Host '     Iwan Blog 开发环境一键启动脚本'
Write-Host '=========================================='
Write-Host ''

Write-Host '[1/4] 检查Docker服务状态...'
$dockerRunning = $false
try {
    docker info | Out-Null
    $dockerRunning = $true
} catch {
    Write-Host 'Docker 未运行，尝试启动...'
    $dockerPath = 'C:\\Program Files\\Docker\\Docker\\Docker Desktop.exe'
    if (Test-Path $dockerPath) {
        Start-Process -FilePath $dockerPath -NoNewWindow
        Write-Host '等待Docker启动...'
        Start-Sleep -Seconds 30
        try {
            docker info | Out-Null
            $dockerRunning = $true
        } catch {
            Write-Host 'Docker启动失败'
            exit 1
        }
    }
}

if ($dockerRunning) {
    Write-Host 'Docker服务已运行'
}

Write-Host ''
Write-Host '[2/4] 启动Docker Compose服务...'
Set-Location 'd:\\Iwan'
docker-compose up -d
Write-Host 'Docker服务启动成功'

Write-Host ''
Write-Host '等待数据库初始化...'
Start-Sleep -Seconds 10

Write-Host ''
Write-Host '[3/4] 启动后端Spring Boot服务...'
Start-Process -FilePath 'mvn' -ArgumentList 'spring-boot:run' -WorkingDirectory 'd:\\Iwan\\backend'
Write-Host '后端服务启动中...'

Write-Host '等待后端服务初始化...'
Start-Sleep -Seconds 15

Write-Host ''
Write-Host '[4/4] 启动前端Vite开发服务器...'
Start-Process -FilePath 'npm' -ArgumentList 'run dev' -WorkingDirectory 'd:\\Iwan\\frontend'
Write-Host '前端服务启动中...'

Write-Host ''
Write-Host '=========================================='
Write-Host '开发环境启动完成！'
Write-Host '=========================================='
Write-Host ''
Write-Host '服务地址：'
Write-Host ' 前端: http://localhost:5173'
Write-Host '后端: http://localhost:8080'
"
