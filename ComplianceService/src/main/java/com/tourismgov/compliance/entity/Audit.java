package com.tourismgov.compliance.entity;

import java.time.LocalDateTime;

import com.tourismgov.compliance.enums.AuditStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audits")
@Getter              
@Setter              
@NoArgsConstructor
public class Audit extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    // Cross-service ID reference (User Service)
    @Column(name = "officer_id", nullable = false)
    private Long officerId;

    @Column(length = 100)
    private String scope;

    @Column(columnDefinition = "TEXT")
    private String findings;

    @Column(name = "audit_date")
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private AuditStatus status = AuditStatus.PLANNED;
}