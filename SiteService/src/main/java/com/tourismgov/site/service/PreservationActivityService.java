package com.tourismgov.site.service;

import java.util.List;

import com.tourismgov.site.dto.PreservationActivityRequest;
import com.tourismgov.site.dto.PreservationActivityResponse;

public interface PreservationActivityService {

    // Logs a new preservation activity for a specific site
    PreservationActivityResponse logActivity(Long siteId, PreservationActivityRequest request);
    
    // Retrieves details of a specific activity
    PreservationActivityResponse getActivityById(Long activityId);
    
    // Retrieves all activities taking place at a specific site
    List<PreservationActivityResponse> getActivitiesBySite(Long siteId);

    // Retrieves all activities assigned to a specific officer
    List<PreservationActivityResponse> getActivitiesByOfficer(Long officerId);
    
    // Updates the workflow status (e.g., PENDING_APPROVAL, IN_PROGRESS, COMPLETED)
    PreservationActivityResponse updateActivityStatus(Long activityId, String status);
    
    // Deletes or archives a preservation record
    void deleteActivity(Long activityId);
}