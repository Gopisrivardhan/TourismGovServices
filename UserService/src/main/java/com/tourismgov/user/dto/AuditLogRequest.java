package com.tourismgov.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuditLogRequest {

    @NotNull
    private Long userId;

    @NotBlank
    @Size(max = 50)
    private String action;

    @NotBlank
    private String resource;

    @NotBlank
    @Size(max = 20)
    private String status;

	public Object getTimestamp() {
		// TODO Auto-generated method stub
		return null;
	}
}