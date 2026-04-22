package com.tourismgov.tourist.dto;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentUploadRequest {

    @NotBlank(message = "Document type is required (e.g., Passport, IDProof)")
    private String docType;

    private MultipartFile file;   // for physical upload
    private String fileUri;       // for direct URI storage
}
