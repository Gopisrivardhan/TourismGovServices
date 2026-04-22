package com.tourismgov.report.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

import com.tourismgov.report.enums.ReportScope;

@Entity
@Table(name = "reports")  
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @NotNull(message = "Report scope is mandatory (e.g., SITE, EVENT, PROGRAM)")
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private ReportScope scope;

    @NotBlank(message = "Report metrics data cannot be empty or null")
    @Column(name = "metrics", columnDefinition = "TEXT")
    private String metrics;

    @NotNull(message = "Generated date must be recorded")
    @Column(name = "generated_date", nullable = false)
    private LocalDateTime generatedDate;

    @NotNull(message = "The User who generated this report must be identified")
    @Column(name = "generated_by_user_id", nullable = false)
    private Long generatedByUserId;
}
