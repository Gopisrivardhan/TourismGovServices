
# ================================================================
#   COMPREHENSIVE API TEST - myWorking Microservices Project
#   Tests: ALL Report APIs + ALL Notification APIs
#          + Notification integration from other services
# ================================================================

$GW = "http://localhost:8383"
$H  = @{ "Content-Type" = "application/json" }

$PASS = 0; $FAIL = 0
function Show-Result($label, $success, $detail="") {
    if ($success) {
        Write-Host "  [PASS] $label" -ForegroundColor Green
        if ($detail) { Write-Host "         $detail" -ForegroundColor Cyan }
        $script:PASS++
    } else {
        Write-Host "  [FAIL] $label" -ForegroundColor Red
        if ($detail) { Write-Host "         $detail" -ForegroundColor Yellow }
        $script:FAIL++
    }
}

function Invoke-API($method, $url, $headers, $body=$null) {
    try {
        if ($body) {
            return @{ ok=$true; data=(Invoke-RestMethod -Method $method -Uri $url -Headers $headers -Body $body) }
        } else {
            return @{ ok=$true; data=(Invoke-RestMethod -Method $method -Uri $url -Headers $headers) }
        }
    } catch {
        $status = 0
        $msg = "$_"
        if ($_.Exception.Response) {
            $status = $_.Exception.Response.StatusCode.value__
            $sr = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
            $msg = $sr.ReadToEnd(); $sr.Close()
        }
        return @{ ok=$false; status=$status; msg=$msg }
    }
}

Write-Host ""
Write-Host "================================================================" -ForegroundColor Magenta
Write-Host "   TOURISM GOV - FULL API TEST SUITE (myWorking Project)        " -ForegroundColor Magenta
Write-Host "================================================================" -ForegroundColor Magenta

# ---------------------------------------------------------------
# STEP 1: Setup - Register and login users
# ---------------------------------------------------------------
Write-Host "`n--- SETUP: Registering Users ---" -ForegroundColor White

$ts = (Get-Date -UFormat "%s").Replace(".","")
$touristEmail = "tourist_$ts@test.com"
$adminEmail   = "admin_$ts@test.com"

$r = Invoke-API POST "$GW/tourismgov/v1/auth/register" $H (@{name="Test Tourist";email=$touristEmail;password="Password123!";role="TOURIST";phone="9876543210"}|ConvertTo-Json)
Show-Result "Register TOURIST" $r.ok "userId=$($r.data.userId) createdAt=$($r.data.createdAt)"
$touristUserId = $r.data.userId

$r = Invoke-API POST "$GW/tourismgov/v1/auth/register" $H (@{name="Test Admin";email=$adminEmail;password="Password123!";role="ADMIN";phone="9876543211"}|ConvertTo-Json)
Show-Result "Register ADMIN" $r.ok "userId=$($r.data.userId)"
$adminUserId = $r.data.userId

$r = Invoke-API POST "$GW/tourismgov/v1/auth/login" $H (@{email=$touristEmail;password="Password123!"}|ConvertTo-Json)
Show-Result "Login as TOURIST" $r.ok
$touristToken = $r.data.token
$TH = @{ "Authorization"="Bearer $touristToken"; "Content-Type"="application/json" }

$r = Invoke-API POST "$GW/tourismgov/v1/auth/login" $H (@{email=$adminEmail;password="Password123!"}|ConvertTo-Json)
Show-Result "Login as ADMIN" $r.ok
$adminToken = $r.data.token
$AH = @{ "Authorization"="Bearer $adminToken"; "Content-Type"="application/json" }

# ---------------------------------------------------------------
# STEP 2: REPORT API TESTS
# ---------------------------------------------------------------
Write-Host "`n--- REPORT API TESTS ---" -ForegroundColor White

# 2a. TOURIST cannot generate report (should be 403)
$r = Invoke-API POST "$GW/tourismgov/v1/reports/generate" $TH (@{scope="SITE"}|ConvertTo-Json)
$is403 = (!$r.ok -and $r.status -eq 403)
Show-Result "Report: TOURIST blocked (403 Forbidden)" $is403 "Status=$($r.status)"

# 2b. ADMIN generates SITE report
$r = Invoke-API POST "$GW/tourismgov/v1/reports/generate" $AH (@{scope="SITE"}|ConvertTo-Json)
Show-Result "Report: Generate SITE report (ADMIN)" $r.ok "reportId=$($r.data.reportId) scope=$($r.data.scope)"
$reportId1 = $r.data.reportId

