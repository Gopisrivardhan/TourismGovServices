package com.tourismgov.tourist.service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tourismgov.tourist.dto.DocumentUploadRequest;
import com.tourismgov.tourist.dto.DocumentVerifyRequest;
import com.tourismgov.tourist.dto.TouristDocumentResponse;
import com.tourismgov.tourist.enums.Status;
import com.tourismgov.tourist.enums.VerificationStatus;
import com.tourismgov.tourist.exception.TouristErrorMessage;
import com.tourismgov.tourist.mapper.TouristMapper;
import com.tourismgov.tourist.model.Tourist;
import com.tourismgov.tourist.model.TouristDocument;
import com.tourismgov.tourist.repository.TouristDocumentRepository;
import com.tourismgov.tourist.repository.TouristRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TouristDocumentServiceImpl implements TouristDocumentService {

	private final TouristDocumentRepository documentRepository;
	private final TouristRepository touristRepository;
	private final TouristMapper touristMapper; 

	@Override
	@Transactional
	public TouristDocumentResponse uploadDocument(Long touristId, DocumentUploadRequest request) {
		log.info("Uploading document for touristId={} with docType={}", touristId, request.getDocType());

		// Validate tourist
		Tourist tourist = touristRepository.findById(touristId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format("Tourist not found with id %d", touristId)));

		// Prevent duplicate docType per tourist
		boolean exists = tourist.getDocuments().stream()
				.anyMatch(d -> d.getDocType().equalsIgnoreCase(request.getDocType()));
		if (exists) {
			log.warn("Duplicate document type {} detected for tourist {}", request.getDocType(), touristId);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Tourist already has a document of type %s", request.getDocType()));
		}

		String storedFileUri;
		try {
			if (request.getFile() != null && !request.getFile().isEmpty()) {
				// Handle physical file upload
				Path filePath = Paths.get("uploads", String.valueOf(touristId),
						System.currentTimeMillis() + "_" + request.getFile().getOriginalFilename());
				Files.createDirectories(filePath.getParent());
				Files.copy(request.getFile().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
				storedFileUri = filePath.toUri().toString(); // file:// URI
				log.info("File uploaded successfully for tourist {} at {}", touristId, storedFileUri);
			} else if (request.getFileUri() != null && !request.getFileUri().isBlank()) {
				String uri = request.getFileUri().trim();
				if (uri.startsWith("http://") || uri.startsWith("https://")) {
					// Remote URL → store directly
					storedFileUri = uri;
					log.info("Remote file URI stored for tourist {}: {}", touristId, storedFileUri);
				} else {
					log.warn("Upload failed: Invalid URI protocol for tourist {}", touristId);
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
							TouristErrorMessage.ERROR_INVALID_URI_PROTOCOL);
				}
			} else {
				log.warn("Upload failed: Missing file or fileUri for tourist {}", touristId);
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TouristErrorMessage.ERROR_MISSING_FILE_DATA);
			}
		} catch (IOException e) {
			log.warn("File save failed for tourist {}: {}", touristId, e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					TouristErrorMessage.ERROR_FILE_SAVE_FAILED);
		}

		// Use Mapper to build document
		TouristDocument doc = touristMapper.toDocumentEntity(request, tourist, storedFileUri);

		TouristDocument saved = documentRepository.save(doc);
		syncTouristStatus(tourist);
		log.info("Document {} uploaded successfully for tourist {}", saved.getDocumentId(), touristId);
		
		// Use Mapper for response
		return touristMapper.toDocumentResponse(saved);
	}

	@Override
	@Transactional
	public TouristDocumentResponse verifyDocument(Long touristId, Long documentId, DocumentVerifyRequest request) {
		log.info("Verifying document {} for tourist {}", documentId, touristId);
		TouristDocument doc = getTouristDocumentOrThrow(touristId, documentId);

		VerificationStatus newStatus;
		try {
			newStatus = VerificationStatus.valueOf(request.getStatus().toUpperCase());
		} catch (IllegalArgumentException e) {
			log.warn("Invalid verification status '{}' for document {} of tourist {}", request.getStatus(), documentId,
					touristId);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format(TouristErrorMessage.ERROR_INVALID_VERIFICATION_STATUS, request.getStatus()));
		}

		doc.setVerificationStatus(newStatus);
		doc.setRemarks(request.getRemarks());
		documentRepository.save(doc);
		log.info("Document {} for tourist {} updated to status {}", documentId, touristId, newStatus);

		syncTouristStatus(doc.getTourist());
		
		// Use Mapper for response
		return touristMapper.toDocumentResponse(doc);
	}

	@Override
	public TouristDocumentResponse getDocumentMetadata(Long touristId, Long documentId) {
		log.info("Fetching metadata for document {} of tourist {}", documentId, touristId);
		TouristDocument doc = getTouristDocumentOrThrow(touristId, documentId);
		log.info("Metadata fetched successfully for document {} of tourist {}", documentId, touristId);
		
		// Use Mapper for response
		return touristMapper.toDocumentResponse(doc);
	}

	@Override
	@Transactional
	public void deleteDocument(Long touristId, Long documentId) {
		log.info("Deleting document {} for tourist {}", documentId, touristId);
		TouristDocument doc = getTouristDocumentOrThrow(touristId, documentId);
		Tourist tourist = doc.getTourist();

		try {
			String fileUri = doc.getFileUri();
			if (fileUri != null && !(fileUri.startsWith("http://") || fileUri.startsWith("https://"))) {
				Path filePath = Paths.get(URI.create(fileUri));
				Files.deleteIfExists(filePath);
				log.info("File deleted for document {} of tourist {}", documentId, touristId);
			}
		} catch (Exception e) {
			log.error("Failed to delete file for document {}: {}", documentId, e.getMessage());
		}

		tourist.getDocuments().remove(doc);
		documentRepository.delete(doc);
		log.info("Document {} deleted successfully for tourist {}", documentId, touristId);

		syncTouristStatus(tourist);
	}

	private TouristDocument getTouristDocumentOrThrow(Long touristId, Long documentId) {
		touristRepository.findById(touristId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
				String.format(TouristErrorMessage.ERROR_TOURIST_NOT_FOUND, touristId)));

		return documentRepository.findByDocumentIdAndTourist_TouristId(documentId, touristId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format(TouristErrorMessage.ERROR_DOCUMENT_NOT_FOUND, documentId, touristId)));
	}

	private void syncTouristStatus(Tourist tourist) {
		List<TouristDocument> docs = tourist.getDocuments();
		if (docs == null || docs.isEmpty()) {
			tourist.setStatus(Status.INACTIVE);
		} else {
			boolean hasRejected = docs.stream()
					.anyMatch(d -> d.getVerificationStatus() == VerificationStatus.REJECTED);

			boolean hasPending = docs.stream()
					.anyMatch(d -> d.getVerificationStatus() == VerificationStatus.PENDING);

			boolean allVerified = docs.stream()
					.allMatch(d -> d.getVerificationStatus() == VerificationStatus.VERIFIED);

			if (hasRejected || hasPending) {
				tourist.setStatus(Status.INACTIVE);
			} else if (allVerified) {
				tourist.setStatus(Status.ACTIVE);
			} else {
				tourist.setStatus(Status.INACTIVE);
			}
		}
		touristRepository.save(tourist);
	}
}