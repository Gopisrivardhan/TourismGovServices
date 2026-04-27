package com.tourismgov.report.controller;

import com.tourismgov.report.dto.ReportRequestDTO;
import com.tourismgov.report.dto.ReportSummaryDTO;
import com.tourismgov.report.enums.ReportScope;
import com.tourismgov.report.service.ReportService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("tourismgov/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/generate")
    public ResponseEntity<ReportSummaryDTO> generate(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid ReportRequestDTO request) {
        
        log.info("Generating {} report for User: {}", request.getScope(), userId);
        
        // Pass the userId cleanly separated from the JSON request body
        ReportSummaryDTO response = reportService.generateReport(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ReportSummaryDTO>> getHistory(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) ReportScope scope,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
        
        List<ReportSummaryDTO> history = reportService.getReportHistory(userId, scope, date);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        byte[] data = reportService.downloadReport(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("Tourism_Report_" + id + ".txt").build());
        
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}