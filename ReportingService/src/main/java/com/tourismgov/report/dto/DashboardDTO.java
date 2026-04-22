package com.tourismgov.report.dto;

import lombok.*;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private String role;
    private Long userId;
    private String userName;
    
    // This Map will hold your 7-8 dynamic quantities (e.g., "upcomingEvents" -> 5)
    private Map<String, Object> metrics; 
    
    
    private long unreadNotifications;
}
