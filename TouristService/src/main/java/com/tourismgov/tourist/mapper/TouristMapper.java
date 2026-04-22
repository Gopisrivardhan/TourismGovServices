package com.tourismgov.tourist.mapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.tourismgov.tourist.dto.DocumentUploadRequest;
import com.tourismgov.tourist.dto.TouristDocumentResponse;
import com.tourismgov.tourist.dto.TouristRequest;
import com.tourismgov.tourist.dto.TouristResponse;
import com.tourismgov.tourist.dto.TouristUpdateRequest;
import com.tourismgov.tourist.dto.UserDTO;
import com.tourismgov.tourist.enums.Status;
import com.tourismgov.tourist.enums.VerificationStatus;
import com.tourismgov.tourist.model.Tourist;
import com.tourismgov.tourist.model.TouristDocument;

@Component
public class TouristMapper {

	/**
     * Maps TouristRequest to a UserDTO for the User Service.
     * This allows you to create the User record first.
     */
	public UserDTO toUserDTO(TouristRequest request) {
	    if (request == null) return null;

	    UserDTO dto = new UserDTO();
	    dto.setName(request.getName());
	    dto.setEmail(request.getEmail());
	    dto.setPhone(request.getContactInfo());
	    dto.setPassword(request.getPassword()); 
	    dto.setRole("TOURIST");
	    dto.setStatus("ACTIVE");
	    return dto;
	}

    /**
     * Maps TouristRequest to a new Tourist entity for local DB.
     */
    public Tourist toTouristEntity(TouristRequest request, Long userId) {
        if (request == null) return null;

        Tourist tourist = new Tourist();
        tourist.setUserId(userId); // Link to the ID returned by User Service
        tourist.setName(request.getName());
        tourist.setDob(request.getDob());
        tourist.setGender(request.getGender());
        tourist.setAddress(request.getAddress());
        tourist.setContactInfo(request.getContactInfo());
        tourist.setStatus(Status.INACTIVE); 
        return tourist;
    }

    /**
     * Converts Entity to Response DTO
     */
    public TouristResponse toResponse(Tourist tourist) {
        if (tourist == null) return null;

        TouristResponse response = new TouristResponse();
        response.setTouristId(tourist.getTouristId());
        response.setName(tourist.getName());
        response.setDob(tourist.getDob());
        response.setGender(tourist.getGender());
        response.setAddress(tourist.getAddress());
        response.setContactInfo(tourist.getContactInfo());
        response.setStatus(tourist.getStatus());

        if (tourist.getDocuments() != null) {
            response.setDocuments(tourist.getDocuments().stream()
                    .map(this::toDocumentResponse)
                    .collect(Collectors.toList()));
        } else {
            response.setDocuments(Collections.emptyList());
        }

        return response;
    }

    /**
     * Converts TouristDocument Entity to Response DTO
     */
    public TouristDocumentResponse toDocumentResponse(TouristDocument doc) {
        if (doc == null) return null;

        TouristDocumentResponse response = new TouristDocumentResponse();
        response.setDocumentId(doc.getDocumentId());
        response.setDocType(doc.getDocType());
        response.setFileUri(doc.getFileUri());
        response.setUploadedDate(doc.getUploadedDate());
        response.setVerificationStatus(doc.getVerificationStatus());
        response.setRemarks(doc.getRemarks());
        return response;
    }

    /**
     * Updates an EXISTING Entity from an Update Request DTO
     */
    public void updateEntityFromRequest(TouristUpdateRequest request, Tourist tourist) {
        if (request == null || tourist == null) return;

        tourist.setName(request.getName());
        tourist.setDob(request.getDob());
        tourist.setGender(request.getGender());
        tourist.setAddress(request.getAddress());
        tourist.setContactInfo(request.getContactInfo());
    }

    /**
     * Maps Document Upload Request to Entity
     */
    public TouristDocument toDocumentEntity(DocumentUploadRequest request, Tourist tourist, String storedFileUri) {
        if (request == null) return null;

        return TouristDocument.builder()
                .tourist(tourist)
                .docType(request.getDocType())
                .fileUri(storedFileUri)
                .uploadedDate(LocalDateTime.now())
                .verificationStatus(VerificationStatus.PENDING)
                .remarks("Document uploaded successfully. Awaiting officer verification.")
                .build();
    }
}