package com.tourismgov.program.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourismgov.program.client.EventClient;
import com.tourismgov.program.client.NotificationClient;
import com.tourismgov.program.client.UserClient;
import com.tourismgov.program.dto.ProgramRequest;
import com.tourismgov.program.dto.ProgramResponse;
import com.tourismgov.program.enums.ProgramStatus;
import com.tourismgov.program.enums.ResourceStatus;
import com.tourismgov.program.enums.ResourceType;
import com.tourismgov.program.exceptions.ResourceNotFoundException;
import com.tourismgov.program.entity.Resource;
import com.tourismgov.program.entity.TourismProgram;
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
    private static final String MODULE_NAME = "ProgramModule";

    // Local Repositories
    private final TourismProgramRepository programRepository;
    private final ResourceRepository resourceRepository;

    // Microservice Feign Clients
    private final UserClient userClient; 
    private final NotificationClient notificationClient;
    private final EventClient eventClient;

    @Override
    @Transactional
    public ProgramResponse createProgram(ProgramRequest request) {
        log.info("Creating Tourism Program: {}", request.getTitle());
        
        // 1. Strict Date Validation
        validateProgramDates(request.getStartDate(), request.getEndDate(), true);

        TourismProgram program = new TourismProgram();
        mapRequestToEntity(request, program);
        
        // Using Enum directly for type safety
        program.setStatus(ProgramStatus.PLANNED);

        TourismProgram saved = programRepository.save(program);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 2. Cross-Service Audit Logging
        userClient.logAction(currentUserId, "CREATE_PROGRAM", MODULE_NAME, STATUS_SUCCESS);

        // 3. Cross-Service Notification Alert
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
            log.error("Failed to send creation notification to Notification Service: {}", e.getMessage());
        }

        return mapToProgramResponse(saved);
    }

    @Override
    @Transactional
    public ProgramResponse updateProgramStatus(Long id, String statusString) {
        TourismProgram p = programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, id));
        
        try {
            ProgramStatus status = ProgramStatus.valueOf(statusString.toUpperCase());
            p.setStatus(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status provided. Allowed: PLANNED, ACTIVE, COMPLETED, CANCELLED");
        }

        TourismProgram updated = programRepository.save(p);
        userClient.logAction(SecurityUtils.getCurrentUserId(), "UPDATE_STATUS", MODULE_NAME, STATUS_SUCCESS);
        
        return mapToProgramResponse(updated);
    }

    @Override
    public Map<String, Object> getBudgetReport(Long programId) {
        log.info("Generating budget report for Program ID: {}", programId);
        
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, programId));

        List<Resource> resources = resourceRepository.findByProgram_ProgramId(programId);
        double totalBudget = (program.getBudget() != null) ? program.getBudget() : 0.0;
        
        // Professional Stream filtering using Enum equality
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

    // --- STANDARD CRUD ---

    @Override 
    @Transactional
    public ProgramResponse updateProgram(Long programId, ProgramRequest request) {
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, programId));

        // Validate dates (isNew = false means it can keep its original past start date if it's already active)
        validateProgramDates(request.getStartDate(), request.getEndDate(), false);
        mapRequestToEntity(request, program);
        
        TourismProgram updated = programRepository.save(program);
        userClient.logAction(SecurityUtils.getCurrentUserId(), "UPDATE_PROGRAM", MODULE_NAME, STATUS_SUCCESS);
        return mapToProgramResponse(updated);
    }

    @Override 
    @Transactional
    public void deleteProgram(Long programId) {
        TourismProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, programId));
        
        // Microservice approach: Notify Event Service to unlink this program from its events
        try {
            eventClient.unlinkProgramFromAllEvents(programId);
        } catch (Exception e) {
            log.error("Failed to communicate with Event Service for cleanup. Deletion aborted to prevent orphaned records.", e);
            throw new IllegalStateException("Cannot delete program: Event Service is unreachable.");
        }
        
        programRepository.delete(program);
        userClient.logAction(SecurityUtils.getCurrentUserId(), "DELETE_PROGRAM", MODULE_NAME, STATUS_SUCCESS);
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
        
        // 1. A brand new program cannot be scheduled to start in the past
        if (isNew && start.isBefore(today)) {
            throw new IllegalArgumentException("A new program must start today or in the future. Start date cannot be in the past.");
        }
        
        // 2. The end date must be strictly after the start date
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End date (" + end + ") must be strictly after the start date (" + start + ").");
        }
    }

    private void mapRequestToEntity(ProgramRequest request, TourismProgram entity) {
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setBudget(request.getBudget());
    }

    private ProgramResponse mapToProgramResponse(TourismProgram program) {
        ProgramResponse res = new ProgramResponse();
        res.setProgramId(program.getProgramId());
        res.setTitle(program.getTitle());
        res.setDescription(program.getDescription());
        res.setStartDate(program.getStartDate());
        res.setEndDate(program.getEndDate());
        res.setBudget(program.getBudget());
        
        // Safely map the enum back to a String for the DTO
        res.setStatus(program.getStatus() != null ? program.getStatus().name() : null);
        
        // Fetch linked Heritage Site IDs from the Event microservice safely
        try {
            List<Long> siteIds = eventClient.getSiteIdsByProgram(program.getProgramId());
            res.setHeritageSiteIds(siteIds);
        } catch (Exception e) {
            log.warn("Could not fetch Heritage Site IDs from Event Service for Program ID: {}", program.getProgramId());
            res.setHeritageSiteIds(List.of()); // Fallback to empty list
        }
        
        return res;
    }
}