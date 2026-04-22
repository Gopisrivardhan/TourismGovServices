package com.tourismgov.tourist.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.tourismgov.tourist.enums.Gender;
import com.tourismgov.tourist.enums.Status;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tourist")
@Getter
@Setter
@NoArgsConstructor
public class Tourist {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long touristId;

	@Column(name = "name", nullable = false, length = 100)
	@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
	@Pattern(regexp = "^[A-Za-z]+( [A-Za-z]+)*$", message = "Name must contain only letters and spaces")
	private String name;

	@Column(name = "dob", nullable = false)
	@NotNull(message = "Date of Birth is required")
	private LocalDate dob;

	@Enumerated(EnumType.STRING)
	@Column(name = "gender", nullable = false)
	private Gender gender;

	@Column(name = "address", nullable = false)
	@Size(min = 5, max = 200, message = "Address must be between 5 and 200 characters")
	private String address;

	@Column(name = "contactInfo", unique = true, nullable = false, length = 10)
	@Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number format")
	private String contactInfo;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, columnDefinition = "ENUM('ACTIVE', 'BLOCKED', 'INACTIVE')")
	private Status status = Status.INACTIVE;

	@Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

	@OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<TouristDocument> documents = new ArrayList<>();

//	@OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//	private List<Booking> bookings = new ArrayList<>();
}
