package com.tourismgov.site.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourismgov.site.client.NotificationClient;
import com.tourismgov.site.client.UserClient;
import com.tourismgov.site.dto.AuditLogRequest; // ✅ ADDED IMPORT
import com.tourismgov.site.dto.HeritageSiteRequest;
import com.tourismgov.site.dto.HeritageSiteResponse;
import com.tourismgov.site.dto.NotificationRequestDTO;
import com.tourismgov.site.dto.PreservationActivityResponse;
import com.tourismgov.site.entity.HeritageSite;
import com.tourismgov.site.entity.PreservationActivity;
import com.tourismgov.site.enums.SiteStatus;
import com.tourismgov.site.exceptions.ResourceNotFoundException;
import com.tourismgov.site.repository.HeritageSiteRepository;
import com.tourismgov.site.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeritageSiteServiceImpl implements HeritageSiteService {

    private static final String RESOURCE_SITE = "HeritageSiteService";
    private static final String ENTITY_SITE = "Heritage Site";
    private static final String STATUS_SUCCESS = "SUCCESS";
    
    private static final String ACTION_SITE_CREATE = "SITE_CREATE";
    private static final String ACTION_SITE_UPDATE = "SITE_UPDATE";
    private static final String ACTION_SITE_DELETE = "SITE_DELETE";

    private final HeritageSiteRepository siteRepository;
    private final UserClient userClient; 
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public HeritageSiteResponse createSite(HeritageSiteRequest request) {
        log.info("Attempting to create heritage site: {}", request.getName());
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // ✅ NEW: Duplicate Check Logic
        if (siteRepository.existsByNameIgnoreCase(request.getName())) {
            log.warn("Creation failed: Heritage site with name '{}' already exists.", request.getName());
            
            // Log the failed attempt to the User Service
            logAuditSafe(currentUserId, ACTION_SITE_CREATE, RESOURCE_SITE, "FAILED");
            
            // Stop the process and throw an exception to the frontend
            throw new IllegalArgumentException("A Heritage Site with the name '" + request.getName() + "' already exists.");
        }
        
        HeritageSite site = new HeritageSite();
        site.setName(request.getName());
        site.setLocation(request.getLocation());
        site.setDescription(request.getDescription());
        
        // Enum Validation Logic
        site.setStatus(validateAndGetStatus(request.getStatus(), SiteStatus.OPEN));
        
        HeritageSite saved = siteRepository.save(site);
        
        // 1. Audit Log (User Service)
        logAuditSafe(currentUserId, ACTION_SITE_CREATE, RESOURCE_SITE, STATUS_SUCCESS);

        // 2. Notification (Notification Service - Global Broadcast)
        try {
            String message = String.format("New heritage site added: %s at %s.", saved.getName(), saved.getLocation());
            NotificationRequestDTO broadcastReq = NotificationRequestDTO.builder()
                    .userId(currentUserId)
                    .entityId(saved.getSiteId())
                    .subject("New Heritage Site Added!")
                    .message(message)
                    .category("SYSTEM")
                    .build();
            notificationClient.sendGlobalBroadcast(broadcastReq);
        } catch (Exception e) {
            log.error("Notification failed for site creation: {}", e.getMessage());
        }

        return mapToSiteResponse(saved);
    }

    @Override
    @Transactional
    public HeritageSiteResponse updateSite(Long siteId, HeritageSiteRequest request) {
        HeritageSite site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SITE, siteId));
        
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // ✅ NEW: Check for Name Duplication (Conflict)
        // Check if the new name already exists in the database
        var existingSiteOptional = siteRepository.findByNameIgnoreCase(request.getName());
        
        // If it exists, AND the ID doesn't match the one we are updating, it's a duplicate!
        if (existingSiteOptional.isPresent() && !existingSiteOptional.get().getSiteId().equals(siteId)) {
            log.warn("Update failed: User tried to rename Site ID {} to an existing name '{}'", siteId, request.getName());
            logAuditSafe(currentUserId, ACTION_SITE_UPDATE, RESOURCE_SITE, "FAILED_DUPLICATE_NAME");
            throw new IllegalArgumentException("A Heritage Site with the name '" + request.getName() + "' already exists.");
        }
        
        String oldStatus = site.getStatus();
        String newStatusName = validateAndGetStatus(request.getStatus(), SiteStatus.valueOf(oldStatus));
        
        // Check if any fields actually changed (Redundancy Check)
        boolean isNameSame = java.util.Objects.equals(site.getName(), request.getName());
        boolean isLocationSame = java.util.Objects.equals(site.getLocation(), request.getLocation());
        boolean isDescSame = java.util.Objects.equals(site.getDescription(), request.getDescription());
        boolean isStatusSame = oldStatus.equals(newStatusName);
        
        // If absolutely nothing changed, reject the request
        if (isNameSame && isLocationSame && isDescSame && isStatusSame) {
            log.warn("Update rejected: No changes detected for Heritage Site ID {}", siteId);
            throw new IllegalArgumentException("No changes detected. The heritage site is already up to date.");
        }

        // Apply changes
        site.setName(request.getName());
        site.setLocation(request.getLocation());
        site.setDescription(request.getDescription());
        site.setStatus(newStatusName);
        
        HeritageSite updatedSite = siteRepository.save(site);
        
        // Audit Log (User Service)
        logAuditSafe(currentUserId, ACTION_SITE_UPDATE, RESOURCE_SITE, STATUS_SUCCESS);

        // Only send notification if the status specifically changed
        if (!isStatusSame) {
            sendUpdateNotification(currentUserId, updatedSite);
        }

        return mapToSiteResponse(updatedSite);
    }
    @Override
    public List<HeritageSiteResponse> getAllSites() {
        return siteRepository.findAll().stream().map(this::mapToSiteResponse).toList(); 
    }

    @Override
    public HeritageSiteResponse getSiteById(Long siteId) {
        return siteRepository.findById(siteId)
                .map(this::mapToSiteResponse)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SITE, siteId));
    }

    @Override
    @Transactional
    public void deleteSite(Long siteId) {
        log.info("Soft deleting (closing permanently) Heritage Site ID: {}", siteId);
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 1. Fetch the site
        HeritageSite site = siteRepository.findById(siteId)
                .orElseThrow(() -> {
                    logAuditSafe(currentUserId, ACTION_SITE_DELETE, RESOURCE_SITE, "FAILED_NOT_FOUND");
                    return new ResourceNotFoundException(ENTITY_SITE, siteId);
                });
        
        // Optional: If it's already permanently closed, do nothing
        if (SiteStatus.PERMANENTLY_CLOSED.name().equals(site.getStatus())) {
            log.warn("Site ID {} is already permanently closed.", siteId);
            return; 
        }

        // 2. Perform the Soft Delete
        site.setStatus(SiteStatus.PERMANENTLY_CLOSED.name());
        siteRepository.save(site);
        
        // 3. Audit Log (User Service)
        logAuditSafe(currentUserId, ACTION_SITE_DELETE, RESOURCE_SITE, STATUS_SUCCESS);
        
        // 4. (Optional) Send Notification
        try {
            String message = String.format("WARNING: %s has been permanently closed.", site.getName());
            notificationClient.sendSystemAlert(currentUserId, siteId, "Site Permanently Closed", message, "SYSTEM");
        } catch (Exception e) {
            log.warn("Failed to send deletion notification: {}", e.getMessage());
        }
    }

    // --- Helper Methods ---

    private String validateAndGetStatus(String statusInput, SiteStatus defaultStatus) {
        if (statusInput == null || statusInput.isBlank()) {
            return defaultStatus.name();
        }
        try {
            return SiteStatus.valueOf(statusInput.toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Site Status. Allowed: OPEN, CLOSED_FOR_MAINTENANCE, RESTORATION_IN_PROGRESS, PERMANENTLY_CLOSED");
        }
    }

    private void sendUpdateNotification(Long userId, HeritageSite site) {
        try {
            String message = String.format("Notice: %s is now marked as %s.", site.getName(), site.getStatus());
            notificationClient.sendSystemAlert(userId, site.getSiteId(), "Site Status Update", message, "SYSTEM");
        } catch (Exception e) {
            log.warn("Failed to send status update notification: {}", e.getMessage());
        }
    }

    private HeritageSiteResponse mapToSiteResponse(HeritageSite site) {
        HeritageSiteResponse res = new HeritageSiteResponse();
        res.setSiteId(site.getSiteId());
        res.setName(site.getName());
        res.setLocation(site.getLocation());
        res.setDescription(site.getDescription());
        res.setStatus(site.getStatus());
        
        if (site.getPreservationActivities() != null) {
            res.setPreservationActivities(site.getPreservationActivities().stream()
                    .map(this::mapToActivityResponse) 
                    .toList());
        }
        return res;
    }
    
    private PreservationActivityResponse mapToActivityResponse(PreservationActivity activity) {
        PreservationActivityResponse response = new PreservationActivityResponse();
        response.setActivityId(activity.getActivityId());
        response.setDescription(activity.getDescription());
        response.setDate(activity.getDate()); 
        response.setStatus(activity.getStatus());
        if (activity.getSite() != null) response.setSiteId(activity.getSite().getSiteId());
        response.setOfficerId(activity.getOfficerId()); 
        return response;
    }

    // ✅ NEW: Private Fault-Tolerant Audit Log Method for the Feign Client
    private void logAuditSafe(Long userId, String action, String resource, String status) {
        try {
            AuditLogRequest auditRequest = new AuditLogRequest();
            auditRequest.setUserId(userId);
            auditRequest.setAction(action);
            auditRequest.setResource(resource);
            auditRequest.setStatus(status);
            
            userClient.logAction(auditRequest);
        } catch (Exception e) {
            log.error("Failed to push audit log to USER-SERVICE: {}", e.getMessage());
        }
    }
}