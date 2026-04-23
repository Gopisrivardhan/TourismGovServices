package com.tourismgov.program.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourismgov.program.client.NotificationClient;
import com.tourismgov.program.client.UserClient;
import com.tourismgov.program.dto.AuditLogRequest;
import com.tourismgov.program.dto.ProgramRequest;
import com.tourismgov.program.dto.ProgramResponse;
import com.tourismgov.program.dto.ResourceResponse;
import com.tourismgov.program.entity.Resource;
import com.tourismgov.program.entity.TourismProgram;
import com.tourismgov.program.enums.ProgramStatus;
import com.tourismgov.program.enums.ResourceStatus;
import com.tourismgov.program.enums.ResourceType;
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
public class TourismProgramServiceImpl implements TourismProgramService {

    private static final String ENTITY_NAME = "Tourism Program";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String MODULE_NAME = "ProgramModule";

    private final TourismProgramRepository programRepository;
    private final ResourceRepository resourceRepository;

    private final UserClient userClient; 
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public ProgramResponse createProgram(ProgramRequest request) {
        log.info("Creating Tourism Program: {}", request.getTitle());
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        try {
            validateProgramDates(request.getStartDate(), request.getEndDate(), true);
        } catch (IllegalArgumentException e) {
            logAuditSafe(currentUserId, "CREATE_PROGRAM", MODULE_NAME, STATUS_FAILED);
            throw e;
        }

        TourismProgram program = new TourismProgram();
        mapRequestToEntity(request, program);
        program.setStatus(ProgramStatus.PLANNED);

        TourismProgram saved = programRepository.save(program);
        
        logAuditSafe(currentUserId, "CREATE_PROGRAM", MODULE_NAME, STATUS_SUCCESS);

        try {
            String message = String.format("Tourism program '%s' has been officially initiated.", saved.getTitle());
            notificationClient.sendSystemAlert(
                    currentUserId, 
                    saved.getProgramId(), 
                    "New Program Launched!", 
                    message, 
                    "SYSTEM"
            );
        } catch (Exception e) {
            log.error("Failed to send creation notification: {}", e.getMessage());
        }

        return mapToProgramResponse(saved);
    }

    @Override
    @Transactional
    public ProgramResponse updateProgramStatus(Long id, String statusString) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        TourismProgram p = programRepository.findById(id)
                .orElseThrow(() -> {
                    logAuditSafe(currentUserId, "UPDATE_STATUS", MODULE_NAME, STATUS_FAILED);
                    return new ResourceNotFoundException(ENTITY_NAME, id);
                });
        
        try {
            ProgramStatus status = ProgramStatus.valueOf(statusString.toUpperCase());
            p.setStatus(status);
        } catch (IllegalArgumentException e) {
            logAuditSafe(currentUserId, "UPDATE_STATUS", MODULE_NAME, STATUS_FAILED);
            throw new IllegalArgumentException("Invalid status provided. Allowed: PLANNED, ACTIVE, COMPLETED, CANCELLED");
        }

        TourismProgram updated = programRepository.save(p);
        logAuditSafe(currentUserId, "UPDATE_STATUS", MODULE_NAME, STATUS_SUCCESS);
        
