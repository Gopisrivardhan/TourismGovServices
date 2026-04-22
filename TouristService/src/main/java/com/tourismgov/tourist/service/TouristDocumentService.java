package com.tourismgov.tourist.service;

import com.tourismgov.tourist.dto.DocumentUploadRequest;
import com.tourismgov.tourist.dto.DocumentVerifyRequest;
import com.tourismgov.tourist.dto.TouristDocumentResponse;

/**
 * Service interface for managing tourist documents in the government tourism system.
 * Provides operations for uploading, verifying, retrieving metadata, and deleting documents.
 */
public interface TouristDocumentService {

    /**
     * Uploads a new document for a given tourist.
     * <p>
     * Business rules:
     * - Associates the uploaded document with the tourist profile.
     * - Typically used for identity verification, travel permits, or supporting documents.
     * </p>
     *
     * @param touristId the ID of the tourist to associate the document with
     * @param request   the document upload request containing file details and metadata
     * @return the uploaded document details as a response DTO
     */
    public TouristDocumentResponse uploadDocument(Long touristId, DocumentUploadRequest request);

    /**
     * Verifies a specific document belonging to a tourist.
     * <p>
     * Business rules:
     * - Updates verification status (e.g., VERIFIED, REJECTED).
     * - May trigger tourist status updates (ACTIVE/INACTIVE) depending on business logic.
     * </p>
     *
     * @param touristId  the ID of the tourist who owns the document
     * @param documentId the ID of the document to verify
     * @param request    the verification request containing status and remarks
     * @return the updated document details as a response DTO
     */
    public TouristDocumentResponse verifyDocument(Long touristId, Long documentId, DocumentVerifyRequest request);

    /**
     * Retrieves metadata for a specific document belonging to a tourist.
     * <p>
     * Metadata includes document type, status, upload date, and verification details.
     * </p>
     *
     * @param touristId  the ID of the tourist who owns the document
     * @param documentId the ID of the document to fetch metadata for
     * @return the document metadata as a response DTO
     */
    public TouristDocumentResponse getDocumentMetadata(Long touristId, Long documentId);

    /**
     * Deletes a specific document belonging to a tourist.
     * <p>
     * Business rules:
     * - Removes the document record and associated file.
     * - May affect tourist status if the document was required for verification.
     * </p>
     *
     * @param touristId  the ID of the tourist who owns the document
     * @param documentId the ID of the document to delete
     */
    public void deleteDocument(Long touristId, Long documentId);
}
