package com.tourismgov.site.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourismgov.site.client.UserClient;
import com.tourismgov.site.dto.AuditLogRequest;
import com.tourismgov.site.dto.PreservationActivityRequest;
import com.tourismgov.site.dto.PreservationActivityResponse;
import com.tourismgov.site.entity.HeritageSite;
import com.tourismgov.site.entity.PreservationActivity;
import com.tourismgov.site.enums.PreservationStatus;
import com.tourismgov.site.enums.SiteStatus; // ✅ ADDED IMPORT
import com.tourismgov.site.exceptions.ResourceNotFoundException;
import com.tourismgov.site.repository.HeritageSiteRepository;
import com.tourismgov.site.repository.PreservationActivityRepository;
import com.tourismgov.site.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreservationActivityServiceImpl implements PreservationActivityService {

    // --- Professional Constants for Zero Warnings ---
    private static final String ENTITY_ACTIVITY = "Preservation Activity";
    private static final String ENTITY_SITE = "Heritage Site";
    
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String MODULE_NAME = "PreservationModule";
    
    private static final String ACTION_LOG = "LOG_PRESERVATION";
    private static final String ACTION_UPDATE = "STATUS_UPDATE";
    private static final String ACTION_DELETE = "DELETE_ACTIVITY";

    // --- Dependencies ---
    private final PreservationActivityRepository activityRepository;
    private final HeritageSiteRepository siteRepository;
    private final UserClient userClient;

    @Override
    @Transactional
    public PreservationActivityResponse logActivity(Long siteId, PreservationActivityRequest request) {
        log.info("Logging preservation activity for Site ID: {}", siteId);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        HeritageSite site = siteRepository.findById(siteId)
                .orElseThrow(() -> {
                    logAuditSafe(currentUserId, ACTION_LOG, MODULE_NAME, "FAILED_NOT_FOUND");
                    return new ResourceNotFoundException(ENTITY_SITE, siteId);
                });

        // ✅ NEW: Prevent logging activities for permanently closed sites
        if (SiteStatus.PERMANENTLY_CLOSED.name().equals(site.getStatus())) {
            log.warn("Activity logging rejected: Site ID {} is permanently closed.", siteId);
            
            // Log the failed attempt so administrators can see someone tried to do this
            logAuditSafe(currentUserId, ACTION_LOG, MODULE_NAME, "FAILED_SITE_CLOSED");
            
            // Throw a 400 Bad Request to the frontend
            throw new IllegalStateException("Cannot log preservation activities for a permanently closed heritage site.");
        }

        PreservationActivity activity = new PreservationActivity();
        activity.setSite(site);
        
        // MICROSERVICE FIX: Store flat ID instead of User entity
        activity.setOfficerId(currentUserId); 
        activity.setDescription(request.getDescription());
        
        // FIX 1: Direct Assignment (Both are LocalDateTime)
        if (request.getDate() != null) {
            activity.setDate(request.getDate()); 
        }

        // Enum Status Validation
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            validateAndSetStatus(activity, request.getStatus());
        } else {
            activity.setStatus(PreservationStatus.IN_PROGRESS.name());
        }

        PreservationActivity saved = activityRepository.save(activity);
        
        // Automatic Site Closure Logic
        // If the officer flags that the site needs closure AND the activity is in progress
        if (request.isRequiresSiteClosure() && PreservationStatus.IN_PROGRESS.name().equals(saved.getStatus())) {
            site.setStatus(SiteStatus.CLOSED_FOR_MAINTENANCE.name());
            siteRepository.save(site);
            log.info("Automatically updated Heritage Site ID {} to CLOSED_FOR_MAINTENANCE", siteId);
        }
        
        // Cross-service audit logging using the helper method
        logAuditSafe(currentUserId, ACTION_LOG, MODULE_NAME, STATUS_SUCCESS);

        return mapToActivityResponse(saved);
    }
    @Override
    @Transactional
    public PreservationActivityResponse updateActivityStatus(Long activityId, String status) {
        log.info("Updating status for activity ID: {} to {}", activityId, status);
        
        PreservationActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_ACTIVITY, activityId));

        validateAndSetStatus(activity, status);
        
        PreservationActivity updated = activityRepository.save(activity);
        
        // Cross-service audit logging using the helper method
        logAuditSafe(SecurityUtils.getCurrentUserId(), ACTION_UPDATE, MODULE_NAME, STATUS_SUCCESS);
        
        return mapToActivityResponse(updated);
    }

    @Override
    public PreservationActivityResponse getActivityById(Long activityId) {
        return activityRepository.findById(activityId)
                .map(this::mapToActivityResponse)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_ACTIVITY, activityId));
    }

    @Override
    public List<PreservationActivityResponse> getActivitiesBySite(Long siteId) {
        if (!siteRepository.existsById(siteId)) {
            throw new ResourceNotFoundException(ENTITY_SITE, siteId);
        }
        return activityRepository.findBySite_SiteId(siteId).stream()
                .map(this::mapToActivityResponse)
                .toList();
    }

    @Override
    public List<PreservationActivityResponse> getActivitiesByOfficer(Long officerId) {
        // In a microservice, we just query the local table for the flat officerId.
        // No need to query UserRepository here!
        return activityRepository.findByOfficerId(officerId).stream()
                .map(this::mapToActivityResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteActivity(Long activityId) {
        log.info("Soft deleting (cancelling) Preservation Activity ID: {}", activityId);
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 1. Fetch the activity
        PreservationActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> {
                    logAuditSafe(currentUserId, ACTION_DELETE, MODULE_NAME, "FAILED_NOT_FOUND");
                    return new ResourceNotFoundException(ENTITY_ACTIVITY, activityId);
                });

        // ✅ NEW: Idempotency Check - If it's already cancelled, do nothing and exit early!
        if (PreservationStatus.CANCELLED.name().equals(activity.getStatus())) {
            log.warn("Preservation Activity ID {} is already cancelled. No action taken.", activityId);
            return; 
        }

        // 2. Prevent deleting an already completed activity
        if (PreservationStatus.COMPLETED.name().equals(activity.getStatus())) {
            log.warn("Cannot cancel a completed preservation activity. ID: {}", activityId);
            logAuditSafe(currentUserId, ACTION_DELETE, MODULE_NAME, "FAILED_ALREADY_COMPLETED");
            throw new IllegalStateException("Completed preservation activities cannot be deleted.");
        }

        // 3. Perform the Soft Delete (Change status to CANCELLED)
        activity.setStatus(PreservationStatus.CANCELLED.name());
        activityRepository.save(activity);
        
        // 4. Audit Log (User Service)
        logAuditSafe(currentUserId, ACTION_DELETE, MODULE_NAME, STATUS_SUCCESS);
    }

    /* --- PRIVATE UTILITY METHODS --- */

    private void validateAndSetStatus(PreservationActivity activity, String statusStr) {
        try {
            PreservationStatus status = PreservationStatus.valueOf(statusStr.toUpperCase());
            activity.setStatus(status.name());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Preservation Status. Use: IN_PROGRESS, COMPLETED, etc.");
        }
    }

    private PreservationActivityResponse mapToActivityResponse(PreservationActivity activity) {
        PreservationActivityResponse res = new PreservationActivityResponse();
        res.setActivityId(activity.getActivityId());
        res.setDescription(activity.getDescription());
        
        // FIX 2: Direct Assignment (Both are LocalDateTime)
        if (activity.getDate() != null) {
            res.setDate(activity.getDate()); 
        }
        
        res.setStatus(activity.getStatus());
        
        if (activity.getSite() != null) {
            res.setSiteId(activity.getSite().getSiteId());
        }
        
        // MICROSERVICE FIX: Map the flat ID
        res.setOfficerId(activity.getOfficerId()); 
        
        return res;
    }

    // Private Fault-Tolerant Audit Log Method for the Feign Client
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