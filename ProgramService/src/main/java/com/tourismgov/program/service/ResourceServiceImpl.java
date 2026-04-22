package com.tourismgov.program.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourismgov.program.client.UserClient;
import com.tourismgov.program.dto.ResourceRequest;
import com.tourismgov.program.dto.ResourceResponse;
import com.tourismgov.program.entity.Resource;
import com.tourismgov.program.entity.TourismProgram;
import com.tourismgov.program.enums.ResourceStatus;
import com.tourismgov.program.exceptions.ResourceNotFoundException;
import com.tourismgov.program.repository.ResourceRepository;
import com.tourismgov.program.repository.TourismProgramRepository;
import com.tourismgov.program.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResourceServiceImpl implements ResourceService {

    private static final String ENTITY_RESOURCE = "Program Resource";
    private static final String ENTITY_PROGRAM = "Tourism Program";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String MODULE_NAME = "ResourceModule";

    private final ResourceRepository resourceRepository;
    private final TourismProgramRepository programRepository;
    
    // MICROSERVICE FIX: Replaced AuditLogService with Feign UserClient
    private final UserClient userClient;

    @Override
    @Transactional
    public ResourceResponse allocateResourceToProgram(Long programId, ResourceRequest request) {
        log.info("Allocating {} resource to Program ID: {}", request.getType(), programId);

        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_PROGRAM, programId));

        Resource resource = new Resource();
        resource.setProgram(program);
        resource.setQuantity(request.getQuantity());
        
        // Professional Enum Usage: Direct assignment, no String conversions
        resource.setType(request.getType()); 
        resource.setStatus(ResourceStatus.ALLOCATED);

        Resource saved = resourceRepository.save(resource);
        
        // Cross-Service Audit Log call
        userClient.logAction(
            SecurityUtils.getCurrentUserId(), 
            "ALLOCATE_RESOURCE", 
            MODULE_NAME, 
            STATUS_SUCCESS
        );
        
        return mapToResourceResponse(saved);
    }

    @Override
    @Transactional
    public ResourceResponse updateResourceStatus(Long resourceId, ResourceStatus newStatus) {
        log.info("Updating Resource ID: {} status to {}", resourceId, newStatus);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_RESOURCE, resourceId));
        
        // Direct Enum assignment
        resource.setStatus(newStatus);
        Resource updated = resourceRepository.save(resource);

        userClient.logAction(
            SecurityUtils.getCurrentUserId(), 
            "UPDATE_RESOURCE_STATUS", 
            MODULE_NAME, 
            STATUS_SUCCESS
        );
        
        return mapToResourceResponse(updated);
    }

    @Override
    public List<ResourceResponse> getResourcesByProgram(Long programId) {
        if (!programRepository.existsById(programId)) {
            throw new ResourceNotFoundException(ENTITY_PROGRAM, programId);
        }
        return resourceRepository.findByProgram_ProgramId(programId).stream()
                .map(this::mapToResourceResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteResource(Long resourceId) {
        if (!resourceRepository.existsById(resourceId)) {
            throw new ResourceNotFoundException(ENTITY_RESOURCE, resourceId);
        }
        resourceRepository.deleteById(resourceId);
        
        userClient.logAction(
            SecurityUtils.getCurrentUserId(), 
            "DELETE_RESOURCE", 
            MODULE_NAME, 
            STATUS_SUCCESS
        );
    }

    private ResourceResponse mapToResourceResponse(Resource resource) {
        ResourceResponse res = new ResourceResponse();
        res.setResourceId(resource.getResourceId());
        res.setProgramId(resource.getProgram().getProgramId());
        
        // Directly mapping Enums to the DTO (Spring/Jackson will auto-convert to JSON Strings)
        res.setType(resource.getType());
        res.setQuantity(resource.getQuantity());
        res.setStatus(resource.getStatus());
        
        return res;
    }
}