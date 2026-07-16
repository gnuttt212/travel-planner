$baseUrl = "http://localhost:8080"
$results = @()

function Test-Endpoint {
    param([string]$Method, [string]$Url, [string]$Body, [string]$Token, [string]$Description)
    
    Write-Host "`n=== TEST: $Description ===" -ForegroundColor Cyan
    Write-Host "$Method $Url"
    
    $params = @{
        Uri = $Url
        Method = $Method
        UseBasicParsing = $true
        TimeoutSec = 10
        ContentType = "application/json"
    }
    if ($Body) { $params.Body = [System.Text.Encoding]::UTF8.GetBytes($Body) }
    if ($Token) { $params.Headers = @{ "Authorization" = "Bearer $Token" } }
    
    try {
        $response = Invoke-WebRequest @params
        $status = $response.StatusCode
        $content = [System.Text.Encoding]::UTF8.GetString($response.Content)
        Write-Host "STATUS: $status" -ForegroundColor Green
        Write-Host "BODY: $($content.Substring(0, [Math]::Min(500, $content.Length)))"
        return @{ Status = $status; Content = $content; Success = $true }
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        $errBody = ""
        try {
            $sr = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
            $errBody = $sr.ReadToEnd()
            $sr.Close()
        } catch {}
        Write-Host "STATUS: $status" -ForegroundColor Red
        Write-Host "ERROR: $errBody"
        return @{ Status = $status; Content = $errBody; Success = $false }
    }
}

# ======== 1. HEALTH CHECK ========
Test-Endpoint -Method "GET" -Url "$baseUrl/actuator/health" -Description "Health Check"

# ======== 2. SWAGGER UI ========
Test-Endpoint -Method "GET" -Url "$baseUrl/swagger-ui/index.html" -Description "Swagger UI"

# ======== 3. AUTH - Register ========
$regBody = '{"username":"testuser","email":"test@example.com","password":"Test1234!"}'
$regResult = Test-Endpoint -Method "POST" -Url "$baseUrl/api/auth/register" -Body $regBody -Description "Auth Register"

# ======== 4. AUTH - Login ========
$loginBody = '{"username":"testuser","password":"Test1234!"}'
$loginResult = Test-Endpoint -Method "POST" -Url "$baseUrl/api/auth/login" -Body $loginBody -Description "Auth Login"

$token = ""
if ($loginResult.Success) {
    try {
        $loginData = $loginResult.Content | ConvertFrom-Json
        if ($loginData.data -and $loginData.data.token) {
            $token = $loginData.data.token
        } elseif ($loginData.token) {
            $token = $loginData.token
        }
        Write-Host "TOKEN: $($token.Substring(0, [Math]::Min(50, $token.Length)))..." -ForegroundColor Yellow
    } catch {
        Write-Host "Could not parse token from login response" -ForegroundColor Red
    }
}

# ======== 5. GET Destinations (no auth) ========
Test-Endpoint -Method "GET" -Url "$baseUrl/api/destinations" -Token $token -Description "Get All Destinations"

# ======== 6. GET single destination ========
Test-Endpoint -Method "GET" -Url "$baseUrl/api/destinations" -Token $token -Description "Get Destinations List for ID"

# ======== 7. POST Create Destination ========
$destBody = '{"name":"Test Beach","country":"Vietnam","description":"A beautiful beach","tags":["beach","relax"],"bestMonths":[6,7,8],"avgCostPerDay":500000}'
Test-Endpoint -Method "POST" -Url "$baseUrl/api/destinations" -Body $destBody -Token $token -Description "Create Destination"

# ======== 8. Recommend Destinations ========
Test-Endpoint -Method "GET" -Url "$baseUrl/api/destinations/recommend?maxBudgetPerDay=2000000&travelMonth=6" -Token $token -Description "Recommend Destinations"

# ======== 9. Budget endpoints ========
$budgetBody = '{"name":"Summer Trip 2026","totalAmount":10000000,"currency":"VND"}'
Test-Endpoint -Method "POST" -Url "$baseUrl/api/budgets" -Body $budgetBody -Token $token -Description "Create Budget"

Test-Endpoint -Method "GET" -Url "$baseUrl/api/budgets" -Token $token -Description "Get All Budgets"

# ======== 10. Routes ========
Test-Endpoint -Method "GET" -Url "$baseUrl/api/routes/optimize?fromLat=10.762&fromLng=106.660&toLat=10.780&toLng=106.700" -Token $token -Description "Optimize Route"

# ======== 11. Itineraries ========
Test-Endpoint -Method "GET" -Url "$baseUrl/api/itineraries" -Token $token -Description "Get Itineraries"

# ======== 12. Collaborative Recommendations ========
Test-Endpoint -Method "GET" -Url "$baseUrl/api/recommendations/collaborative" -Token $token -Description "Collaborative Recommendations"

# ======== 13. Export PDF ========
Test-Endpoint -Method "GET" -Url "$baseUrl/api/export/itinerary/pdf" -Token $token -Description "Export Itinerary PDF"

Write-Host "`n`n======== TEST SUMMARY ========" -ForegroundColor Magenta
Write-Host "All tests completed!" -ForegroundColor Green
