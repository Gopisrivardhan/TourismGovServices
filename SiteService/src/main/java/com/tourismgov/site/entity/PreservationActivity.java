package com.tourismgov.site.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "preservation_activities")
@Getter
@Setter
@NoArgsConstructor 
public class PreservationActivity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private HeritageSite site;

    @Column(name = "activity_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "activity_date")
    private LocalDateTime date;

    @Column(name = "activity_status")
    private String status = "IN_PROGRESS";

    // Cross-service ID reference (User Service)
    @Column(name = "officer_id", nullable = false)
    private Long officerId;
}