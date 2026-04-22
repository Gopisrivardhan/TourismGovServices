package com.tourismgov.program.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tourismgov.program.entity.TourismProgram;
import com.tourismgov.program.enums.ProgramStatus;

@Repository
public interface TourismProgramRepository extends JpaRepository<TourismProgram, Long> {

    boolean existsByTitleIgnoreCase(String title);
    Optional<TourismProgram> findByTitle(String title);

    // CHANGED: Uses ProgramStatus Enum
    List<TourismProgram> findByStatus(ProgramStatus status);

    List<TourismProgram> findByStartDateBeforeAndEndDateAfter(LocalDate date1, LocalDate date2);
}