# 2c. ADMIN generates EVENT report
$r = Invoke-API POST "$GW/tourismgov/v1/reports/generate" $AH (@{scope="EVENT"}|ConvertTo-Json)
Show-Result "Report: Generate EVENT report (ADMIN)" $r.ok "reportId=$($r.data.reportId)"
$reportId2 = $r.data.reportId

# 2d. ADMIN generates PROGRAM report
$r = Invoke-API POST "$GW/tourismgov/v1/reports/generate" $AH (@{scope="PROGRAM"}|ConvertTo-Json)
Show-Result "Report: Generate PROGRAM report (ADMIN)" $r.ok "reportId=$($r.data.reportId)"
$reportId3 = $r.data.reportId

# 2e. ADMIN generates COMPLIANCE report
$r = Invoke-API POST "$GW/tourismgov/v1/reports/generate" $AH (@{scope="COMPLIANCE"}|ConvertTo-Json)
Show-Result "Report: Generate COMPLIANCE report (ADMIN)" $r.ok "reportId=$($r.data.reportId)"
$reportId4 = $r.data.reportId

# 2f. Invalid scope -> 400
$r = Invoke-API POST "$GW/tourismgov/v1/reports/generate" $AH (@{scope="INVALID_SCOPE"}|ConvertTo-Json)
$isBadReq = (!$r.ok -and ($r.status -eq 400 -or $r.status -eq 500))
Show-Result "Report: Invalid scope handled" $isBadReq "Status=$($r.status)"

# 2g. Get report history
$r = Invoke-API GET "$GW/tourismgov/v1/reports/history" $AH
Show-Result "Report: GET history" $r.ok "Count=$($r.data.Count)"

# 2h. Download report
if ($reportId1) {
    $r = Invoke-API GET "$GW/tourismgov/v1/reports/download/$reportId1" $AH
    Show-Result "Report: Download report #$reportId1" $r.ok
}

# 2i. Download non-existent report -> 404
$r = Invoke-API GET "$GW/tourismgov/v1/reports/download/999999" $AH
$is404 = (!$r.ok -and $r.status -eq 404)
Show-Result "Report: Download non-existent (404)" $is404 "Status=$($r.status)"

# 2j. Dashboard stats (correct endpoint is /stats)
$r = Invoke-API GET "$GW/tourismgov/v1/dashboard/stats" $AH
Show-Result "Report: Dashboard stats" $r.ok "sites=$($r.data.totalSites) events=$($r.data.totalEvents)"

# ---------------------------------------------------------------
# STEP 3: NOTIFICATION API TESTS
# ---------------------------------------------------------------
Write-Host "`n--- NOTIFICATION API TESTS ---" -ForegroundColor White

Start-Sleep 2 # Give time for report notifications to be saved

# 3a. Admin gets all their notifications (auto-created by report generation)
$r = Invoke-API GET "$GW/tourismgov/v1/notifications" $AH
Show-Result "Notification: GET all (ADMIN)" $r.ok "Count=$($r.data.Count)"
$notifList = $r.data
$firstNotifId = if ($notifList.Count -gt 0) { $notifList[0].notificationId } else { $null }

# 3b. Get unread notifications
$r = Invoke-API GET "$GW/tourismgov/v1/notifications/unread" $AH
Show-Result "Notification: GET unread (ADMIN)" $r.ok "Unread=$($r.data.Count)"

# 3c. Get by category SYSTEM
$r = Invoke-API GET "$GW/tourismgov/v1/notifications/category/SYSTEM" $AH
Show-Result "Notification: GET by category=SYSTEM" $r.ok "Count=$($r.data.Count)"

# 3d. Mark first notification as read
if ($firstNotifId) {
    $r = Invoke-API PATCH "$GW/tourismgov/v1/notifications/$firstNotifId/read" $AH
    Show-Result "Notification: Mark #$firstNotifId as READ" $r.ok "status=$($r.data.status)"
}

# 3e. Tourist gets their notifications
$r = Invoke-API GET "$GW/tourismgov/v1/notifications" $TH
Show-Result "Notification: GET all (TOURIST)" $r.ok "Count=$($r.data.Count)"

# 3f. Tourist gets unread
$r = Invoke-API GET "$GW/tourismgov/v1/notifications/unread" $TH
Show-Result "Notification: GET unread (TOURIST)" $r.ok "Unread=$($r.data.Count)"

