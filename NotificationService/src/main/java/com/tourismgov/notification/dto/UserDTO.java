package com.tourismgov.notification.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long userId;
    private String name;
    private String email;
    private String role; // Match this with your Role enum string (e.g., "ADMIN", "TOURIST")
}