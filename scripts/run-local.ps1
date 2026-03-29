param(
    [switch]$WithFrontend
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot

Write-Host 'Starting travel_ai_service on http://localhost:7001'
Start-Process powershell -ArgumentList '-NoExit', '-Command', "Set-Location '$root'; mvn -pl travel_ai_service spring-boot:run"

Start-Sleep -Seconds 3

Write-Host 'Starting gateway-service on http://localhost:7000'
Start-Process powershell -ArgumentList '-NoExit', '-Command', "Set-Location '$root'; mvn -pl gateway-service spring-boot:run"

if ($WithFrontend) {
    Start-Sleep -Seconds 3
    Write-Host 'Starting frontend on http://localhost:7002'
    Start-Process powershell -ArgumentList '-NoExit', '-Command', "Set-Location '$root\\frontend'; npm run dev"
}

Write-Host 'Local services started.'
