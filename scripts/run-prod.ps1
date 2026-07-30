# Production startup script for Travel Planner backend
# This script requires the production environment variables to be set.

if (-not $env:DB_URL) {
    Write-Error "Missing DB_URL environment variable"
    exit 1
}
if (-not $env:DB_USERNAME) {
    Write-Error "Missing DB_USERNAME environment variable"
    exit 1
}
if (-not $env:DB_PASSWORD) {
    Write-Error "Missing DB_PASSWORD environment variable"
    exit 1
}
if (-not $env:JWT_SECRET) {
    Write-Error "Missing JWT_SECRET environment variable"
    exit 1
}
if (-not $env:ORS_API_KEY) {
    Write-Error "Missing ORS_API_KEY environment variable"
    exit 1
}
if (-not $env:OPENWEATHERMAP_API_KEY) {
    Write-Error "Missing OPENWEATHERMAP_API_KEY environment variable"
    exit 1
}

if (-not $env:SPRING_PROFILES_ACTIVE) {
    $env:SPRING_PROFILES_ACTIVE = 'prod'
}

Write-Host "Starting Travel Planner backend in prod profile..."
Write-Host "DB_URL=$env:DB_URL"
Write-Host "JWT_SECRET=********"
Write-Host "ORS_API_KEY=********"
Write-Host "OPENWEATHERMAP_API_KEY=********"

& .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod
