package com.tourismgov.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.tourismgov.report.dto.ComplianceDTO;

import java.util.List;

@FeignClient(name = "tourismgov-app")
public interface ComplianceClient {
    @GetMapping("/tourismgov/v1/compliance")
    List<ComplianceDTO> getAllComplianceRecords();
}
