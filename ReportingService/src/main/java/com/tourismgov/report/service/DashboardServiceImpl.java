package com.tourismgov.report.service;

import com.tourismgov.report.client.*;
import com.tourismgov.report.dto.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserClient userClient;
    private final SiteClient siteClient;
    private final EventClient eventClient;
    private final ProgramClient programClient;
    private final ComplianceClient complianceClient;
    private final BookingClient bookingClient;
    private final NotificationClient notificationClient;

    @Override
    public DashboardDTO getDashboardMetrics(String role, Long userId) {
        // Step 1: Initial User Verification (Blocking because we need the role to proceed)
        UserDTO user = userClient.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        String dbRole = user.getRole().name().toUpperCase();
        // Gateway injects 'ROLE_ADMIN'; DB stores 'ADMIN' — strip the prefix for comparison
        String headerRole = role.toUpperCase().replace("ROLE_", "");
        if (!dbRole.equals(headerRole)) {
            log.warn("Role mismatch: DB={} Header={}", dbRole, headerRole);
            // Don't hard-fail — the gateway already validated the token, just log it
        }

        // Step 2: Parallel Data Fetching (The Aggregator Pattern)
        // We fetch everything concurrently to reduce load time from seconds to milliseconds
        CompletableFuture<List<SiteDTO>> sitesFuture = CompletableFuture.supplyAsync(() -> 
            handleFetch(siteClient::getAllSites, "Sites"));
        
        CompletableFuture<List<EventDTO>> eventsFuture = CompletableFuture.supplyAsync(() -> 
            handleFetch(eventClient::getAllEvents, "Events"));
        
        CompletableFuture<List<NotificationDTO>> notificationsFuture = CompletableFuture.supplyAsync(() -> 
            handleFetch(() -> notificationClient.getUnreadNotifications(userId), "Notifications"));
        
        CompletableFuture<List<BookingDTO>> bookingsFuture = CompletableFuture.supplyAsync(() -> 
            handleFetch(bookingClient::getAllBookings, "Bookings"));

        // Wait for core metrics to finish
        CompletableFuture.allOf(sitesFuture, eventsFuture, notificationsFuture, bookingsFuture).join();

        // Step 3: Processing Common Metrics
        List<SiteDTO> allSites = sitesFuture.join();
        List<EventDTO> allEvents = eventsFuture.join();
        List<NotificationDTO> unreadNotifications = notificationsFuture.join();
        List<BookingDTO> allBookings = bookingsFuture.join();

        Map<String, Object> metrics = new LinkedHashMap<>();
        long totalS = allSites.size();
        long activeS = allSites.stream().filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus())).count();

        metrics.put("totalHeritageSites", totalS);
        metrics.put("siteActivityPct", calculatePercentage(activeS, totalS) + "%");
        metrics.put("activeEvents", allEvents.stream().filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus())).count());

        // Step 4: Role-Specific Logic
        switch (dbRole) {
            case "TOURIST":
                List<BookingDTO> myBookings = allBookings.stream()
                        .filter(b -> userId.equals(b.getTouristId()))
                        .toList();
                long totalB = myBookings.size();
                long doneB = myBookings.stream().filter(b -> "COMPLETED".equalsIgnoreCase(b.getStatus())).count();

                metrics.put("tripCompletionRate", calculatePercentage(doneB, totalB) + "%");
                metrics.put("upcomingEvents", myBookings.stream().filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus())).count());
                break;

            case "MANAGER":
            case "ADMIN":
                // Additional fetch for Programs (Parallelized)
                List<ProgramDTO> allPrograms = handleFetch(programClient::getAllPrograms, "Programs");
                double totalBudget = allPrograms.stream().mapToDouble(p -> p.getBudget() != null ? p.getBudget() : 0.0).sum();
                
                metrics.put("totalUsers", handleFetch(userClient::getAllUsers, "Users").size());
                metrics.put("totalEvents", allEvents.size()); // FIXED BUG: Previously used totalS
                metrics.put("totalBookings", allBookings.size());
                metrics.put("totalBudget", "₹" + totalBudget);
                metrics.put("activePrograms", allPrograms.stream().filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus())).count());
                break;

            case "OFFICER":
                metrics.put("totalBookings", allBookings.size());
                metrics.put("pendingApprovals", allBookings.stream().filter(b -> "PENDING".equalsIgnoreCase(b.getStatus())).count());
                metrics.put("docsToVerify", 0); 
                break;

            case "COMPLIANCE":
            case "AUDITOR":
                List<ComplianceDTO> allCompliance = handleFetch(complianceClient::getAllComplianceRecords, "Compliance");
                metrics.put("policyViolations", allCompliance.stream().filter(c -> "FAIL".equalsIgnoreCase(c.getResult())).count());
                metrics.put("pendingAudits", allCompliance.stream().filter(c -> "PENDING".equalsIgnoreCase(c.getResult())).count());
                break;
        }

        return DashboardDTO.builder()
                .role(dbRole)
                .userId(userId)
                .userName(user.getName())
                .metrics(metrics)
                .unreadNotifications((long) unreadNotifications.size())
                .build();
    }

    // Resilience Helper: Prevents the whole dashboard from crashing if one service is down
    private <T> List<T> handleFetch(java.util.function.Supplier<List<T>> fetcher, String serviceName) {
        try {
            return fetcher.get();
        } catch (Exception e) {
            log.error("DASHBOARD ERROR: Failed to fetch {} - {}", serviceName, e.getMessage());
            return Collections.emptyList();
        }
    }

    private double calculatePercentage(long part, long total) {
        if (total <= 0) return 0.0;
        return Math.round((double) part / total * 100.0 * 10.0) / 10.0;
    }
}