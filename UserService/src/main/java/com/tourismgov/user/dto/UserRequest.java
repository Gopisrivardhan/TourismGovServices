package com.tourismgov.user.dto;

import com.tourismgov.user.enums.Role;
import com.tourismgov.user.enums.Status;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private Role role;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    @Size(max = 20)
    private String phone;

    private Status status;
}