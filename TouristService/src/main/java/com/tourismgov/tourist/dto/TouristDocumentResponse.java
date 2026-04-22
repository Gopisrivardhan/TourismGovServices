package com.tourismgov.tourist.dto;

import java.time.LocalDateTime;

import com.tourismgov.tourist.enums.VerificationStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TouristDocumentResponse {
    private Long documentId;
    private String docType;
    private String fileUri;
    private LocalDateTime uploadedDate;
    private VerificationStatus verificationStatus;
    private String remarks;

}
