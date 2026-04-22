package com.tourismgov.report.repository;

import com.tourismgov.report.enums.ReportScope;
import com.tourismgov.report.model.Report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("SELECT r FROM Report r WHERE r.generatedByUserId = :userId " +
           "AND (:scope IS NULL OR r.scope = :scope) " +
           "AND (:startDate IS NULL OR r.generatedDate >= :startDate) " +
           "AND (:endDate IS NULL OR r.generatedDate <= :endDate) " +
           "ORDER BY r.generatedDate DESC") 
    List<Report> findMyReports(
            @Param("userId") Long userId,
            @Param("scope") ReportScope scope, 
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
