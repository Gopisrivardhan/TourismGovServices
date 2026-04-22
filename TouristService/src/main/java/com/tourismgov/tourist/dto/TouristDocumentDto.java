package com.tourismgov.tourist.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TouristDocumentDto {
    private Long documentId;
    private String docType; // IDProof/Passport
    private String fileUri;
    private String verificationStatus;
}