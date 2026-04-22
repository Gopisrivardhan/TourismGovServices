package com.tourismgov.tourist.model;

import java.time.LocalDateTime;

import com.tourismgov.tourist.enums.VerificationStatus;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tourist_documents")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TouristDocument{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "document_id")
	private Long documentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tourist_id", nullable = false)
	private Tourist tourist;

	@Column(name = "doc_type", nullable = false)
	private String docType; // IDProof/Passport

	@Column(name = "uploaded_Date", nullable = false)
	private LocalDateTime uploadedDate;

	@Column(name = "file_uri", nullable = false)
	private String fileUri;

	@Column(name = "verification_status")
	@Enumerated(EnumType.STRING)
	private VerificationStatus verificationStatus;
	
	@Column(length = 500)
	private String remarks;
}