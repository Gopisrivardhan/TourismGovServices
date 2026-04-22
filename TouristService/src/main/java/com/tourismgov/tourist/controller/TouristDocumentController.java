package com.tourismgov.tourist.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tourismgov.tourist.dto.DocumentUploadRequest;
import com.tourismgov.tourist.dto.DocumentVerifyRequest;
import com.tourismgov.tourist.dto.TouristDocumentResponse;
import com.tourismgov.tourist.service.TouristDocumentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/tourismgov/v1/touristdoc")
public class TouristDocumentController {

	private final TouristDocumentService touristDocumentService;

	// Upload Document
	@PostMapping("/{touristId}/documents")
	public ResponseEntity<TouristDocumentResponse> uploadDocument(@PathVariable Long touristId,
			@ModelAttribute DocumentUploadRequest request) {
		TouristDocumentResponse response = touristDocumentService.uploadDocument(touristId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// Verify Document
	@PatchMapping("/{touristId}/documents/{documentId}/verify")
	public ResponseEntity<TouristDocumentResponse> verifyDocument(@PathVariable Long touristId,
			@PathVariable Long documentId, @Valid @RequestBody DocumentVerifyRequest request) {
		log.info("Received verification request for Doc: {} with Status: {} and Remarks: {}", 
	             documentId, request.getStatus(), request.getRemarks());
		TouristDocumentResponse response = touristDocumentService.verifyDocument(touristId, documentId, request);
		return ResponseEntity.ok(response);
	}

	// View Document
	@GetMapping("/{touristId}/documents/{documentId}/view")
	public ResponseEntity<TouristDocumentResponse> viewDocument(@PathVariable Long touristId,
			@PathVariable Long documentId) {
		TouristDocumentResponse response = touristDocumentService.getDocumentMetadata(touristId, documentId);
		return ResponseEntity.ok(response);
	}

	// Delete Document
	@DeleteMapping("/{touristId}/documents/{documentId}")
	public ResponseEntity<String> deleteDocument(@PathVariable Long touristId, @PathVariable Long documentId) {
		touristDocumentService.deleteDocument(touristId, documentId);
		return ResponseEntity.ok("Document deleted successfully");
	}

}
