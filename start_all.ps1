$services = @(
    @{name="EurekaServer"; port=8761},
    @{name="ConfigServer"; port=8181},
    @{name="UserService"; port=1111},
    @{name="TouristService"; port=2020},
    @{name="SiteService"; port=3030},
    @{name="EventBookingService"; port=4040},
    @{name="ProgramService"; port=5050},
    @{name="ComplianceService"; port=6060},
    @{name="NotificationService"; port=7070},
    @{name="ReportingService"; port=9090},
    @{name="GatewayAPI"; port=8383}
)

foreach ($svc in $services) {
    Write-Host "Starting $($svc.name)..."
    $svcPath = "d:\DEVELOP LIFE\myWorking\$($svc.name)"
    
    # Start the process in the background
    # Using start-process with minimized window style to keep it tidy
    Start-Process -FilePath cmd.exe -ArgumentList "/c mvnw.cmd spring-boot:run" -WorkingDirectory $svcPath -WindowStyle Minimized
    
    # Wait until port is open
    $portOpened = $false
    Write-Host "Waiting for $($svc.name) to start on port $($svc.port)..."
    for ($i = 0; $i -lt 90; $i++) {
        $tcp = New-Object System.Net.Sockets.TcpClient
        try {
            $tcp.Connect("localhost", $svc.port)
            $portOpened = $true
            $tcp.Close()
            break
        } catch {
            Start-Sleep -Seconds 2
        }
    }
    
    if ($portOpened) {
        Write-Host "$($svc.name) is UP on port $($svc.port)."
    } else {
        Write-Host "WARNING: $($svc.name) taking too long to start on $($svc.port). Moving to next..."
    }
}

Write-Host "`n--- ALL SERVICES ATTEMPTED TO START ---"
Write-Host "Check Eureka at http://localhost:8761 to verify registration."
