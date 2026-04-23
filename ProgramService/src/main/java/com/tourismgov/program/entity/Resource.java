package com.tourismgov.program.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.tourismgov.program.enums.ResourceStatus;
import com.tourismgov.program.enums.ResourceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "resources")
@Getter
@Setter
@NoArgsConstructor
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Long resourceId;
	
    @Enumerated(EnumType.STRING) // CRITICAL: Saves enum as text in DB
    @Column(name = "resource_type", nullable = false, length = 50)
    private ResourceType type;

    @Column(nullable = false)
    private Double quantity; 

    
    @Enumerated(EnumType.STRING) // CRITICAL: Saves enum as text in DB
    @Column(nullable = false, length = 50)
    private ResourceStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    @JsonBackReference // FIX: Prevents infinite looping when paired with @JsonManagedReference
    private TourismProgram program;
}