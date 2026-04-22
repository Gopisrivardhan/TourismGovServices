package com.tourismgov.tourist.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tourismgov.tourist.enums.Status;
import com.tourismgov.tourist.model.Tourist;

@Repository
public interface TouristRepository extends JpaRepository<Tourist, Long> {
	Optional<Tourist> findByContactInfo(String contactInfo);
	Page<Tourist> findAll(Pageable pageable);
	Page<Tourist> findByStatus(Status status, Pageable pageable);
}