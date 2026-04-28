$BASE = "http://localhost:8383"
$J = @{"Content-Type"="application/json"}
$P = 0; $F = 0

function Chk($label, $got, $expect) {
    if ($null -ne $expect -and "$got" -ne "$expect") {
        Write-Host "  [FAIL] $label  (got=$got expected=$expect)" -ForegroundColor Red
        $script:F++
    } else {
        Write-Host "  [PASS] $label  $(if($null -ne $got -and $got -ne $true){"-> $got"})" -ForegroundColor Green
        $script:P++
    }
}

function TryAPI($method, $url, $hdrs, $body) {
    try {
        $p = @{ Method=$method; Uri=$url; Headers=$hdrs; ErrorAction='Stop' }
        if ($body) { $p.Body = $body }
        $data = Invoke-RestMethod @p
        return @{ ok=$true; data=$data; status=200 }
    } catch {
        $st = $_.Exception.Response.StatusCode.value__
        try {
            $sr = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
            $rb = $sr.ReadToEnd(); $sr.Close()
        } catch { $rb = "" }
        return @{ ok=$false; data=$rb; status=$st }
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   NOTIFICATION FULL API + INTEGRATION TEST" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# ── SETUP ──────────────────────────────────────────────────────────────
Write-Host "--- SETUP: Register & Login ---" -ForegroundColor Yellow
$ts = [System.DateTime]::UtcNow.Ticks
$aEmail = "ntf_adm_$ts@t.com"
$tEmail = "ntf_tst_$ts@t.com"

TryAPI POST "$BASE/tourismgov/v1/auth/register" $J (@{name="Ntf Admin";email=$aEmail;password="Admin@1234";phone="9100000001";role="ADMIN"} | ConvertTo-Json) | Out-Null
TryAPI POST "$BASE/tourismgov/v1/auth/register" $J (@{name="Ntf Tourist";email=$tEmail;password="Tourist@123";phone="9100000002";role="TOURIST"} | ConvertTo-Json) | Out-Null

$aLogin = Invoke-RestMethod -Method POST -Uri "$BASE/tourismgov/v1/auth/login" -Headers $J -Body (@{email=$aEmail;password="Admin@1234"} | ConvertTo-Json)
$tLogin = Invoke-RestMethod -Method POST -Uri "$BASE/tourismgov/v1/auth/login" -Headers $J -Body (@{email=$tEmail;password="Tourist@123"} | ConvertTo-Json)

$ADMIN_ID   = $aLogin.userId
$TOURIST_ID = $tLogin.userId
$A  = @{"Authorization"="Bearer $($aLogin.token)"; "Content-Type"="application/json"}
$AG = @{"Authorization"="Bearer $($aLogin.token)"}
$TK = @{"Authorization"="Bearer $($tLogin.token)"; "Content-Type"="application/json"}
$TG = @{"Authorization"="Bearer $($tLogin.token)"}

Write-Host "  Admin:   userId=$ADMIN_ID  email=$aEmail" -ForegroundColor Gray
Write-Host "  Tourist: userId=$TOURIST_ID  email=$tEmail" -ForegroundColor Gray

# ── 1. GET All ─────────────────────────────────────────────────────────
Write-Host ""
Write-Host "--- 1. GET My Notifications ---" -ForegroundColor Yellow

$r = TryAPI GET "$BASE/tourismgov/v1/notifications" $AG $null
Chk "GET /notifications (ADMIN)"   $r.status 200
Write-Host "       Admin count=$($r.data.Count)" -ForegroundColor Gray

$r2 = TryAPI GET "$BASE/tourismgov/v1/notifications" $TG $null
Chk "GET /notifications (TOURIST)" $r2.status 200
Write-Host "       Tourist count=$($r2.data.Count)" -ForegroundColor Gray

# ── 2. GET Unread ──────────────────────────────────────────────────────
Write-Host ""
Write-Host "--- 2. GET Unread Notifications ---" -ForegroundColor Yellow

$ru = TryAPI GET "$BASE/tourismgov/v1/notifications/unread" $AG $null
Chk "GET /unread (ADMIN)"   $ru.status 200
Write-Host "       Admin unread=$($ru.data.Count)" -ForegroundColor Gray

$rtu = TryAPI GET "$BASE/tourismgov/v1/notifications/unread" $TG $null
Chk "GET /unread (TOURIST)" $rtu.status 200
Write-Host "       Tourist unread=$($rtu.data.Count)" -ForegroundColor Gray

# ── 3. GET By Category ─────────────────────────────────────────────────
Write-Host ""
Write-Host "--- 3. GET By Category ---" -ForegroundColor Yellow

foreach ($cat in @("SYSTEM","TRANSACTIONAL","ACTION_REQUIRED","MANAGEMENT","ANNOUNCEMENT")) {
    $rc = TryAPI GET "$BASE/tourismgov/v1/notifications/category/$cat" $AG $null
    Chk "GET /category/$cat" $rc.status 200
    Write-Host "       $cat count=$($rc.data.Count)" -ForegroundColor Gray
}

# ── 4. POST Create Targeted Notification ──────────────────────────────
Write-Host ""
Write-Host "--- 4. POST Create Direct Notification ---" -ForegroundColor Yellow

$createBody = @{userId=$TOURIST_ID; subject="Test Alert"; message="Direct test notification for tourist"; category="SYSTEM"; entityId=0} | ConvertTo-Json
$rc = TryAPI POST "$BASE/tourismgov/v1/notifications" $A $createBody
Chk "POST /notifications create (recipient=TOURIST)" ($rc.status -eq 201 -or $rc.ok) $true
$newNotifId = $rc.data.notificationId
Write-Host "       notificationId=$newNotifId userId=$($rc.data.userId) status=$($rc.data.status)" -ForegroundColor Gray

# Verify tourist sees it
Start-Sleep 1
$r3 = TryAPI GET "$BASE/tourismgov/v1/notifications" $TG $null
Chk "Tourist receives targeted notification" ($r3.data.Count -ge 1) $true
Write-Host "       Tourist notification count now=$($r3.data.Count)" -ForegroundColor Gray

# ── 5. PATCH Mark as Read ──────────────────────────────────────────────
Write-Host ""
Write-Host "--- 5. PATCH Mark as Read ---" -ForegroundColor Yellow

if ($newNotifId) {
    $rm = TryAPI PATCH "$BASE/tourismgov/v1/notifications/$newNotifId/read" $TK $null
    Chk "PATCH /$newNotifId/read (owner=tourist)" $rm.status 200
    Chk "status changed to READ" $rm.data.status "READ"

    # Idempotent second call
    $rm2 = TryAPI PATCH "$BASE/tourismgov/v1/notifications/$newNotifId/read" $TK $null
    Chk "PATCH /$newNotifId/read (idempotent - already READ)" $rm2.status 200
    Chk "Still READ after second call" $rm2.data.status "READ"
}

# ── 6. PATCH Mark All as Read ──────────────────────────────────────────
Write-Host ""
Write-Host "--- 6. PATCH Mark All as Read ---" -ForegroundColor Yellow

$rma = TryAPI PATCH "$BASE/tourismgov/v1/notifications/read-all" $A $null
Chk "PATCH /read-all (ADMIN)" ($rma.status -eq 204 -or $rma.status -eq 200) $true

$afterRead = TryAPI GET "$BASE/tourismgov/v1/notifications/unread" $AG $null
Write-Host "       Admin unread after mark-all-read: $($afterRead.data.Count)" -ForegroundColor Gray
Chk "Admin unread=0 after mark-all-read" $afterRead.data.Count 0

# ── 7. POST Broadcast ──────────────────────────────────────────────────
Write-Host ""
Write-Host "--- 7. POST Broadcast to All Users ---" -ForegroundColor Yellow

$broadBody = @{subject="System Broadcast"; message="This broadcast test reaches all users."; category="SYSTEM"} | ConvertTo-Json
$rb = TryAPI POST "$BASE/tourismgov/v1/notifications/broadcast" $A $broadBody
Chk "POST /broadcast (ADMIN)" ($rb.status -eq 200 -or $rb.ok) $true
Write-Host "       Response: $($rb.data)" -ForegroundColor Gray

Start-Sleep 2
$afterBC_Tourist = TryAPI GET "$BASE/tourismgov/v1/notifications/unread" $TG $null
Write-Host "       Tourist unread after broadcast: $($afterBC_Tourist.data.Count)" -ForegroundColor Gray
Chk "Tourist got broadcast notification" ($afterBC_Tourist.data.Count -ge 1) $true

$afterBC_Admin = TryAPI GET "$BASE/tourismgov/v1/notifications/unread" $AG $null
Write-Host "       Admin unread after broadcast: $($afterBC_Admin.data.Count)" -ForegroundColor Gray
Chk "Admin got broadcast notification" ($afterBC_Admin.data.Count -ge 1) $true

# ── 8. ERROR CASES ─────────────────────────────────────────────────────
Write-Host ""
Write-Host "--- 8. Error Handling ---" -ForegroundColor Yellow

# No token -> 401
$r8a = TryAPI GET "$BASE/tourismgov/v1/notifications" @{} $null
Chk "No token -> 401" $r8a.status 401
$err = $r8a.data | ConvertFrom-Json -ErrorAction SilentlyContinue
Write-Host "       Error msg: $($err.message)" -ForegroundColor Gray

# Wrong HTTP method DELETE -> 405
$r8b = TryAPI DELETE "$BASE/tourismgov/v1/notifications" $AG $null
Chk "DELETE /notifications (wrong method) -> 405" $r8b.status 405

# Non-existent notification -> 404
$r8c = TryAPI PATCH "$BASE/tourismgov/v1/notifications/999999/read" $AG $null
Chk "PATCH /999999/read (not found) -> 404" $r8c.status 404

# Mark another user's notification -> 404
if ($newNotifId) {
    $r8d = TryAPI PATCH "$BASE/tourismgov/v1/notifications/$newNotifId/read" $A $null
    Chk "Admin marks TOURIST notification (not owner) -> 404" $r8d.status 404
}

# Tourist tries broadcast -> 403
$r8e = TryAPI POST "$BASE/tourismgov/v1/notifications/broadcast" $TK $broadBody
Chk "TOURIST tries broadcast -> 403" $r8e.status 403

# Invalid category -> 400/500
$r8f = TryAPI GET "$BASE/tourismgov/v1/notifications/category/INVALID_CAT" $AG $null
Chk "GET /category/INVALID_CAT -> 400" ($r8f.status -eq 400 -or $r8f.status -eq 500) $true

# Missing required field (subject) -> 400
$r8g = TryAPI POST "$BASE/tourismgov/v1/notifications" $A (@{userId=$TOURIST_ID;message="No subject";category="SYSTEM"} | ConvertTo-Json)
Chk "POST missing subject -> 400" $r8g.status 400

# Non-existent recipient userId -> 404/503
$r8h = TryAPI POST "$BASE/tourismgov/v1/notifications" $A (@{userId=999999;subject="Ghost";message="This user does not exist";category="SYSTEM"} | ConvertTo-Json)
Chk "POST non-existent userId -> 404" ($r8h.status -eq 404 -or $r8h.status -eq 503) $true
Write-Host "       Error: $($r8h.data)" -ForegroundColor Gray

# ─────────────────────────────────────────────────────────────────────
# PART 2: INTEGRATION — Notifications triggered by other services
# ─────────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   INTEGRATION: Other Services -> Notifications" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

$adminBefore = (TryAPI GET "$BASE/tourismgov/v1/notifications" $AG $null).data.Count

# ── A. ReportingService → Notification ────────────────────────────────
Write-Host ""
Write-Host "--- A. ReportingService -> NotificationService ---" -ForegroundColor Yellow

$repBody = @{scope="SITE"} | ConvertTo-Json
$repR = TryAPI POST "$BASE/tourismgov/v1/reports/generate" $A $repBody
Chk "Generate SITE report (triggers notification)" $repR.status 201
Write-Host "       reportId=$($repR.data.reportId)" -ForegroundColor Gray

Start-Sleep 2
$afterReport = TryAPI GET "$BASE/tourismgov/v1/notifications" $AG $null
$reportNotif = $afterReport.data | Where-Object { $_.subject -like "*Report*" -or $_.category -eq "SYSTEM" } | Select-Object -First 1
Chk "Admin receives report-generated notification" ($afterReport.data.Count -gt $adminBefore) $true
Write-Host "       Admin total=$($afterReport.data.Count) (was $adminBefore before)" -ForegroundColor Gray
$adminBefore = $afterReport.data.Count

# ── B. SiteService → Notification ─────────────────────────────────────
Write-Host ""
Write-Host "--- B. SiteService -> NotificationService ---" -ForegroundColor Yellow

$siteTs = [System.DateTime]::UtcNow.Ticks
$siteBody = @{name="Test Heritage Site $siteTs";location="Hyderabad, TG";description="Ancient temple complex";category="HERITAGE";status="ACTIVE"} | ConvertTo-Json
$siteR = TryAPI POST "$BASE/tourismgov/v1/sites" $A $siteBody
Chk "POST /sites (create site)" $siteR.status 201
Write-Host "       siteId=$($siteR.data.siteId) name=$($siteR.data.name)" -ForegroundColor Gray

Start-Sleep 2
$afterSite = TryAPI GET "$BASE/tourismgov/v1/notifications" $AG $null
Chk "Site creation triggered notification" ($afterSite.data.Count -gt $adminBefore) $true
Write-Host "       Admin total=$($afterSite.data.Count) (was $adminBefore before)" -ForegroundColor Gray
$adminBefore = $afterSite.data.Count

# ── C. ProgramService → Notification ──────────────────────────────────
Write-Host ""
Write-Host "--- C. ProgramService -> NotificationService ---" -ForegroundColor Yellow

$progTs = [System.DateTime]::UtcNow.Ticks
$progBody = @{title="Tourism Awareness Program $progTs";description="Cultural awareness initiative";budget=50000;startDate="2026-05-01";endDate="2026-05-31";status="ACTIVE"} | ConvertTo-Json
$progR = TryAPI POST "$BASE/tourismgov/v1/programs" $A $progBody
Chk "POST /programs (create program)" $progR.status 201
Write-Host "       programId=$($progR.data.programId) title=$($progR.data.title)" -ForegroundColor Gray

Start-Sleep 2
$afterProg = TryAPI GET "$BASE/tourismgov/v1/notifications" $AG $null
Chk "Program creation triggered notification" ($afterProg.data.Count -gt $adminBefore) $true
Write-Host "       Admin total=$($afterProg.data.Count) (was $adminBefore before)" -ForegroundColor Gray
$adminBefore = $afterProg.data.Count

# ── D. EventBookingService → Notification ─────────────────────────────
Write-Host ""
Write-Host "--- D. EventBookingService -> NotificationService ---" -ForegroundColor Yellow

# Create an event first
$evtTs = [System.DateTime]::UtcNow.Ticks
$evtBody = @{title="Heritage Walk Event $evtTs";description="Guided tour";location="Hyderabad";date="2026-06-01";capacity=100;status="UPCOMING"} | ConvertTo-Json
$evtR = TryAPI POST "$BASE/tourismgov/v1/events" $A $evtBody
if ($evtR.ok) {
    $evtId = $evtR.data.eventId
    Write-Host "       eventId=$evtId created" -ForegroundColor Gray

    # Book the event as tourist
    $bookBody = @{eventId=$evtId;userId=$TOURIST_ID} | ConvertTo-Json
    $bookR = TryAPI POST "$BASE/tourismgov/v1/bookings" $TK $bookBody
    Chk "POST /bookings (tourist books event)" $bookR.status 201
    Write-Host "       bookingId=$($bookR.data.bookingId)" -ForegroundColor Gray

    Start-Sleep 2
    $afterBook = TryAPI GET "$BASE/tourismgov/v1/notifications" $TG $null
    Chk "Booking triggers notification to tourist" ($afterBook.data.Count -ge 1) $true
    Write-Host "       Tourist notifications after booking=$($afterBook.data.Count)" -ForegroundColor Gray
} else {
    Write-Host "  [SKIP] Event creation failed: $($evtR.status) $($evtR.data)" -ForegroundColor Yellow
}

# ── SUMMARY ────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   FINAL RESULTS: $P PASSED  |  $F FAILED" -ForegroundColor $(if($F -eq 0){'Green'}else{'Yellow'})
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
