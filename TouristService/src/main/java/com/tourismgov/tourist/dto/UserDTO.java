package com.tourismgov.tourist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long userId;
    
    private String name;
    
    private String email;
    
    private String phone;
    
    private String role; // e.g., "TOURIST", "ADMIN", "OFFICER"
    
    private String status;
    
    private String password;
}