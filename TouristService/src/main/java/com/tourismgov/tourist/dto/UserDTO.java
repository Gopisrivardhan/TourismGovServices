package com.tourismgov.tourist.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {

    private Long userId;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private String role;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    @Size(max = 20)
    private String phone;

    // ✅ ADDED: Needed so the Mapper can set "ACTIVE"
    private String status;

}