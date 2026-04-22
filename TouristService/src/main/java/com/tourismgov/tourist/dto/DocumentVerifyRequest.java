package com.tourismgov.tourist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentVerifyRequest {
	@NotBlank(message = "Verification status is required")
	@Pattern(
	    regexp = "^(PENDING|VERIFIED|REJECTED)$", 
	    flags = Pattern.Flag.CASE_INSENSITIVE, 
	    message = "Invalid status. Must be: PENDING, VERIFIED, or REJECTED"
	)
    private String status;
	private String remarks;
}