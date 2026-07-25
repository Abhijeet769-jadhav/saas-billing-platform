<#
.SYNOPSIS
    SaaS Billing Platform — Graceful Stop Script (Windows)
#>

Write-Host ""
Write-Host "→ Stopping SaaS Billing Platform..." -ForegroundColor Yellow
docker-compose down
Write-Host "  ✅ All services stopped." -ForegroundColor Green
Write-Host ""
