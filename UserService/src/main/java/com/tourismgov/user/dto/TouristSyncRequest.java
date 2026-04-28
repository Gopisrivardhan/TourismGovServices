package com.tourismgov.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TouristSyncRequest {
    private Long userId;
    private String name;
    private String email;
    private String contactInfo;
}
