package com.tourismgov.compliance.entity;

import java.time.LocalDateTime;

import com.tourismgov.compliance.enums.ComplianceResult;
import com.tourismgov.compliance.enums.ComplianceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "compliance_records")
@Getter 
@Setter 
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceRecord extends BaseEntity{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long complianceId;

    private String referenceNumber;
    private Long entityId; 
    @Enumerated(EnumType.STRING)
    @Column(name = "compliance_type", nullable = false)
    private ComplianceType type;   

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false)
    private ComplianceResult result;
    private LocalDateTime date;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
}