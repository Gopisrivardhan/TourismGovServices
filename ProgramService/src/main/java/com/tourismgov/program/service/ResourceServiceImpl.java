package com.tourismgov.program.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourismgov.program.client.UserClient;
import com.tourismgov.program.dto.AuditLogRequest;
import com.tourismgov.program.dto.ResourceRequest;
import com.tourismgov.program.dto.ResourceResponse;
import com.tourismgov.program.entity.Resource;
import com.tourismgov.program.entity.TourismProgram;
import com.tourismgov.program.enums.ProgramStatus;
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
    private static final String STATUS_FAILED = "FAILED";
    private static final String MODULE_NAME = "ResourceModule";

    private final ResourceRepository resourceRepository;
    private final TourismProgramRepository programRepository;
    
    // MICROSERVICE FIX: Replaced AuditLogService with Feign UserClient
    private final UserClient userClient;

    @Override
    @Transactional
    public ResourceResponse allocateResourceToProgram(Long programId, ResourceRequest request) {
        log.info("Allocating {} resource to Program ID: {}", request.getType(), programId);
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 1. Fetch the Program
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> {
                    logAuditSafe(currentUserId, "ALLOCATE_RESOURCE", MODULE_NAME, STATUS_FAILED);
                    return new ResourceNotFoundException(ENTITY_PROGRAM, programId);
                });

        // ✅ NEW: Business Logic Validation - Prevent allocation to Cancelled or Completed programs
        if (program.getStatus() == ProgramStatus.CANCELLED || program.getStatus() == ProgramStatus.COMPLETED) {
            log.warn("Allocation rejected: Program ID {} is {}", programId, program.getStatus());
            logAuditSafe(currentUserId, "ALLOCATE_RESOURCE", MODULE_NAME, STATUS_FAILED);
            throw new IllegalStateException("Cannot allocate resources to a program that is " + program.getStatus());
        }

        // 2. Allocate the Resource
        Resource resource = new Resource();
        resource.setProgram(program);
        resource.setQuantity(request.getQuantity());
        resource.setType(request.getType()); 
        resource.setStatus(ResourceStatus.ALLOCATED);

        Resource saved = resourceRepository.save(resource);
        
        // 3. Cross-Service Audit Log call
        logAuditSafe(currentUserId, "ALLOCATE_RESOURCE", MODULE_NAME, STATUS_SUCCESS);
        
        return mapToResourceResponse(saved);
    }

    @Override
    @Transactional
    public ResourceResponse updateResourceStatus(Long resourceId, ResourceStatus newStatus) {
        log.info("Updating Resource ID: {} status to {}", resourceId, newStatus);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> {
                    logAuditSafe(currentUserId, "UPDATE_RESOURCE_STATUS", MODULE_NAME, STATUS_FAILED);
                    return new ResourceNotFoundException(ENTITY_RESOURCE, resourceId);
                });
        
        // Direct Enum assignment
        resource.setStatus(newStatus);
        Resource updated = resourceRepository.save(resource);

        logAuditSafe(currentUserId, "UPDATE_RESOURCE_STATUS", MODULE_NAME, STATUS_SUCCESS);
        
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
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        if (!resourceRepository.existsById(resourceId)) {
            logAuditSafe(currentUserId, "DELETE_RESOURCE", MODULE_NAME, STATUS_FAILED);
            throw new ResourceNotFoundException(ENTITY_RESOURCE, resourceId);
        }
        
        resourceRepository.deleteById(resourceId);
        
        logAuditSafe(currentUserId, "DELETE_RESOURCE", MODULE_NAME, STATUS_SUCCESS);
    }

    // --- Private Fault-Tolerant Audit Log Method ---
    private void logAuditSafe(Long userId, String action, String resource, String status) {
        try {
            // Instantiate the DTO and set the 4 required fields safely
            AuditLogRequest auditRequest = new AuditLogRequest();
            auditRequest.setUserId(userId);
            auditRequest.setAction(action);
            auditRequest.setResource(resource);
            auditRequest.setStatus(status);
            
            // Push to the USER-SERVICE
            userClient.logAction(auditRequest);
            
        } catch (Exception e) {
            log.error("Failed to push audit log to USER-SERVICE: {}", e.getMessage());
        }
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