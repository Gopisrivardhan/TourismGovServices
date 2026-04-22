package com.tourismgov.report.service;

import com.tourismgov.report.dto.ReportRequestDTO;
import com.tourismgov.report.dto.ReportSummaryDTO;
import com.tourismgov.report.enums.ReportScope;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    // Method 1: Generate the snapshot
	ReportSummaryDTO generateReport(ReportRequestDTO request);

    // Method 2: Return lightweight summaries for the dashboard
    public List<ReportSummaryDTO> getReportHistory(Long userId, ReportScope scope, LocalDate date);

    // Method 3: Download report content
    byte[] downloadReport(Long reportId);
}
