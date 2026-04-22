package com.tourismgov.program.service;

import java.util.List;
import com.tourismgov.program.dto.ResourceRequest;
import com.tourismgov.program.dto.ResourceResponse;
import com.tourismgov.program.enums.ResourceStatus;

public interface ResourceService {
    ResourceResponse allocateResourceToProgram(Long programId, ResourceRequest request);
    ResourceResponse updateResourceStatus(Long resourceId, ResourceStatus newStatus);
    List<ResourceResponse> getResourcesByProgram(Long programId);
    void deleteResource(Long resourceId);
}