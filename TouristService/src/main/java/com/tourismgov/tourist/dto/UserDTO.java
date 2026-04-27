package com.tourismgov.tourist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
	private Long userId;
    private String name;
    private String role;     // Enum mapping handles String -> Role automatically
    private String email;
    private String password;
    private String phone;
    private String status;   // E.g., "ACTIVE" or "PENDING"
}