        return mapToProgramResponse(updated);
    }

    @Override
    public Map<String, Object> getBudgetReport(Long programId) {
        log.info("Generating budget report for Program ID: {}", programId);
        
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, programId));

        List<Resource> resources = resourceRepository.findByProgram_ProgramId(programId);
        double totalBudget = (program.getBudget() != null) ? program.getBudget() : 0.0;
        
        double spentFunds = resources.stream()
                .filter(r -> r.getType() == ResourceType.FUNDS && r.getStatus() == ResourceStatus.ALLOCATED)
                .mapToDouble(r -> (r.getQuantity() != null) ? r.getQuantity() : 0.0)
                .sum();

        Map<String, Object> report = new HashMap<>();
        report.put("programTitle", program.getTitle());
        report.put("totalBudget", totalBudget);
        report.put("amountSpent", spentFunds);
        report.put("remainingBudget", totalBudget - spentFunds);
        report.put("currentStatus", program.getStatus()); 
        
        return report;
    }

    @Override 
    @Transactional
    public ProgramResponse updateProgram(Long programId, ProgramRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> {
                    logAuditSafe(currentUserId, "UPDATE_PROGRAM", MODULE_NAME, STATUS_FAILED);
                    return new ResourceNotFoundException(ENTITY_NAME, programId);
                });

        try {
            validateProgramDates(request.getStartDate(), request.getEndDate(), false);
        } catch (IllegalArgumentException e) {
            logAuditSafe(currentUserId, "UPDATE_PROGRAM", MODULE_NAME, STATUS_FAILED);
            throw e;
        }

        mapRequestToEntity(request, program);
        
        TourismProgram updated = programRepository.save(program);
        logAuditSafe(currentUserId, "UPDATE_PROGRAM", MODULE_NAME, STATUS_SUCCESS);
        return mapToProgramResponse(updated);
    }

    @Override 
    @Transactional
    public void deleteProgram(Long programId) {
        log.info("Cancelling Program ID: {} and its associated resources", programId);
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 1. Fetch the Program
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> {
                    logAuditSafe(currentUserId, "DELETE_PROGRAM", MODULE_NAME, STATUS_FAILED);
                    return new ResourceNotFoundException(ENTITY_NAME, programId);
                });
        
        // 2. Fetch all Resources linked to this Program
        List<Resource> resources = resourceRepository.findByProgram_ProgramId(programId);

        // 3. Update Resource Statuses (Skip if already RELEASED)
        boolean resourcesUpdated = false;
        for (Resource resource : resources) {
            if (resource.getStatus() != ResourceStatus.RELEASED) {
                resource.setStatus(ResourceStatus.CANCELLED);
                resourcesUpdated = true;
            }
        }
        
        // 4. Save resources if any were changed
        if (resourcesUpdated) {
            resourceRepository.saveAll(resources);
            log.info("Successfully cancelled eligible resources for Program ID: {}", programId);
        }

        // 5. Soft-delete the Program
        program.setStatus(ProgramStatus.CANCELLED);
        programRepository.save(program);
        logAuditSafe(currentUserId, "DELETE_PROGRAM", MODULE_NAME, STATUS_SUCCESS);
    }

    @Override 
    public ProgramResponse getProgramById(Long id) {
        return programRepository.findById(id).map(this::mapToProgramResponse)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, id));
    }

    @Override 
    public List<ProgramResponse> getAllPrograms() {
        return programRepository.findAll().stream().map(this::mapToProgramResponse).toList();
    }

    @Override 
    public Page<ProgramResponse> getProgramsPaged(int page, int size) {
        return programRepository.findAll(PageRequest.of(page, size)).map(this::mapToProgramResponse);
    }

    // --- PRIVATE VALIDATION & MAPPING ---

    private void validateProgramDates(LocalDate start, LocalDate end, boolean isNew) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end dates are strictly required.");
        }
        LocalDate today = LocalDate.now();
        if (isNew && start.isBefore(today)) {
            throw new IllegalArgumentException("Start date cannot be in the past.");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End date must be strictly after the start date.");
        }
    }

    private void mapRequestToEntity(ProgramRequest request, TourismProgram entity) {
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setBudget(request.getBudget());
    }

    // ✅ UPDATED: Now fetches resources and attaches them to the response
    private ProgramResponse mapToProgramResponse(TourismProgram program) {
        ProgramResponse res = new ProgramResponse();
        res.setProgramId(program.getProgramId());
        res.setTitle(program.getTitle());
        res.setDescription(program.getDescription());
        res.setStartDate(program.getStartDate());
        res.setEndDate(program.getEndDate());
        res.setBudget(program.getBudget());
        res.setStatus(program.getStatus() != null ? program.getStatus().name() : null);
        
        // Fetch resources for this program and map them
        List<Resource> resources = resourceRepository.findByProgram_ProgramId(program.getProgramId());
        List<ResourceResponse> resourceResponses = resources.stream()
                .map(this::mapToResourceResponse)
                .toList();
        
        // Attach resources to the response
        res.setResources(resourceResponses);
        
        return res;
    }

    // ✅ NEW: Helper method to map individual Resources
    private ResourceResponse mapToResourceResponse(Resource resource) {
        ResourceResponse res = new ResourceResponse();
        res.setResourceId(resource.getResourceId());
        if (resource.getProgram() != null) {
            res.setProgramId(resource.getProgram().getProgramId());
        }
        res.setType(resource.getType());
        res.setQuantity(resource.getQuantity());
        res.setStatus(resource.getStatus());
        return res;
    }

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