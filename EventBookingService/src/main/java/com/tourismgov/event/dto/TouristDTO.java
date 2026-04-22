package com.tourismgov.event.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TouristDTO {
    private Long touristId;
    private Long userId; // The security user ID mapped to this tourist
    private String name;
    private String status; // e.g., ACTIVE or INACTIVE
}