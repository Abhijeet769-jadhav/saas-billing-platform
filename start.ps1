<#
.SYNOPSIS
    SaaS Billing Platform — One-Click Start Script (Windows)
.DESCRIPTION
    Starts the full billing platform locally using Docker Compose.
    Run this from the project root folder.
#>

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "╔══════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   SaaS Billing Platform — Starting Up...     ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# ── Check Docker ──────────────────────────────────
Write-Host "→ Checking Docker..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version 2>&1
    Write-Host "  ✅ Docker found: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "  ❌ Docker not found! Please install Docker Desktop from:" -ForegroundColor Red
    Write-Host "     https://www.docker.com/products/docker-desktop" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Check Docker is running
try {
    docker ps 2>&1 | Out-Null
    Write-Host "  ✅ Docker daemon is running" -ForegroundColor Green
} catch {
    Write-Host "  ❌ Docker is not running! Please start Docker Desktop first." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# ── Setup .env ────────────────────────────────────
if (-not (Test-Path ".env")) {
    Write-Host ""
    Write-Host "→ Creating .env from .env.example..." -ForegroundColor Yellow
    Copy-Item ".env.example" ".env"
    Write-Host "  ✅ .env file created" -ForegroundColor Green
    Write-Host "  ℹ  You can edit .env to configure SMTP email, Stripe, etc." -ForegroundColor Cyan
} else {
    Write-Host "→ .env file exists ✅" -ForegroundColor Green
}

# ── Start Services ────────────────────────────────
Write-Host ""
Write-Host "→ Starting all services (this may take a few minutes on first run)..." -ForegroundColor Yellow
Write-Host "  Building Docker images + downloading dependencies..." -ForegroundColor Gray

docker-compose up --build -d 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "  ❌ Docker Compose failed. Check the output above for errors." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# ── Wait for Backend ──────────────────────────────
Write-Host ""
Write-Host "→ Waiting for backend to become healthy..." -ForegroundColor Yellow
$maxWait = 120
$waited = 0
$healthy = $false

while ($waited -lt $maxWait) {
    Start-Sleep -Seconds 3
    $waited += 3
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/plans" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $healthy = $true
            break
        }
    } catch { }
    Write-Host "  ... waiting ($waited/$maxWait seconds)" -ForegroundColor Gray
}

# ── Results ───────────────────────────────────────
Write-Host ""
if ($healthy) {
    Write-Host "╔══════════════════════════════════════════════╗" -ForegroundColor Green
    Write-Host "║   ✅ Platform is LIVE!                        ║" -ForegroundColor Green
    Write-Host "╠══════════════════════════════════════════════╣" -ForegroundColor Green
    Write-Host "║                                              ║" -ForegroundColor Green
    Write-Host "║   🌐 Frontend:  http://localhost:3000        ║" -ForegroundColor Green
    Write-Host "║   ⚙️  Backend:   http://localhost:8080        ║" -ForegroundColor Green
    Write-Host "║   📖 Swagger:   http://localhost:8080/swagger-ui.html ║" -ForegroundColor Green
    Write-Host "║                                              ║" -ForegroundColor Green
    Write-Host "║   👤 Admin Login:                            ║" -ForegroundColor Green
    Write-Host "║      Email:    admin@billing.local           ║" -ForegroundColor Green
    Write-Host "║      Password: Admin@123                     ║" -ForegroundColor Green
    Write-Host "║                                              ║" -ForegroundColor Green
    Write-Host "╚══════════════════════════════════════════════╝" -ForegroundColor Green
    
    # Open browser
    Write-Host ""
    Write-Host "→ Opening browser..." -ForegroundColor Yellow
    Start-Process "http://localhost:3000"
} else {
    Write-Host "  ⚠️  Backend took longer than expected." -ForegroundColor Yellow
    Write-Host "     Try opening http://localhost:3000 in a minute." -ForegroundColor Yellow
    Write-Host "     Check logs with: docker-compose logs backend" -ForegroundColor Gray
}

Write-Host ""
Write-Host "  To stop:    .\stop.ps1  OR  docker-compose down" -ForegroundColor Gray
Write-Host "  View logs:  docker-compose logs -f" -ForegroundColor Gray
Write-Host ""
