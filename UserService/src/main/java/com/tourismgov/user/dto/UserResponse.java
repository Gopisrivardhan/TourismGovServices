package com.tourismgov.user.dto;

import com.tourismgov.user.enums.Role;
import com.tourismgov.user.enums.Status;

import lombok.Data;

@Data
public class UserResponse {

	private Long userId;
	private String name;
	private Role role;
	private String email;
	private String phone;
	private Status status;
}