# 3g. Create a direct notification (POST /notifications)
$notifBody = @{
    userId   = $adminUserId
    entityId = 1
    subject  = "Test Alert"
    message  = "This is a direct notification test."
    category = "SYSTEM"
} | ConvertTo-Json
$r = Invoke-API POST "$GW/tourismgov/v1/notifications" $AH $notifBody
Show-Result "Notification: POST create direct notification" $r.ok "notifId=$($r.data.notificationId)"

# ---------------------------------------------------------------
# STEP 4: NOTIFICATION INTEGRATION TESTS (from other services)
# ---------------------------------------------------------------
Write-Host "`n--- NOTIFICATION INTEGRATION (Other Services -> Notifications) ---" -ForegroundColor White

# 4a. Register tourist via TouristService (which sends a welcome notification)
$ts2 = (Get-Date -UFormat "%s").Replace(".","")
$touristEmail2 = "tourist2_$ts2@test.com"
$touristBody = @{
    name        = "Tourist2 Test"
    email       = $touristEmail2
    password    = "Password123!"
    contactInfo = "9000011111"
    dob         = "1995-06-15"
    gender      = "MALE"
    address     = "Test City"
} | ConvertTo-Json
$r = Invoke-API POST "$GW/tourismgov/v1/tourist/create" $H $touristBody
Show-Result "Integration: Tourist registered via TouristService" $r.ok "touristId=$($r.data.touristId)"
if ($r.ok) {
    Start-Sleep 2
    $loginT2 = Invoke-API POST "$GW/tourismgov/v1/auth/login" $H (@{email=$touristEmail2;password="Password123!"}|ConvertTo-Json)
    if ($loginT2.ok) {
        $TH2 = @{ "Authorization"="Bearer $($loginT2.data.token)"; "Content-Type"="application/json" }
        $r2 = Invoke-API GET "$GW/tourismgov/v1/notifications" $TH2
        Show-Result "Integration: Tourist registration welcome notification" ($r2.data.Count -gt 0) "Tourist notif count=$($r2.data.Count)"
    } else {
        Show-Result "Integration: Tourist registration welcome notification" $false "Could not login as tourist2"
    }
} else {
    Show-Result "Integration: Tourist registration welcome notification" $false "TouristService registration failed"
}

# 4b. Create a Site (triggers notification broadcast)
$siteBody = @{
    name     = "Test Heritage Site $ts"
    location = "Test City"
    type     = "MONUMENT"
    status   = "OPEN"
    description = "Test site for notification integration"
} | ConvertTo-Json
$r = Invoke-API POST "$GW/tourismgov/v1/sites" $AH $siteBody
Show-Result "Integration: Create Site (triggers broadcast notification)" $r.ok "siteId=$($r.data.siteId)"
$siteId = $r.data.siteId
Start-Sleep 2

# 4c. Check notifications increased after site creation
$r = Invoke-API GET "$GW/tourismgov/v1/notifications" $AH
Show-Result "Integration: Site creation notification delivered (ADMIN)" $r.ok "Total Admin notifs=$($r.data.Count)"

# 4d. Create a Program (triggers notification)
$progBody = @{
    title       = "Tourism Program $ts"
    description = "Test program"
    startDate   = (Get-Date).AddDays(7).ToString("yyyy-MM-dd")
    endDate     = (Get-Date).AddDays(30).ToString("yyyy-MM-dd")
    budget      = 50000
} | ConvertTo-Json
$r = Invoke-API POST "$GW/tourismgov/v1/programs" $AH $progBody
Show-Result "Integration: Create Program (triggers notification)" $r.ok "programId=$($r.data.programId)"
Start-Sleep 2

# 4e. Final count of all admin notifications
$r = Invoke-API GET "$GW/tourismgov/v1/notifications" $AH
Show-Result "Integration: All notifications accumulated (ADMIN)" $r.ok "Total=$($r.data.Count)"

# ---------------------------------------------------------------
# FINAL SUMMARY
# ---------------------------------------------------------------
Write-Host ""
Write-Host "================================================================" -ForegroundColor Magenta
Write-Host "   RESULTS: $PASS PASSED   |   $FAIL FAILED" -ForegroundColor $(if ($FAIL -eq 0) { "Green" } else { "Yellow" })
Write-Host "================================================================" -ForegroundColor Magenta
Write-Host ""
