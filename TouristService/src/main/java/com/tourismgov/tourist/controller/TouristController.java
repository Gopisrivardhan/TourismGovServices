package com.tourismgov.tourist.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tourismgov.tourist.dto.TouristRequest;
import com.tourismgov.tourist.dto.TouristResponse;
import com.tourismgov.tourist.dto.TouristSummaryResponse;
import com.tourismgov.tourist.dto.TouristUpdateRequest;
import com.tourismgov.tourist.enums.Status;
import com.tourismgov.tourist.service.TouristService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/tourismgov/v1/tourist")
public class TouristController {

	private final TouristService touristService;

	// Tourist Registration
	@PostMapping("/create")
	public ResponseEntity<TouristResponse> createTourist(@Valid @RequestBody TouristRequest request) {
		log.info("API: create tourist called");
		TouristResponse response = touristService.createTourist(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// Tourist Profile
	@GetMapping("/{touristId}")
	public ResponseEntity<TouristResponse> getTouristProfile(@PathVariable Long touristId) {
		TouristResponse response = touristService.getTouristById(touristId);
		return ResponseEntity.ok(response);
	}

	// Tourist Profile (Edit)
	@PutMapping("/{touristId}/update")
	public ResponseEntity<TouristResponse> updateTouristProfile(@PathVariable Long touristId,
			@Valid @RequestBody TouristUpdateRequest request) {
		TouristResponse response = touristService.updateTourist(touristId, request);
		return ResponseEntity.ok(response);
	}

	// Delete Tourist
	@DeleteMapping("/{touristId}")
	public ResponseEntity<String> deleteTourist(@PathVariable Long touristId) {
		touristService.deleteTourist(touristId);
		return ResponseEntity.ok("Tourist deleted successfully");
	}

	// List of Profiles
	@GetMapping("/admin")
	public ResponseEntity<Page<TouristSummaryResponse>> getTourists(@RequestParam(required = false) Status status,
			Pageable pageable) {
		Page<TouristSummaryResponse> response = touristService.getTouristSummariesByStatus(status, pageable);
		return ResponseEntity.ok(response);
	}

}
