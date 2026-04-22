package com.tourismgov.site.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourismgov.site.client.UserClient;
import com.tourismgov.site.dto.PreservationActivityRequest;
import com.tourismgov.site.dto.PreservationActivityResponse;
import com.tourismgov.site.entity.HeritageSite;
import com.tourismgov.site.entity.PreservationActivity;
import com.tourismgov.site.enums.PreservationStatus;
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
    
    // MICROSERVICE FIX: OpenFeign Client replaces UserRepository and AuditLogService
    private final UserClient userClient;

    @Override
    @Transactional
    public PreservationActivityResponse logActivity(Long siteId, PreservationActivityRequest request) {
        log.info("Logging preservation activity for Site ID: {}", siteId);
        
        HeritageSite site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SITE, siteId));

        Long currentUserId = SecurityUtils.getCurrentUserId();

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
        
        // Cross-service audit logging
        userClient.logAction(currentUserId, ACTION_LOG, MODULE_NAME, STATUS_SUCCESS);

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
        
        userClient.logAction(SecurityUtils.getCurrentUserId(), ACTION_UPDATE, MODULE_NAME, STATUS_SUCCESS);
        
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
        if (!activityRepository.existsById(activityId)) {
            throw new ResourceNotFoundException(ENTITY_ACTIVITY, activityId);
        }
        activityRepository.deleteById(activityId);
        userClient.logAction(SecurityUtils.getCurrentUserId(), ACTION_DELETE, MODULE_NAME, STATUS_SUCCESS);
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
        res.setCreatedAt(activity.getCreatedAt());
        
        if (activity.getSite() != null) {
            res.setSiteId(activity.getSite().getSiteId());
        }
        
        // MICROSERVICE FIX: Map the flat ID
        res.setOfficerId(activity.getOfficerId()); 
        
        return res;
    }
}