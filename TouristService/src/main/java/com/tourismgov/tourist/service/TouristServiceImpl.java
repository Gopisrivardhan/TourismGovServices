package com.tourismgov.tourist.service;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tourismgov.tourist.client.UserClient;
import com.tourismgov.tourist.client.NotificationClient;
import com.tourismgov.tourist.dto.TouristRequest;
import com.tourismgov.tourist.dto.TouristResponse;
import com.tourismgov.tourist.dto.TouristSummaryResponse;
import com.tourismgov.tourist.dto.TouristUpdateRequest;
import com.tourismgov.tourist.dto.TouristSyncRequest;
import com.tourismgov.tourist.dto.UserDTO;
import com.tourismgov.tourist.enums.Status;
import com.tourismgov.tourist.exception.TouristErrorMessage;
import com.tourismgov.tourist.mapper.TouristMapper;
import com.tourismgov.tourist.model.Tourist;
import com.tourismgov.tourist.repository.TouristRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TouristServiceImpl implements TouristService {

	private final TouristRepository touristRepository;
	private final TouristMapper touristMapper;
	private final UserClient userClient;
	private final NotificationClient notificationClient;

	@Override
	@Transactional
	public TouristResponse createTourist(TouristRequest request) {
	    log.info("Starting Dual Registration for: {}", request.getEmail());

	    // 1. Convert Request to UserDTO
	    UserDTO newUserRequest = touristMapper.toUserDTO(request);

	    // 2. Call User Service to create the record
	    UserDTO savedUser;
	    try {
	        // This call performs the INSERT into the user table
	        savedUser = userClient.registerUser(newUserRequest);
	        log.info("User created successfully with ID: {}", savedUser.getUserId());
	    } catch (Exception e) {
	        log.error("User Service registration failed: {}", e.getMessage());
	        // If User Service returns 409 Conflict or 400, we pass it along
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User registration failed. Email might already exist.");
	    }

	    // 3. Use the ID from savedUser to create the Tourist locally
	    Tourist tourist = touristMapper.toTouristEntity(request, savedUser.getUserId());
	    validateAdult(tourist);
	    
	    Tourist savedTourist = touristRepository.save(tourist);
	    
	    // Send private welcome notification
	    try {
	        notificationClient.sendSystemAlert(
	                savedUser.getUserId(), 
	                savedTourist.getTouristId(), 
	                "Welcome to TourismGov!", 
	                "Your registration was successful. Welcome aboard, " + savedUser.getName() + "!", 
	                "SYSTEM");
	    } catch (Exception e) {
	        log.error("Failed to send welcome notification: {}", e.getMessage());
	    }
	    
	    return touristMapper.toResponse(savedTourist);
	}

	@Override
	public TouristResponse getTouristById(Long touristId) {
		log.info("Fetching tourist with ID: {}", touristId);
		Tourist tourist = findTouristByIdOrThrow(touristId);
		
		// Security validation removed
		
		log.info("Tourist {} fetched successfully", touristId);
		return touristMapper.toResponse(tourist);
	}

	@Override
	@Transactional
	public TouristResponse updateTourist(Long touristId, TouristUpdateRequest request) {
		log.info("Updating tourist profile for ID: {}", touristId);
		Tourist tourist = findTouristByIdOrThrow(touristId);
		
		// Security validation removed
		
		touristMapper.updateEntityFromRequest(request, tourist);
		validateAdult(tourist);

		tourist = touristRepository.save(tourist);
		log.info("Tourist ID {} updated successfully", touristId);

		return touristMapper.toResponse(tourist);
	}

	@Override
	@Transactional
	public void deleteTourist(Long touristId) {
		log.info("Attempting to delete tourist with ID: {}", touristId);

		Tourist tourist = findTouristByIdOrThrow(touristId);
		
		// Security validation removed

		touristRepository.delete(tourist);
		log.info("Tourist {} deleted successfully", touristId);
	}

	@Override
	public Page<TouristSummaryResponse> getTouristSummariesByStatus(Status status, Pageable pageable) {
		Page<Tourist> page = (status != null) ? touristRepository.findByStatus(status, pageable)
				: touristRepository.findAll(pageable);
		log.info("Fetched {} tourist records", page.getTotalElements());
		return page.map(t -> new TouristSummaryResponse(t.getTouristId(), t.getName(), t.getStatus()));
	}

	@Override
	@Transactional
	public void syncTouristProfile(TouristSyncRequest request) {
	    log.info("Internal Sync: Creating tourist profile for user ID {}", request.getUserId());
	    
	    if (touristRepository.findById(request.getUserId()).isPresent()) {
	        log.info("Tourist profile already exists for user ID {}. Skipping sync.", request.getUserId());
	        return;
	    }
	    
	    Tourist tourist = new Tourist();
	    tourist.setTouristId(request.getUserId()); // Use same ID as User
	    tourist.setUserId(request.getUserId());
	    tourist.setName(request.getName());
	    tourist.setContactInfo(request.getContactInfo());
	    tourist.setStatus(Status.ACTIVE);
	    // DOB, Gender, Address will remain null and can be updated later
	    
	    touristRepository.save(tourist);
	    log.info("Internal Sync: Tourist profile saved for user ID {}", request.getUserId());
	}

	private Tourist findTouristByIdOrThrow(Long touristId) {
		return touristRepository.findById(touristId).orElseThrow(() -> {
			log.error("Tourist {} not found", touristId);
			return new ResponseStatusException(HttpStatus.NOT_FOUND,
					String.format(TouristErrorMessage.ERROR_TOURIST_NOT_FOUND, touristId));
		});
	}

	private void validateAdult(Tourist tourist) {
		if (tourist.getDob() != null && Period.between(tourist.getDob(), LocalDate.now()).getYears() < 18) {
			log.error("Tourist {} is under 18 years old", tourist.getName());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TouristErrorMessage.ERROR_UNDERAGE_TOURIST);
		}
	}
}